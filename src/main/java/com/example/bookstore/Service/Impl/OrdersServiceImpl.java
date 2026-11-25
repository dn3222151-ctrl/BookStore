package com.example.bookstore.Service.Impl;

import com.example.bookstore.Entity.Books;
import com.example.bookstore.Entity.OrderDetails;
import com.example.bookstore.Entity.Orders;
import com.example.bookstore.Entity.User;
import com.example.bookstore.Repository.BooksRepository;
import com.example.bookstore.Repository.OrderDetailsRepository;
import com.example.bookstore.Repository.OrdersRepository;
import com.example.bookstore.Repository.UserRepository;
import com.example.bookstore.Service.OrdersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class OrdersServiceImpl implements OrdersService {

    @Autowired
    private OrdersRepository ordersRepository;

    @Autowired
    private OrderDetailsRepository orderDetailsRepository;

    @Autowired
    private BooksRepository booksRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public List<Orders> getAllOrders() {
        return ordersRepository.findAllByOrderByOrderDateDesc();
    }

    @Override
    public List<Orders> getOrdersByStatus(String status) {
        if (status == null || status.trim().isEmpty() || status.equalsIgnoreCase("ALL")) {
            return getAllOrders();
        }
        return ordersRepository.findByStatusOrderByOrderDateDesc(status.trim());
    }

    @Override
    public Orders getOrderById(Integer id) {
        return ordersRepository.findById(id).orElse(null);
    }

    @Override
    public List<Orders> getOrdersByUserId(Integer userId) {
        return ordersRepository.findByUserIdOrderByOrderDateDesc(userId);
    }

    @Override
    @Transactional
    public Orders createOrder(Integer userId, String shippingAddress, Map<Integer, Integer> cartItems) {
        if (cartItems == null || cartItems.isEmpty()) {
            throw new IllegalArgumentException("Giỏ hàng đang trống, không thể tạo đơn hàng!");
        }

        User user = null;
        if (userId != null && userId > 0) {
            user = userRepository.findById(userId).orElse(null);
        }

        Orders order = new Orders();
        order.setUser(user);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus("PENDING"); // Trạng thái mặc định: Chờ xác nhận
        order.setShippingAddress(shippingAddress != null ? shippingAddress.trim() : "Chưa có địa chỉ");

        double totalAmount = 0.0;
        List<OrderDetails> detailsList = new ArrayList<>();

        for (Map.Entry<Integer, Integer> entry : cartItems.entrySet()) {
            Integer bookId = entry.getKey();
            Integer quantity = entry.getValue();

            if (quantity == null || quantity <= 0) continue;

            Books book = booksRepository.findById(bookId).orElse(null);
            if (book == null) continue;

            // Giảm số lượng tồn kho trong database
            int currentStock = book.getQuantity();
            int newStock = Math.max(0, currentStock - quantity);
            book.setQuantity(newStock);
            booksRepository.save(book);

            OrderDetails detail = new OrderDetails();
            detail.setOrder(order);
            detail.setBook(book);
            detail.setQuantity(quantity);
            detail.setPrice(book.getPrice());

            totalAmount += book.getPrice() * quantity;
            detailsList.add(detail);
        }

        order.setTotalAmount(totalAmount);
        order.setOrderDetails(detailsList);

        Orders savedOrder = ordersRepository.save(order);
        for (OrderDetails detail : detailsList) {
            detail.setOrder(savedOrder);
            orderDetailsRepository.save(detail);
        }

        return savedOrder;
    }

    @Override
    @Transactional
    public void updateOrderStatus(Integer orderId, String newStatus) {
        Orders order = ordersRepository.findById(orderId).orElse(null);
        if (order != null && newStatus != null) {
            order.setStatus(newStatus.trim());
            ordersRepository.save(order);
        }
    }

    @Override
    @Transactional
    public void cancelOrder(Integer orderId) {
        Orders order = ordersRepository.findById(orderId).orElse(null);
        if (order != null && !"CANCELLED".equalsIgnoreCase(order.getStatus())) {
            // Hoàn lại số lượng sách vào kho
            List<OrderDetails> details = orderDetailsRepository.findByOrderId(orderId);
            for (OrderDetails detail : details) {
                Books book = detail.getBook();
                if (book != null) {
                    book.setQuantity(book.getQuantity() + detail.getQuantity());
                    booksRepository.save(book);
                }
            }
            order.setStatus("CANCELLED");
            ordersRepository.save(order);
        }
    }
}

