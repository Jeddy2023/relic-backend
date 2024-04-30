package com.relic.relic.service.implementations;

import com.relic.relic.dto.CreateProductDto;
import com.relic.relic.dto.ProductDto;
import com.relic.relic.enums.Condition;
import com.relic.relic.exception.BiddingUnauthorizedException;
import com.relic.relic.exception.CustomException;
import com.relic.relic.exception.ResourceNotFoundException;
import com.relic.relic.model.Category;
import com.relic.relic.model.Product;
import com.relic.relic.model.Review;
import com.relic.relic.model.User;
import com.relic.relic.repository.CategoryRepository;
import com.relic.relic.repository.ProductRepository;
import com.relic.relic.repository.UserRepository;
import com.relic.relic.service.FileUploadService;
import com.relic.relic.service.ProductService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@AllArgsConstructor
public class ProductServiceImple implements ProductService {

    private ModelMapper modelMapper;
    private ProductRepository productRepository;
    private CategoryRepository categoryRepository;
    private UserRepository userRepository;
    private FileUploadService fileUploadService;

    @Override
    public String createProduct(CreateProductDto createProductDto) {
        if (createProductDto == null) {
            throw new CustomException("All fields are required");
        }

        if (createProductDto.getName() == null || createProductDto.getName().isEmpty() ||
                createProductDto.getShortDescription() == null || createProductDto.getShortDescription().isEmpty() ||
                createProductDto.getDescription() == null || createProductDto.getDescription().isEmpty() ||
                createProductDto.getCondition() == null ||
                createProductDto.getMinimumBid() <= 0 ||
                createProductDto.getBiddingTimeLimit() == null || createProductDto.getBiddingTimeLimit().getTime() <= 0 ){
            throw new CustomException("All fields are required");
        }

        try {
            Condition.valueOf(createProductDto.getCondition());
        } catch (IllegalArgumentException e) {
            throw new CustomException("Invalid condition provided");
        }


        User user = getLoggedInUser();
        if (user == null) {
            throw new BiddingUnauthorizedException("Login to access this resource");
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
        newProduct.setCategory(category.get());
        newProduct.setBiddingTimeLimit(createProductDto.getBiddingTimeLimit());

        productRepository.save(newProduct);
        return "Product created succesfully";
    }

    @Override
    public List<ProductDto> getAllProducts() {
        List<Product> allProducts = productRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
        return mapProductListToDto(allProducts);
    }

    @Override
    public List<ProductDto> findProductsByFilter() {
        return null;
    }

    @Override
    public List<ProductDto> getProductByCategory(Long categoryId) {
        List<Product> allProducts = productRepository.findProductsByCategoryId(categoryId);
        return mapProductListToDto(allProducts);
    }

    @Override
    public List<ProductDto> getProductByCondition(Condition condition) {
        List<Product> allProducts = productRepository.findProductsByCondition(condition);
        return mapProductListToDto(allProducts);
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

        if (createProductDto == null) {
            throw new CustomException("All fields are required");
        }

        if (createProductDto.getName() == null || createProductDto.getName().isEmpty() ||
                createProductDto.getShortDescription() == null || createProductDto.getShortDescription().isEmpty() ||
                createProductDto.getDescription() == null || createProductDto.getDescription().isEmpty() ||
                createProductDto.getCondition() == null ||
                createProductDto.getMinimumBid() <= 0 ||
                createProductDto.getBiddingTimeLimit() == null || createProductDto.getBiddingTimeLimit().getTime() <= 0 ){
            throw new CustomException("All fields are required");
        }

        try {
            Condition.valueOf(createProductDto.getCondition());
        } catch (IllegalArgumentException e) {
            throw new CustomException("Invalid condition provided");
        }

        User user = getLoggedInUser();
        if (user == null) {
            throw new BiddingUnauthorizedException("Login to access this resource");
        }

        Optional<Category> category = categoryRepository.findById(createProductDto.getCategoryId());
        if (!category.isPresent()) {
            throw new ResourceNotFoundException("Category not found");
        }

        product.setName(createProductDto.getName());
        product.setShortDescription(createProductDto.getShortDescription());
        product.setDescription(createProductDto.getDescription());
        product.setCondition(Condition.valueOf(createProductDto.getCondition()));
        product.setMinimumBid(createProductDto.getMinimumBid());
        product.setCategory(category.get());
        product.setBiddingTimeLimit(createProductDto.getBiddingTimeLimit());

        productRepository.save(product);

        return "Product updated successfully";
    }
    @Override
    public List<ProductDto> searchProducts(String productName) {
        List<Product> allProducts = productRepository.findByNameIsContainingIgnoreCase(productName);
        return mapProductListToDto(allProducts);
    }

    @Override
    public List<ProductDto> getProductsByCurrentBidRange(Long minBid, Long maxBid) {
        List<Product> allProducts = productRepository.findProductsByCurrentBidRange(minBid, maxBid);
        return mapProductListToDto(allProducts);
    }

    @Override
    public List<ProductDto> getProductsByMinimumBid(Double minimumBid) {
        List<Product> allProducts = productRepository.findProductsByMinimumBid(minimumBid);
        return mapProductListToDto(allProducts);
    }

    @Override
    public List<ProductDto> getProductsByRating(int rating) {
        List<Product> allProducts = productRepository.findProductsByReviewsRating(rating);
        return mapProductListToDto(allProducts);
    }

    @Override
    public List<ProductDto> getProductsSortedByAverageRating() {
        List<Product> products = productRepository.findAll();
        if (products.isEmpty()) {
            return Collections.emptyList();
        }
        return products.stream()
                .sorted(Comparator.comparingDouble(this::calculateAverageRating).reversed())
                .map(product -> modelMapper.map(product, ProductDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductDto> getProductsSortedByCurrentBidLowToHigh() {
        List<Product> allProducts = productRepository.findAll(Sort.by(Sort.Direction.ASC, "currentBid"));
        return mapProductListToDto(allProducts);
    }

    @Override
    public List<ProductDto> getProductsSortedByCurrentBidHighToLow() {
        List<Product> allProducts = productRepository.findAll(Sort.by(Sort.Direction.DESC, "currentBid"));
        return mapProductListToDto(allProducts);
    }

    @Override
    public List<ProductDto> getProductsSortedByAuctionEndingSoonest() {
        List<Product> allProducts = productRepository.findAll(Sort.by(Sort.Direction.ASC, "biddingTimeLimit"));
        return mapProductListToDto(allProducts);
    }

    @Override
    public List<ProductDto> getProductsSortedByRecentlyStarted() {
        List<Product> allProducts = productRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
        return mapProductListToDto(allProducts);
    }

    @Override
    public List<ProductDto> getProductsSortedByMostActive() {
        List<Product> products = productRepository.findAll();
        if (products.isEmpty()) {
            return Collections.emptyList();
        }
        return products.stream()
                .sorted(Comparator.comparingInt(Product::getNumberOfBids).reversed())
                .map(product -> modelMapper.map(product, ProductDto.class))
                .collect(Collectors.toList());
    }

    private List<ProductDto> mapProductListToDto(List<Product> products) {
        if (products.isEmpty()) {
            return Collections.emptyList();
        }
        return products.stream()
                .map(product -> modelMapper.map(product, ProductDto.class))
                .collect(Collectors.toList());
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
    private double calculateAverageRating(Product product) {
        return product.getReviews().stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);
    }

}
