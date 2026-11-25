package com.example.bookstore.Repository;

import com.example.bookstore.Entity.Orders;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrdersRepository extends JpaRepository<Orders, Integer> {
    List<Orders> findAllByOrderByOrderDateDesc();
    List<Orders> findByUserIdOrderByOrderDateDesc(Integer userId);
    List<Orders> findByStatusOrderByOrderDateDesc(String status);
}

