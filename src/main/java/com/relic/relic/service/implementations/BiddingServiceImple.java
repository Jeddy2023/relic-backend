package com.relic.relic.service.implementations;

import com.relic.relic.dto.*;
import com.relic.relic.enums.Role;
import com.relic.relic.exception.BiddingUnauthorizedException;
import com.relic.relic.exception.CustomException;
import com.relic.relic.exception.ResourceNotFoundException;
import com.relic.relic.model.Bid;
import com.relic.relic.model.Order;
import com.relic.relic.model.Product;
import com.relic.relic.model.User;
import com.relic.relic.repository.BiddingRepository;
import com.relic.relic.repository.OrderRepository;
import com.relic.relic.repository.ProductRepository;
import com.relic.relic.service.BiddingService;
import com.relic.relic.service.NotificationService;
import com.relic.relic.service.OrderService;
import com.relic.relic.util.UserAuthenticationHelper;
import jakarta.transaction.Transactional;
import lombok.NoArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class BiddingServiceImple implements BiddingService {
    private ProductRepository productRepository;
    private BiddingRepository biddingRepository;
    private OrderRepository orderRepository;
    private UserAuthenticationHelper userAuthenticationHelper;
    private ModelMapper modelMapper;
    private final NotificationService notificationService;
    @Autowired
    private OrderService orderService;

    public BiddingServiceImple(ProductRepository productRepository, BiddingRepository biddingRepository, OrderRepository orderRepository, UserAuthenticationHelper userAuthenticationHelper, ModelMapper modelMapper, NotificationService notificationService) {
        this.productRepository = productRepository;
        this.biddingRepository = biddingRepository;
        this.orderRepository = orderRepository;
        this.userAuthenticationHelper = userAuthenticationHelper;
        this.modelMapper = modelMapper;
        this.notificationService = notificationService;
    }

    @Override
    public String placeBid(Long productId, BidDto bidDto) {
        if (bidDto == null || bidDto.getBidAmount() == null) {
            throw new CustomException("All fields are required");
        }

        User user = userAuthenticationHelper.getLoggedInUser();

        if(user.getRole() == Role.ADMIN){
            throw new CustomException("Admin users cannot place bids");
        }

        if(user.isBlocked()){
            throw new CustomException("You are not authorised to access this resource you are blocked");
        }

        if(!user.isVerified()){
            throw new CustomException("Please verify your email to access this");
        }

        Optional<Product> productOptional = productRepository.findById(productId);
        if (productOptional.isEmpty()) {
            throw new ResourceNotFoundException("Product not found");
        }

        Product product = productOptional.get();
        if (product.isBiddingClosed()) {
            throw new CustomException("Bidding is closed for this product");
        }

        if(Objects.equals(user.getId(), product.getVendor().getId())){
            throw new BiddingUnauthorizedException("You cannot place a bid on your own product");
        }

        if (bidDto.getBidAmount().compareTo(product.getMinimumBid()) < 0) {
            throw new CustomException("Bid amount must be greater than or equal to the minimum bid");
        }

        Long currentBid = product.getCurrentBid() != null ? product.getCurrentBid() : 0L;
        if (bidDto.getBidAmount().compareTo(currentBid) <= 0) {
            throw new CustomException("Bid amount must be greater than ₦" + product.getCurrentBid());
        }

        // Create a new bid
        Bid bid = new Bid();
        bid.setBidder(user);
        bid.setProduct(product);
        bid.setBidAmount(bidDto.getBidAmount());
        bid.setIsWinningBid(true);
        biddingRepository.save(bid);

        // Update the product's current bid
        product.setCurrentBid(bidDto.getBidAmount());
        productRepository.save(product);

        // Notify the vendor of the new bid
        notificationService.sendNotification("New bid has been placed on " + product.getName() + " by " + user.getUsername(), product.getVendor().getId());

        // Update the status of the previous highest bid
        List<Bid> previousBids = biddingRepository.findBidsByProductIdOrderByBidAmountDesc(productId);
        if (previousBids.size() > 1) {
            Bid previousBid = previousBids.get(1);
            previousBid.setIsWinningBid(false);
            biddingRepository.save(previousBid);
            User previousBidder = previousBid.getBidder();
            notificationService.sendNotification("You have been outbid on " + product.getName(), previousBidder.getId());
        }

        return "Bid has been placed successfully";
    }

    @Override
    public List<BidListDto> getBiddingList(Long productId) {
        List<Bid> biddingList = biddingRepository.findBidsByProductIdOrderByBidAmountDesc(productId);
        if (biddingList.isEmpty()) {
            return Collections.emptyList();
        }
        return biddingList.stream().map(bid -> modelMapper.map(bid, BidListDto.class)).collect(Collectors.toList());
    }

    @Override
    public BidListDto getWinningBid(Long productId) {
        Optional<Product> productOptional = productRepository.findById(productId);
        if (productOptional.isEmpty()) {
            throw new ResourceNotFoundException("Product not found");
        }

        Bid winningBid = biddingRepository.findByProductIdAndIsWinningBidTrue(productId);
        if (winningBid == null) {
            return null;
        }

        return modelMapper.map(winningBid, BidListDto.class);
    }

    @Override
    public String closeBidding(Long productId) {
        Optional<Product> productOptional = productRepository.findById(productId);
        if (productOptional.isEmpty()) {
            throw new ResourceNotFoundException("Product not found");
        }
        User user = userAuthenticationHelper.getLoggedInUser();
        Product product = productOptional.get();
        if(product.isBiddingClosed()){
            throw new CustomException("Auction is already closed");
        }
        if(product.getVendor() != user){
            throw new BiddingUnauthorizedException("You cannot close this bid because the product does not belong to you");
        }
        product.setBiddingClosed(true);
        productRepository.save(product);

        List<User> bidders = biddingRepository.findDistinctBiddersByProductId(productId);
        if (bidders.size() > 0) {
            bidders.forEach(bidder -> notificationService.sendNotification(
                    "Bidding has closed for " + product.getName(), bidder.getId()));
        }


        return "Bidding has been closed for product " + product.getName();
    }

    @Override
    public String reopenBidding(Long productId) throws ParseException {
        Optional<Product> productOptional = productRepository.findById(productId);
        if (productOptional.isEmpty()) {
            throw new ResourceNotFoundException("Product not found");
        }

        User user = userAuthenticationHelper.getLoggedInUser();
        Product product = productOptional.get();
        if(!product.isBiddingClosed()){
            throw new CustomException("Auction is already opened");
        }
        if(product.getVendor() != user){
            throw new BiddingUnauthorizedException("You cannot re open this bid because the product does not belong to you");
        }

        if (product.getWinningBidder() != null) {
            throw new CustomException("Product already has a winner, bidding is not allowed");
        }

        product.setBiddingClosed(false);
        productRepository.save(product);

        List<User> biddingList = biddingRepository.findDistinctBiddersByProductId(productId);
        if (biddingList.size() > 0) {
            biddingList.forEach(bidder -> notificationService.sendNotification(
                    "Bidding has reopened for " + product.getName(), bidder.getId()));
        }

        Date currentDate = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return currentDate.after(formatter.parse(product.getBiddingTimeLimit())) ? "Successfully reopened bid but please update the Bidding Time Limit " : "Successfully reopened bid ";
    }

    @Override
    public String declareWinner(Long productId) {
        Optional<Product> productOptional = productRepository.findById(productId);
        if (productOptional.isEmpty()) {
            throw new ResourceNotFoundException("Product not found");
        }

        User user = userAuthenticationHelper.getLoggedInUser();
        Product product = productOptional.get();
        if(product.getVendor() != user){
            throw new BiddingUnauthorizedException("You cannot declare winner for this bid because the product does not belong to you");
        }

        List<Bid> biddingList = biddingRepository.findBidsByProductIdOrderByBidAmountDesc(productId);

        Bid winningBid = biddingList.get(0);
        User winner = winningBid.getBidder();

        winningBid.setIsWinningBid(true);
        product.setWinningBidder(winner);
        product.setBiddingClosed(true);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SS", Locale.ENGLISH);
        product.setBiddingTimeLimit(dateFormat.format(new Date()));
        productRepository.save(product);
        biddingRepository.save(winningBid);

        CreateOrderDto orderDto = new CreateOrderDto();
        orderDto.setProductId(product.getId());
        Long totalPrice = winningBid.getBidAmount();
        orderDto.setTotalPrice(totalPrice);
        orderDto.setCompanyPercentage(3.0);
        orderDto.setVendorId(product.getVendor().getId());
        orderDto.setCustomerId(winningBid.getBidder().getId());
        Double companyCommission = 0.03 * totalPrice;
        orderDto.setCompanyCommission(companyCommission);

        orderService.createOrder(orderDto);

        notificationService.sendNotification("🎉 Congratulations! You have won " + product.getName() + ", Do well to pay for your order and it will be shipped to you", winner.getId());

        List<Bid> losingBids = biddingRepository.findByProductIdAndIsWinningBidFalseAndBidderNot(productId, winner);

        for (Bid bid : losingBids) {
            notificationService.sendNotification(
                    "You lost the bid on " + product.getName() + " to " + winner.getUsername(),
                    bid.getBidder().getId()
            );
        }

        return winner.getUsername() + " has be declared the winner of " + product.getName();
    }

    @Override
    public List<UserBiddingListDto> getUserBids(Long userId, String keyword, String createdAtStart, String createdAtEnd, String sortBy) {
        List<Bid> userBids = biddingRepository.findByBidderId(userId);

        if (keyword != null && !keyword.isEmpty()) {
            userBids = userBids.stream()
                    .filter(bid -> bid.getProduct().getName().toLowerCase().contains(keyword.toLowerCase()))
                    .collect(Collectors.toList());
        }


        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);

        if (createdAtStart != null && createdAtEnd != null) {
            try {
                Date startDate = dateFormat.parse(createdAtStart);
                Date endDate = dateFormat.parse(createdAtEnd);
                userBids = userBids.stream()
                        .filter(bid -> bid.getCreatedAt().after(startDate) && bid.getCreatedAt().before(endDate))
                        .collect(Collectors.toList());
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        switch (sortBy) {
            case "Date Ascending":
                userBids.sort(Comparator.comparing(Bid::getCreatedAt));
                break;
            case "Date Descending":
                userBids.sort(Comparator.comparing(Bid::getCreatedAt).reversed());
                break;
            case "Bid Amount Ascending":
                userBids.sort(Comparator.comparing(Bid::getBidAmount));
                break;
            case "Bid Amount Descending":
                userBids.sort(Comparator.comparing(Bid::getBidAmount).reversed());
                break;
        }

        return userBids.stream()
                .map(bid -> modelMapper.map(bid, UserBiddingListDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<AdminBidListDto> getAllBidsForAdmin(String keyword, String createdAtStart, String createdAtEnd, String sortBy) {
        List<Bid> allBids = biddingRepository.findAll();

        if (keyword != null && !keyword.isEmpty()) {
            allBids = allBids.stream()
                    .filter(bid -> bid.getProduct().getName().toLowerCase().contains(keyword.toLowerCase()) ||
                            bid.getBidder().getUsername().toLowerCase().contains(keyword.toLowerCase()))
                    .collect(Collectors.toList());
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);

        if (createdAtStart != null && createdAtEnd != null) {
            try {
                Date startDate = dateFormat.parse(createdAtStart);
                Date endDate = dateFormat.parse(createdAtEnd);
                allBids = allBids.stream()
                        .filter(bid -> bid.getCreatedAt().after(startDate) && bid.getCreatedAt().before(endDate))
                        .collect(Collectors.toList());
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        switch (sortBy) {
            case "Date Ascending":
                allBids.sort(Comparator.comparing(Bid::getCreatedAt));
                break;
            case "Date Descending":
                allBids.sort(Comparator.comparing(Bid::getCreatedAt).reversed());
                break;
            case "Bid Amount Ascending":
                allBids.sort(Comparator.comparing(Bid::getBidAmount));
                break;
            case "Bid Amount Descending":
                allBids.sort(Comparator.comparing(Bid::getBidAmount).reversed());
                break;
        }

        return allBids.stream()
                .map(bid -> modelMapper.map(bid, AdminBidListDto.class))
                .collect(Collectors.toList());
    }
}
