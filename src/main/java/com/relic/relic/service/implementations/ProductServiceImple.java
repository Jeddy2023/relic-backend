package com.relic.relic.service.implementations;

import com.relic.relic.dto.*;
import com.relic.relic.enums.Condition;
import com.relic.relic.enums.Role;
import com.relic.relic.exception.BiddingUnauthorizedException;
import com.relic.relic.exception.CustomException;
import com.relic.relic.exception.MediaUploadException;
import com.relic.relic.exception.ResourceNotFoundException;
import com.relic.relic.model.*;
import com.relic.relic.repository.BiddingRepository;
import com.relic.relic.repository.CategoryRepository;
import com.relic.relic.repository.ProductRepository;
import com.relic.relic.repository.UserRepository;
import com.relic.relic.service.FileUploadService;
import com.relic.relic.service.NotificationService;
import com.relic.relic.service.ProductService;
import com.relic.relic.util.UserAuthenticationHelper;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@AllArgsConstructor
public class ProductServiceImple implements ProductService {

    private ModelMapper modelMapper;
    private ProductRepository productRepository;
    private CategoryRepository categoryRepository;
    private UserRepository userRepository;
    private final NotificationService notificationService;
    private BiddingRepository biddingRepository;
    private FileUploadService fileUploadService;
    private UserAuthenticationHelper userAuthenticationHelper;

    @Override
    public String createProduct(CreateProductDto createProductDto) {
        if (createProductDto.getName() == null || createProductDto.getName().isEmpty() ||
                createProductDto.getShortDescription() == null || createProductDto.getShortDescription().isEmpty() ||
                createProductDto.getDescription() == null || createProductDto.getDescription().isEmpty() ||
                createProductDto.getCondition() == null ||
                createProductDto.getMinimumBid() <= 0 ||
                createProductDto.getBiddingTimeLimit() == null ||
                createProductDto.getProductImage() == null) {
            throw new CustomException("All fields are required");
        }

        try {
            Condition.valueOf(createProductDto.getCondition());
        } catch (IllegalArgumentException e) {
            throw new CustomException("Invalid condition provided");
        }

        String uploadedImageUrl;
        try {
            uploadedImageUrl = fileUploadService.uploadImage(createProductDto.getProductImage());
        } catch (IOException e) {
            throw new MediaUploadException("An error occurred while uploading image");
        }

        User user = userAuthenticationHelper.getLoggedInUser();
        if (user == null) {
            throw new BiddingUnauthorizedException("Login to access this resource");
        }

        if(!user.isBlocked()){
            throw new CustomException("You are not authorised to access this resource you are blocked");
        }

        if(!user.isVerified()){
            throw new CustomException("Please verify your email to access this");
        }

        if(user.getRole() != Role.SELLER){
            throw new BiddingUnauthorizedException("You have to be a Vendor to access this resource");
        }

        Optional<Category> category = categoryRepository.findById(createProductDto.getCategoryId());
        if (!category.isPresent()) {
            throw new ResourceNotFoundException("Category not found");
        }

        Product newProduct = new Product();
        newProduct.setVendor(user);
        newProduct.setName(createProductDto.getName());
        newProduct.setShortDescription(createProductDto.getShortDescription());
        newProduct.setDescription(createProductDto.getDescription());
        newProduct.setCondition(Condition.valueOf(createProductDto.getCondition()));
        newProduct.setMinimumBid(createProductDto.getMinimumBid());
        newProduct.setCurrentBid(createProductDto.getMinimumBid());
        newProduct.setCategory(category.get());
        newProduct.setBiddingTimeLimit(createProductDto.getBiddingTimeLimit());
        newProduct.setProductImageUrl(uploadedImageUrl);

        productRepository.save(newProduct);
        return "Product created succesfully";
    }

    @Override
    public TotalProductsCountDto getTotalProductCounts() {
        User user = userAuthenticationHelper.getLoggedInUser();
        List<Product> products = productRepository.findByVendorId(user.getId());

        long totalPendingReviews = products.stream()
                .filter(product -> product.getReviews() == null || product.getReviews().isEmpty())
                .count();

        long totalActiveProducts = products.stream()
                .filter(product -> !product.isBiddingClosed())
                .count();

        long totalOfflineProducts = products.stream()
                .filter(Product::isBiddingClosed)
                .count();

        long totalUserProducts = products.size();

        TotalProductsCountDto countsDto = new TotalProductsCountDto();
        countsDto.setTotalPendingReviews(totalPendingReviews);
        countsDto.setTotalActiveProducts(totalActiveProducts);
        countsDto.setTotalOfflineProducts(totalOfflineProducts);
        countsDto.setTotalUserProducts(totalUserProducts);

        return countsDto;
    }

    @Override
    public List<UserProductDto> getUserProducts(
            boolean pendingReviews,
            boolean activeProducts,
            boolean offlineBid,
            String keyword,
            Long userId
    ) {
        User user;
        if(userId == null){
            user = userAuthenticationHelper.getLoggedInUser();
        } else {
            Optional<User> userForProduct = userRepository.findById(userId);
            if(userForProduct.isEmpty()) {
                throw new CustomException("User not found");
            }
            user = userForProduct.get();
        }

        List<Product> products = productRepository.findByVendorId(user.getId());

        if (pendingReviews) {
            products = products.stream()
                    .filter(product -> product.getReviews() == null || product.getReviews().isEmpty())
                    .collect(Collectors.toList());
        }
        if (keyword != null) {
            String lowerCaseKeyword = keyword.toLowerCase();
            products = products.stream()
                    .filter(product -> product.getName().toLowerCase().contains(lowerCaseKeyword))
                    .collect(Collectors.toList());
        }
        if (activeProducts) {
            products = products.stream()
                    .filter(product -> !product.isBiddingClosed())
                    .collect(Collectors.toList());
        }
        if (offlineBid) {
            products = products.stream()
                    .filter(Product::isBiddingClosed)
                    .collect(Collectors.toList());
        }

        products.sort(Comparator.comparing(Product::getUpdatedAt).reversed());

        return products.stream()
                .map(product -> {
                    UserProductDto userProductDto = modelMapper.map(product, UserProductDto.class);
                    userProductDto.setTotalReviews(product.getReviews() != null ? product.getReviews().size() : 0);
                    userProductDto.setTotalBids(product.getBidList() != null ? product.getBidList().size() : 0);
                    return userProductDto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductDto> getAllProducts(
            boolean totalWonProducts,
            boolean totalActiveAuctions,
            Long categoryId,
            Condition condition,
            String keyword,
            Long minBid,
            Long maxBid,
            Double minimumBid,
            Integer rating,
            String sortBy
    ) {
        List<Product> products;

        if (totalWonProducts) {
            User user = userAuthenticationHelper.getLoggedInUser();
            products = productRepository.findByWinningBidderId(user.getId());
        } else if (totalActiveAuctions) {
            User user = userAuthenticationHelper.getLoggedInUser();
            products = productRepository.findByVendorIdAndIsBiddingClosedFalse(user.getId());
        } else {
            products = productRepository.findByIsBiddingClosedFalse();
        }

        if (categoryId != null) {
            products = products.stream()
                    .filter(product -> product.getCategory().getId().equals(categoryId))
                    .collect(Collectors.toList());
        }
        if (condition != null) {
            products = products.stream()
                    .filter(product -> product.getCondition() == condition)
                    .collect(Collectors.toList());
        }
        if (keyword != null) {
            String lowerCaseKeyword = keyword.toLowerCase();
            products = products.stream()
                    .filter(product -> product.getName().toLowerCase().contains(lowerCaseKeyword))
                    .collect(Collectors.toList());
        }
        if (minBid != null && maxBid != null) {
            products = products.stream()
                    .filter(product -> product.getCurrentBid() != null &&
                            product.getCurrentBid() >= minBid && product.getCurrentBid() <= maxBid)
                    .collect(Collectors.toList());
        }
        if (minimumBid != null) {
            products = products.stream()
                    .filter(product -> product.getMinimumBid() != null && product.getMinimumBid() >= minimumBid)
                    .collect(Collectors.toList());
        }
        if (rating != null) {
            products = products.stream()
                    .filter(product -> product.getAverageRating() != null && product.getAverageRating().intValue() == rating)
                    .collect(Collectors.toList());
        }

        switch (sortBy) {
            case "Average Rating":
                products = products.stream()
                        .sorted((p1, p2) -> Double.compare(
                                p2.getAverageRating() != null ? p2.getAverageRating() : 0,
                                p1.getAverageRating() != null ? p1.getAverageRating() : 0))
                        .collect(Collectors.toList());
                break;
            case "Sort by current bid: low to high":
                products = products.stream()
                        .sorted(Comparator.comparingLong(p -> p.getCurrentBid() != null ? p.getCurrentBid() : 0))
                        .collect(Collectors.toList());
                break;
            case "Sort by current bid: high to low":
                products = products.stream()
                        .sorted((p1, p2) -> Long.compare(
                                p2.getCurrentBid() != null ? p2.getCurrentBid() : 0,
                                p1.getCurrentBid() != null ? p1.getCurrentBid() : 0))
                        .collect(Collectors.toList());
                break;
            case "Sort auction by ending soonest":
                products = products.stream()
                        .sorted(Comparator.comparing(Product::getBiddingTimeLimit))
                        .collect(Collectors.toList());
                break;
            case "Sort auction by recently started":
                products = products.stream()
                        .sorted((p1, p2) -> p2.getCreatedAt().compareTo(p1.getCreatedAt()))
                        .collect(Collectors.toList());
                break;
            case "Sort auction by most active":
                products = products.stream()
                        .sorted((p1, p2) -> Integer.compare(p2.getNumberOfBids(), p1.getNumberOfBids()))
                        .collect(Collectors.toList());
                break;
        }

        return products.stream()
                .map(product -> modelMapper.map(product, ProductDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<AdminProductDto> getAllProductsForAdmin(
            Long categoryId,
            Condition condition,
            String keyword,
            Boolean isBiddingClosed,
            Boolean isBlocked,
            String biddingTimeLimitStart,
            String biddingTimeLimitEnd,
            Long minBid,
            Long maxBid,
            String sortBy
    ) {
        List<Product> products = productRepository.findAll();

        if (categoryId != null) {
            products = products.stream()
                    .filter(product -> product.getCategory().getId().equals(categoryId))
                    .collect(Collectors.toList());
        }

        if (condition != null) {
            products = products.stream()
                    .filter(product -> product.getCondition() == condition)
                    .collect(Collectors.toList());
        }

        if (keyword != null) {
            String lowerCaseKeyword = keyword.toLowerCase();
            products = products.stream()
                    .filter(product -> product.getName().toLowerCase().contains(lowerCaseKeyword) ||
                            product.getShortDescription().toLowerCase().contains(lowerCaseKeyword) ||
                            product.getVendor().getShopname().toLowerCase().contains(lowerCaseKeyword))
                    .collect(Collectors.toList());
        }

        if (isBiddingClosed != null) {
            products = products.stream()
                    .filter(product -> product.isBiddingClosed() == isBiddingClosed)
                    .collect(Collectors.toList());
        }

        if (isBlocked != null) {
            products = products.stream()
                    .filter(product -> product.isBlocked() == isBlocked)
                    .collect(Collectors.toList());
        }

        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SS", Locale.ENGLISH);
            if (biddingTimeLimitStart != null && biddingTimeLimitEnd != null) {
                Date startDate = dateFormat.parse(biddingTimeLimitStart);
                Date endDate = dateFormat.parse(biddingTimeLimitEnd);
                products = products.stream()
                        .filter(product -> {
                            try {
                                Date biddingTimeLimit = dateFormat.parse(product.getBiddingTimeLimit());
                                return biddingTimeLimit.after(startDate) && biddingTimeLimit.before(endDate);
                            } catch (ParseException e) {
                                e.printStackTrace();
                                return false;
                            }
                        })
                        .collect(Collectors.toList());
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (minBid != null && maxBid != null) {
            products = products.stream()
                    .filter(product -> product.getCurrentBid() != null &&
                            product.getCurrentBid() >= minBid && product.getCurrentBid() <= maxBid)
                    .collect(Collectors.toList());
        }

        switch (sortBy) {
            case "Most Active":
                products = products.stream()
                        .sorted((p1, p2) -> Integer.compare(p2.getNumberOfBids(), p1.getNumberOfBids()))
                        .collect(Collectors.toList());
                break;
            case "Recently Started":
                products = products.stream()
                        .sorted(Comparator.comparing(Product::getCreatedAt).reversed())
                        .collect(Collectors.toList());
                break;
            case "Current Bid: Low to High":
                products = products.stream()
                        .sorted(Comparator.comparingLong(p -> p.getCurrentBid() != null ? p.getCurrentBid() : 0))
                        .collect(Collectors.toList());
                break;
            case "Current Bid: High to Low":
                products = products.stream()
                        .sorted((p1, p2) -> Long.compare(p2.getCurrentBid() != null ? p2.getCurrentBid() : 0,
                                p1.getCurrentBid() != null ? p1.getCurrentBid() : 0))
                        .collect(Collectors.toList());
                break;
            case "Ending Soonest":
                products = products.stream()
                        .sorted(Comparator.comparing(Product::getBiddingTimeLimit))
                        .collect(Collectors.toList());
                break;
        }

        return products.stream()
                .map(product -> modelMapper.map(product, AdminProductDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public ProductDto getProductById(Long id) {
        Optional<Product> product = productRepository.findById(id);
        if (product.isEmpty()) throw new ResourceNotFoundException("Product not found");
        return modelMapper.map(product.get(), ProductDto.class);
    }
    @Override
    public String deleteProduct(Long id) {
        Optional<Product> productOptional = productRepository.findById(id);
        if (productOptional.isEmpty()) {
            throw new ResourceNotFoundException("Product not found");
        }
        Product product = productOptional.get();
        User user = userAuthenticationHelper.getLoggedInUser();

        if(user != product.getVendor() && user.getRole() != Role.ADMIN){
            throw new BiddingUnauthorizedException("This product does not belong to you, you cannot delete it");
        }

        List<User> bidders = biddingRepository.findDistinctBiddersByProductId(product.getId());
        if (bidders.size() > 0) {
            bidders.forEach(bidder -> notificationService.sendNotification(
                    product.getName() + "has been deleted ", bidder.getId()));
        }


        productRepository.delete(product);
        return "Product deleted succesfully";
    }
    @Override
    public String updateProduct(Long id, CreateProductDto createProductDto) {
        Optional<Product> productOptional = productRepository.findById(id);
        if (productOptional.isEmpty()) {
            throw new ResourceNotFoundException("Product not found");
        }

        Product product = productOptional.get();

        if (createProductDto.getName() == null || createProductDto.getName().isEmpty() ||
                createProductDto.getShortDescription() == null || createProductDto.getShortDescription().isEmpty() ||
                createProductDto.getDescription() == null || createProductDto.getDescription().isEmpty() ||
                createProductDto.getCondition() == null ||
                createProductDto.getMinimumBid() <= 0 ||
                createProductDto.getBiddingTimeLimit() == null){
            throw new CustomException("All fields are required");
        }

        try {
            Condition.valueOf(createProductDto.getCondition());
        } catch (IllegalArgumentException e) {
            throw new CustomException("Invalid condition provided");
        }

        User user = userAuthenticationHelper.getLoggedInUser();
        if (user == null) {
            throw new BiddingUnauthorizedException("Login to access this resource");
        }

        if(user != product.getVendor()){
            throw new BiddingUnauthorizedException("This product does not belong to you, you cannot update it");
        }

        Optional<Category> category = categoryRepository.findById(createProductDto.getCategoryId());
        if (!category.isPresent()) {
            throw new ResourceNotFoundException("Category not found");
        }

        String uploadedImageUrl;

        if(createProductDto.getProductImage() != null){
            try {
                uploadedImageUrl = fileUploadService.uploadImage(createProductDto.getProductImage());
            } catch (IOException e) {
                throw new MediaUploadException("An error occurred while uploading image");
            }
        }else{
            uploadedImageUrl = product.getProductImageUrl();
        }

        product.setName(createProductDto.getName());
        product.setShortDescription(createProductDto.getShortDescription());
        product.setDescription(createProductDto.getDescription());
        product.setCondition(Condition.valueOf(createProductDto.getCondition()));
        product.setMinimumBid(createProductDto.getMinimumBid());
        product.setCategory(category.get());
        product.setBiddingTimeLimit(createProductDto.getBiddingTimeLimit());
        product.setProductImageUrl(uploadedImageUrl);

        productRepository.save(product);

        return "Product updated successfully";
    }

    @Scheduled(fixedRate = 1000)
    public void checkBiddingTimeLimits(){
        List<Product> products = productRepository.findAll();

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        for (Product product : products) {
            try{
                Date biddingTimeLimit = dateFormat.parse(product.getBiddingTimeLimit());

                if (!product.isApproachingNotificationSent() && !product.isBiddingClosed() && isApproachingBiddingTimeLimit(biddingTimeLimit) && !product.getBidList().isEmpty()) {
                    sendBiddingEndingNotification(product);
                    product.setApproachingNotificationSent(true);
                }

                if (product.getBiddingTimeLimit() != null && new Date().after(biddingTimeLimit) && !product.isBiddingClosed()) {
                    if (!product.isEndingNotificationSent()) {
                        product.setEndingNotificationSent(true);
                        if (product.getBidList().isEmpty()) {
                            notificationService.sendNotification("Bidding time limit for " + product.getName() + " has reached and there was no bid on this product. Please update the bidding time limit.", product.getVendor().getId());
                        } else {
                            notificationService.sendNotification("Bidding time limit for " + product.getName() + " has reached. Please award your winner so they can proceed to pay.", product.getVendor().getId());
                        }
                    }
                    product.setBiddingClosed(true);
                    productRepository.save(product);
                }

            } catch (ParseException e) {
                System.err.println("Error getting bidding time limit for product: " + product.getName());
                e.printStackTrace();
            }
        }
    }
    private boolean isApproachingBiddingTimeLimit(Date productDate) throws ParseException {
        Date currentTime = new Date();

        long timeDifferenceInMillis = productDate.getTime() - currentTime.getTime();

        long timeDifferenceInHours = timeDifferenceInMillis / (60 * 60 * 1000);

        return timeDifferenceInHours <= 24;
    }
    private void sendBiddingEndingNotification(Product product) {
        List<Bid> bidList = product.getBidList();

        for (Bid bid : bidList) {
            User bidder = bid.getBidder();
            boolean isWinningBidder = product.getWinningBidder() != null && product.getWinningBidder().equals(bidder);

            String notificationMessage;
            if (isWinningBidder) {
                notificationMessage = "Bidding for " + product.getName() + "will soon end and you are winning the bid.";
            } else {
                notificationMessage = "Bidding for " + product.getName() + "will soon end and you are losing the bid.";
            }

            notificationService.sendNotification(notificationMessage, bidder.getId());
        }
    }

    public String createProductWithUsername(CreateProductDto createProductDto) {
        if (createProductDto.getUsername() == null ||
                createProductDto.getName() == null || createProductDto.getName().isEmpty() ||
                createProductDto.getShortDescription() == null || createProductDto.getShortDescription().isEmpty() ||
                createProductDto.getDescription() == null || createProductDto.getDescription().isEmpty() ||
                createProductDto.getCondition() == null ||
                createProductDto.getMinimumBid() <= 0 ||
                createProductDto.getBiddingTimeLimit() == null ||
                createProductDto.getProductImage() == null) {
            throw new CustomException("All fields are required");
        }

        try {
            Condition.valueOf(createProductDto.getCondition());
        } catch (IllegalArgumentException e) {
            throw new CustomException("Invalid condition provided");
        }

        String uploadedImageUrl;
        try {
            uploadedImageUrl = fileUploadService.uploadImage(createProductDto.getProductImage());
        } catch (IOException e) {
            throw new MediaUploadException("An error occurred while uploading image");
        }

        Optional<User> vendor = userRepository.findByUsername(createProductDto.getUsername());
        if (vendor.isEmpty()) {
            throw new ResourceNotFoundException("Vendor not found");
        }

        if(vendor.get().getRole() != Role.SELLER){
            throw new BiddingUnauthorizedException("The specified user is not a seller");
        }

        Optional<Category> category = categoryRepository.findById(createProductDto.getCategoryId());
        if (!category.isPresent()) {
            throw new ResourceNotFoundException("Category not found");
        }

        Product newProduct = new Product();
        newProduct.setVendor(vendor.get());
        newProduct.setName(createProductDto.getName());
        newProduct.setShortDescription(createProductDto.getShortDescription());
        newProduct.setDescription(createProductDto.getDescription());
        newProduct.setCondition(Condition.valueOf(createProductDto.getCondition()));
        newProduct.setMinimumBid(createProductDto.getMinimumBid());
        newProduct.setCurrentBid(createProductDto.getMinimumBid());
        newProduct.setCategory(category.get());
        newProduct.setBiddingTimeLimit(createProductDto.getBiddingTimeLimit());
        newProduct.setProductImageUrl(uploadedImageUrl);

        productRepository.save(newProduct);
        return "Product created successfully";
    }

    public String blockProduct(Long productId) {
        Optional<Product> productOptional = productRepository.findById(productId);
        if (productOptional.isEmpty()) {
            throw new ResourceNotFoundException("Product not found");
        }
        Product product = productOptional.get();
        if(product.isBlocked()){
            throw new CustomException("Auction is already blocked");
        }
        product.setBlocked(true);
        productRepository.save(product);
        return "Product blocked successfully";
    }

    public String unblockProduct(Long productId) {
        Optional<Product> productOptional = productRepository.findById(productId);
        if (productOptional.isEmpty()) {
            throw new ResourceNotFoundException("Product not found");
        }
        Product product = productOptional.get();
        if(!product.isBlocked()){
            throw new CustomException("Auction is not blocked");
        }
        product.setBlocked(false);
        productRepository.save(product);
        return "Product unblocked successfully";
    }

    public String endProduct(Long productId) {
        Optional<Product> productOptional = productRepository.findById(productId);
        if (productOptional.isEmpty()) {
            throw new ResourceNotFoundException("Product not found");
        }
        Product product = productOptional.get();
        if(product.isBiddingClosed()){
            throw new CustomException("Auction is already closed down");
        }
        product.setBiddingClosed(true);
        productRepository.save(product);
        return "Product ended successfully";
    }

    public String resetWinningBidder(Long productId) {
        Optional<Product> productOptional = productRepository.findById(productId);
        if (productOptional.isEmpty()) {
            throw new ResourceNotFoundException("Product not found");
        }
        Product product = productOptional.get();
        product.setWinningBidder(null);
        productRepository.save(product);
        return "Winning bidder reset successfully";
    }

}
