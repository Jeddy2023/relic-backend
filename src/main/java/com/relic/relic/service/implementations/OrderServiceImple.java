package com.relic.relic.service.implementations;

import com.relic.relic.dto.*;
import com.relic.relic.enums.OrderStatus;
import com.relic.relic.exception.CustomException;
import com.relic.relic.exception.ResourceNotFoundException;
import com.relic.relic.model.Order;
import com.relic.relic.model.Product;
import com.relic.relic.model.User;
import com.relic.relic.repository.NotificationRepository;
import com.relic.relic.repository.OrderRepository;
import com.relic.relic.repository.ProductRepository;
import com.relic.relic.repository.UserRepository;
import com.relic.relic.service.OrderService;
import com.relic.relic.util.UserAuthenticationHelper;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@AllArgsConstructor
public class OrderServiceImple implements OrderService {
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final NotificationRepository notificationRepository;
    private final OrderRepository orderRepository;
    private UserAuthenticationHelper userAuthenticationHelper;
    private final ModelMapper modelMapper;

    @Override
    public void createOrder(CreateOrderDto createOrderDto) {
        Optional<Product> productOptional = productRepository.findById(createOrderDto.getProductId());
        if (productOptional.isEmpty()) {
            throw new ResourceNotFoundException("Product not found");
        }

        Optional<User> vendorOptional = userRepository.findById(createOrderDto.getVendorId());
        if (vendorOptional.isEmpty()) {
            throw new ResourceNotFoundException("Vendor not found");
        }

        Optional<User> customerOptional = userRepository.findById(createOrderDto.getCustomerId());
        if (customerOptional.isEmpty()) {
            throw new ResourceNotFoundException("Customer not found");
        }

        Order order = new Order();
        order.setCustomer(customerOptional.get());
        order.setProduct(productOptional.get());
        order.setVendor(vendorOptional.get());
        order.setTotalPrice(createOrderDto.getTotalPrice());
        order.setCompanyCommission(createOrderDto.getCompanyCommission());
        order.setCompanyPercentage(createOrderDto.getCompanyPercentage());
        orderRepository.save(order);
    }

    @Override
    public OrderDto getOrderById(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        return modelMapper.map(order, OrderDto.class);
    }

    @Override
    public String cancelOrder(CancelOrderDto cancelOrderDto) {
        Order order = orderRepository.findById(cancelOrderDto.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (!order.getStatus().equals(OrderStatus.PENDING)) {
            throw new CustomException("Cannot cancel order. Order status is not pending.");
        }

        order.setStatus(OrderStatus.CANCELLED);
        order.setCancelReason(cancelOrderDto.getCancelReason());

        orderRepository.save(order);

        return "Order cancelled successfully";
    }

    @Override
    public List<OrderDto> getAllOrders(
            OrderStatus status,
            Boolean vendorPaymentSent,
            Boolean paymentReceived,
            Double minCompanyCommission,
            Double maxCompanyCommission,
            Double minCompanyPercentage,
            Double maxCompanyPercentage,
            Double minTotalPrice,
            Double maxTotalPrice,
            String createdAtStart,
            String createdAtEnd,
            String sortBy
    ) {
        List<Order> orders = orderRepository.findAll();

        if (status != null) {
            orders = orders.stream()
                    .filter(order -> order.getStatus().equals(status))
                    .collect(Collectors.toList());
        }
        if (vendorPaymentSent != null) {
            orders = orders.stream()
                    .filter(order -> order.isVendorPaymentSent() == vendorPaymentSent)
                    .collect(Collectors.toList());
        }
        if (paymentReceived != null) {
            orders = orders.stream()
                    .filter(order -> order.isPaymentReceived() == paymentReceived)
                    .collect(Collectors.toList());
        }
        if (minCompanyCommission != null && maxCompanyCommission != null) {
            orders = orders.stream()
                    .filter(order -> order.getCompanyCommission() != null &&
                            order.getCompanyCommission() >= minCompanyCommission &&
                            order.getCompanyCommission() <= maxCompanyCommission)
                    .collect(Collectors.toList());
        }
        if (minCompanyPercentage != null && maxCompanyPercentage != null) {
            orders = orders.stream()
                    .filter(order -> order.getCompanyPercentage() != null &&
                            order.getCompanyPercentage() >= minCompanyPercentage &&
                            order.getCompanyPercentage() <= maxCompanyPercentage)
                    .collect(Collectors.toList());
        }
        if (minTotalPrice != null && maxTotalPrice != null) {
            orders = orders.stream()
                    .filter(order -> order.getTotalPrice() != null &&
                            order.getTotalPrice() >= minTotalPrice &&
                            order.getTotalPrice() <= maxTotalPrice)
                    .collect(Collectors.toList());
        }
        if (createdAtStart != null && createdAtEnd != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SS", Locale.ENGLISH);
            try {
                Date startDate = dateFormat.parse(createdAtStart);
                Date endDate = dateFormat.parse(createdAtEnd);
                orders = orders.stream()
                        .filter(order -> order.getCreatedAt().after(startDate) && order.getCreatedAt().before(endDate))
                        .collect(Collectors.toList());
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        switch (sortBy) {
            case "Total Price Ascending":
                orders.sort(Comparator.comparing(Order::getTotalPrice));
                break;
            case "Total Price Descending":
                orders.sort(Comparator.comparing(Order::getTotalPrice).reversed());
                break;
            case "Date Ascending":
                orders.sort(Comparator.comparing(Order::getCreatedAt));
                break;
            case "Date Descending":
                orders.sort(Comparator.comparing(Order::getCreatedAt).reversed());
                break;
        }

        return orders.stream()
                .map(order -> modelMapper.map(order, OrderDto.class))
                .collect(Collectors.toList());
    }

    public List<OrderUserDto> getUserOrders(
            OrderStatus status,
            Double minTotalPrice,
            Double maxTotalPrice,
            String createdAtStart,
            String createdAtEnd,
            String sortBy,
            Long userId,
            String keyword
    ) {
        User loggedInUser;
        if(userId != null){
            Optional<User> newLoggedInUser = userRepository.findById(userId);
            loggedInUser = newLoggedInUser.get();
        }
        else{
            loggedInUser = userAuthenticationHelper.getLoggedInUser();
        }

        List<Order> orders = orderRepository.findAll().stream()
                .filter(order -> order.getCustomer().equals(loggedInUser) || order.getVendor().equals(loggedInUser))
                .collect(Collectors.toList());

        if (keyword != null) {
            String lowerCaseKeyword = keyword.toLowerCase();
            orders = orders.stream()
                    .filter(order -> order.getProduct().getName().toLowerCase().contains(lowerCaseKeyword))
                    .collect(Collectors.toList());
        }

        if (status != null) {
            orders = orders.stream()
                    .filter(order -> order.getStatus().equals(status))
                    .collect(Collectors.toList());
        }
        if (minTotalPrice != null && maxTotalPrice != null) {
            orders = orders.stream()
                    .filter(order -> order.getTotalPrice() != null &&
                            order.getTotalPrice() >= minTotalPrice &&
                            order.getTotalPrice() <= maxTotalPrice)
                    .collect(Collectors.toList());
        }
        if (createdAtStart != null && createdAtEnd != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SS", Locale.ENGLISH);
            try {
                Date startDate = dateFormat.parse(createdAtStart);
                Date endDate = dateFormat.parse(createdAtEnd);
                orders = orders.stream()
                        .filter(order -> order.getCreatedAt().after(startDate) && order.getCreatedAt().before(endDate))
                        .collect(Collectors.toList());
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        switch (sortBy) {
            case "Total Price Ascending":
                orders.sort(Comparator.comparing(Order::getTotalPrice));
                break;
            case "Total Price Descending":
                orders.sort(Comparator.comparing(Order::getTotalPrice).reversed());
                break;
            case "Date Ascending":
                orders.sort(Comparator.comparing(Order::getCreatedAt));
                break;
            case "Date Descending":
                orders.sort(Comparator.comparing(Order::getCreatedAt).reversed());
                break;
        }

        return orders.stream()
                .map(order -> modelMapper.map(order, OrderUserDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public void markPaymentAsReceived(Long orderId) throws ResourceNotFoundException {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        order.setPaymentReceived(true);
        order.setStatus(OrderStatus.PAID);

        orderRepository.save(order);
    }

    @Override
    public void markOrderAsShipped(Long orderId) throws ResourceNotFoundException {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        order.setStatus(OrderStatus.SHIPPED);

        orderRepository.save(order);
    }

    @Override
    public void markOrderAsDelivered(Long orderId) throws ResourceNotFoundException {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        order.setStatus(OrderStatus.DELIVERED);

        orderRepository.save(order);
    }

    @Override
    public void markVendorPaymentSent(Long orderId) throws ResourceNotFoundException {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        order.setVendorPaymentSent(true);
        order.setStatus(OrderStatus.COMPLETED);

        orderRepository.save(order);
    }

    @Override
    public TotalOrdersCountDto getTotalOrderCounts() {
        List<Order> orders = orderRepository.findAll();

        long totalPendingOrders = countOrdersByStatus(orders, OrderStatus.PENDING);
        long totalCompletedOrders = countOrdersByStatus(orders, OrderStatus.COMPLETED);
        long totalCancelledOrders = countOrdersByStatus(orders, OrderStatus.CANCELLED);
        long totalOrders = orders.size();

        TotalOrdersCountDto countsDto = new TotalOrdersCountDto();
        countsDto.setTotalOrders(totalOrders);
        countsDto.setTotalPendingOrders(totalPendingOrders);
        countsDto.setTotalCompletedOrders(totalCompletedOrders);
        countsDto.setTotalCancelledOrders(totalCancelledOrders);

        return countsDto;
    }

    private long countOrdersByStatus(List<Order> orders, OrderStatus status) {
        return orders.stream()
                .filter(order -> order.getStatus().equals(status))
                .count();
    }
}
