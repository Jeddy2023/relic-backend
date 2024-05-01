package com.relic.relic.service.implementations;

import com.relic.relic.dto.ApplicationReviewDto;
import com.relic.relic.dto.ProductDto;
import com.relic.relic.dto.ProductReveiwDto;
import com.relic.relic.dto.ReviewDto;
import com.relic.relic.enums.ReviewType;
import com.relic.relic.exception.BiddingUnauthorizedException;
import com.relic.relic.exception.CustomException;
import com.relic.relic.exception.ResourceNotFoundException;
import com.relic.relic.model.Product;
import com.relic.relic.model.Review;
import com.relic.relic.model.User;
import com.relic.relic.repository.CategoryRepository;
import com.relic.relic.repository.ProductRepository;
import com.relic.relic.repository.ReviewRepository;
import com.relic.relic.repository.UserRepository;
import com.relic.relic.service.ReviewService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@AllArgsConstructor
public class ReviewServiceImple implements ReviewService {
    private ProductRepository productRepository;
    private ReviewRepository reviewRepository;
    private ModelMapper modelMapper;
    private UserRepository userRepository;

    @Override
    public String giveproductReview(ProductReveiwDto productReviewDTO, Long productId) {
        Optional<Product> productOptional = productRepository.findById(productId);
        if (productOptional.isEmpty()) {
            throw new ResourceNotFoundException("Product not found");
        }

        Product product = productOptional.get();

        if(productReviewDTO.getComment().isEmpty() || productReviewDTO.getRating() <= 0){
            throw new CustomException("All fields are required");
        }

        User user = getLoggedInUser();
        if (user == null) {
            throw new BiddingUnauthorizedException("Login to access this resource");
        }

        Review reviews = new Review();
        reviews.setReviewType(ReviewType.PRODUCT_REVIEW);
        reviews.setComment(productReviewDTO.getComment());
        reviews.setRating(productReviewDTO.getRating());
        reviews.setUser(user);
        reviews.setProduct(product);
        reviewRepository.save(reviews);
        return "Review sent successfully";
    }

    @Override
    public String giveApplicationReview(ApplicationReviewDto applicationReviewDTO) {
        if(applicationReviewDTO.getComment() == null || applicationReviewDTO.getComment().isEmpty() || applicationReviewDTO.getRating() <= 0){
            throw new CustomException("All fields are required");
        }

        User user = getLoggedInUser();
        if (user == null) {
            throw new BiddingUnauthorizedException("Login to access this resource");
        }

        Review reviews = new Review();
        reviews.setReviewType(ReviewType.APPLICATION_REVIEW);
        reviews.setComment(applicationReviewDTO.getComment());
        reviews.setRating(applicationReviewDTO.getRating());
        reviews.setUser(user);
        reviewRepository.save(reviews);
        return "Review sent successfully";
    }

    @Override
    public List<ReviewDto> getAllReviews() {
        List<Review> reviews = reviewRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
        return mapReviewListToDto(reviews);
    }

    @Override
    public List<ReviewDto> getAllProductReviews() {
        List<Review> reviews = reviewRepository.findAllByReviewType(ReviewType.PRODUCT_REVIEW);
        return mapReviewListToDto(reviews);
    }

    @Override
    public List<ReviewDto> getAllApplicationReviews() {
        List<Review> reviews = reviewRepository.findAllByReviewType(ReviewType.APPLICATION_REVIEW);
        return mapReviewListToDto(reviews);
    }

    @Override
    public List<ReviewDto> getAllReviewForAParticularProduct(Long productId) {
        Optional<Product> productOptional = productRepository.findById(productId);
        if (productOptional.isEmpty()) {
            throw new ResourceNotFoundException("Product not found");
        }

        Product product = productOptional.get();

        List<Review> reviews = reviewRepository.findAllByProduct(product);
        return mapReviewListToDto(reviews);
    }

    private User getLoggedInUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BiddingUnauthorizedException("Login to access this resource");
        }
        String name = authentication.getName();
        return userRepository.findByUsernameOrEmail(name, name)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private List<ReviewDto> mapReviewListToDto(List<Review> reviews) {
        if (reviews.isEmpty()) {
            return Collections.emptyList();
        }
        return reviews.stream()
                .map(review -> modelMapper.map(review, ReviewDto.class))
                .collect(Collectors.toList());
    }
}
