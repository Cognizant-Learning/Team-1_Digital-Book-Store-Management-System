package com.libraryManagement.project.service.impl;

import com.libraryManagement.project.dto.requestDTO.BuyNowRequestDTO;
import com.libraryManagement.project.dto.requestDTO.OrderRequestDTO;
import com.libraryManagement.project.dto.responseDTO.OrderItemResponseDTO;
import com.libraryManagement.project.dto.responseDTO.OrderResponseDTO;
import com.libraryManagement.project.entity.*;
import com.libraryManagement.project.enums.OrderStatus;
import com.libraryManagement.project.exception.InvalidInputException;
import com.libraryManagement.project.exception.ResourceNotFoundException;
import com.libraryManagement.project.repository.*;
import com.libraryManagement.project.service.OrderService;
import com.libraryManagement.project.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.InvalidAlgorithmParameterException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final CartItemsRepository cartItemsRepository;
    private final OrdersRepository ordersRepository;
    private final OrderItemsRepository orderItemsRepository;
    private final ShippingAddressRepository shippingAddressRepository;
    private final BookRepository bookRepository;
    private final InventoryRepository inventoryRepository;
    private final ReviewRepository reviewRepository;

    @Autowired
    public OrderServiceImpl(UserRepository userRepository, CartRepository cartRepository, CartItemsRepository cartItemsRepository, OrdersRepository ordersRepository, OrderItemsRepository orderItemsRepository, ShippingAddressRepository shippingAddressRepository, BookRepository bookRepository, InventoryRepository inventoryRepository, ReviewRepository reviewRepository) {
        this.userRepository = userRepository;
        this.cartRepository = cartRepository;
        this.cartItemsRepository = cartItemsRepository;
        this.ordersRepository = ordersRepository;
        this.orderItemsRepository = orderItemsRepository;
        this.shippingAddressRepository = shippingAddressRepository;
        this.bookRepository = bookRepository;
        this.inventoryRepository = inventoryRepository;
        this.reviewRepository = reviewRepository;
    }

    @Override
    public OrderResponseDTO placeOrder(OrderRequestDTO orderRequestDTO){

        //Fetch User, Cart, ShippingAddress Object from database using their IDs

        User user = userRepository.findById(Objects.requireNonNull(SecurityUtil.getCurrentUserId()))
                .orElseThrow(() -> new ResourceNotFoundException("User does not exist"));

        Cart cart = cartRepository.findById(orderRequestDTO.getCartId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart does not exist"));


        ShippingAddress address = shippingAddressRepository.findById(orderRequestDTO.getAddressId())
                .orElseThrow(() -> new ResourceNotFoundException("Address does not exist"));

        List<CartItems> cartItems = cartItemsRepository.findCartItemsByCartId(cart.getCartId());

        if(cartItems.isEmpty()){
            throw new ResourceNotFoundException("Cart is empty");
        }

        //Creating Order object
        Order order = new Order();
        order.setUser(user);
        order.setStatus(OrderStatus.valueOf(orderRequestDTO.getStatus()));        order.setPaymentId(STR."id_pay\{UUID.randomUUID()}");
        order.setAddress(address);


        double totalAmount;
        List<OrderItems> orderItems;

        //Calculating total cost for all items
        totalAmount = cartItems.stream()
                .mapToDouble(cartItem -> cartItem.getBook().getPrice() * cartItem.getQuantity())
                .sum();

        //calculate gst
        totalAmount = totalAmount + (totalAmount * 0.18);

        //Mapping cartItems to orderItems
        orderItems = cartItems.stream()
                .map(cartItem -> {
                    OrderItems orderItem = new OrderItems();
                    orderItem.setBook(cartItem.getBook());
                    orderItem.setQuantity(cartItem.getQuantity());
                    orderItem.setUnitPrice(cartItem.getBook().getPrice());
                    orderItem.setOrder(order);
                    return orderItem;
                })
                .collect(Collectors.toList());


        //Updating inventory
        orderItems.forEach(orderItem -> {
            Book book = orderItem.getBook();
            int orderQuantity = orderItem.getQuantity();

            Inventory inventory = inventoryRepository.findByBook(book)
                    .orElseThrow(() -> new ResourceNotFoundException("No inventory present"));

            int stockQuantity = inventory.getStockQuantity();
            int updatedQuantity = stockQuantity - orderQuantity;

            if(updatedQuantity < 0){
                throw new ResourceNotFoundException("No stock left for book with id" + book.getBookId());
            }

            inventory.setStockQuantity(updatedQuantity);

            inventoryRepository.save(inventory);
        });

        order.setTotalAmount(totalAmount);
        order.setOrderItems(orderItems);


        //save order and order items
        ordersRepository.save(order);
        orderItemsRepository.saveAll(orderItems);

        //remove all the cart items
        cartItemsRepository.deleteAll(cartItems);
        cartRepository.delete(cart);

        return mapToOrderResponseDTO(order,orderItems);



    }

    @Override
    public OrderResponseDTO buyNow(BuyNowRequestDTO buyNowRequestDTO){

        //Fetch User, Cart, ShippingAddress Object from database using their IDs

        User user = userRepository.findById(SecurityUtil.getCurrentUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User does not exist"));

        ShippingAddress address = shippingAddressRepository.findById(buyNowRequestDTO.getAddressId())
                .orElseThrow(() -> new ResourceNotFoundException("Address does not exist"));

        Book book = bookRepository.findById(buyNowRequestDTO.getBookId())
                .orElseThrow(() -> new ResourceNotFoundException("Book does not exist"));

        int quantity = buyNowRequestDTO.getQuantity();
        double price = book.getPrice();

        if(quantity <= 0){
            throw new InvalidInputException("Quantity can't be less than 1");
        }

        //Creating orderItem object
        OrderItems orderItem = new OrderItems();
        orderItem.setBook(book);
        orderItem.setQuantity(quantity);
        orderItem.setUnitPrice(price);

        //updating inventory
        Inventory inventory = inventoryRepository.findByBook(book)
                .orElseThrow(() -> new ResourceNotFoundException("No inventory present"));

        int stockQuantity = inventory.getStockQuantity();
        int updatedQuantity = stockQuantity - quantity;

        if(updatedQuantity < 0){
            throw new ResourceNotFoundException("No stock left for this book");
        }

        inventory.setStockQuantity(updatedQuantity);

        inventoryRepository.save(inventory);

        //Creating Order Object
        Order order = new Order();
        order.setUser(user);
        order.setStatus(OrderStatus.PLACED);
        order.setPaymentId(STR."id_pay\{UUID.randomUUID()}");
        order.setAddress(address);
        order.setTotalAmount(price * quantity);

        List<OrderItems> orderItems = new ArrayList<>();

        orderItem.setOrder(order);
        orderItems.add(orderItem);

        order.setOrderItems(orderItems);


        ordersRepository.save(order);
        orderItemsRepository.saveAll(orderItems);

        return mapToOrderResponseDTO(order,orderItems);



    }

    private OrderResponseDTO mapToOrderResponseDTO(Order order, List<OrderItems> orderItems) {

        //Mapping orderItems to OrderItemResponseDTO
        List<OrderItemResponseDTO> itemDTOs = orderItems.stream().map(item -> {
            OrderItemResponseDTO dto = new OrderItemResponseDTO();
            dto.setOrderItemId(item.getItemId());
            dto.setItemId(item.getBook().getBookId());
            dto.setItemName(item.getBook().getTitle());
            dto.setQuantity(item.getQuantity());
            dto.setPrice(item.getUnitPrice());
            return dto;
        }).collect(Collectors.toList());

        //Mapping Order to OrderResponseDTO
        OrderResponseDTO response = new OrderResponseDTO();
        response.setOrderId(order.getOrderId());
        response.setUserId(order.getUser().getUserId());
        response.setAddressId(order.getAddress().getAddressId());
        response.setTotalAmount(order.getTotalAmount());
        response.setPlacedAt(LocalDateTime.now());
        response.setItems(itemDTOs);

        return response;


    }

    @Override
    public List<Order> getAllOrders() {
        return ordersRepository.findAll();
    }

    @Override
    public List<OrderItemResponseDTO> getOrdersByUserId(Long userId) {
        // 1. Fetch all OrderItems entities for the user.
        List<OrderItems> orderItems = orderItemsRepository.findAllByUserId(userId);

        return orderItems.stream()
                .map(this::mapToOrderItemResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * ✨ FIX: This is the correct helper method.
     * It takes an 'OrderItems' object and returns the 'OrderItemResponseDTO'.
     */
    private OrderItemResponseDTO mapToOrderItemResponseDTO(OrderItems item) {
        boolean hasReviewed = reviewRepository.existsByUserAndBook(item.getOrder().getUser(), item.getBook());
        return new OrderItemResponseDTO(
                item.getItemId(),     // The ID of the order item line
                item.getBook().getBookId(), // The ID of the book
                item.getBook().getTitle(),  // The name of the book
                item.getQuantity(),
                item.getUnitPrice(),
                hasReviewed
        );
    }
}
