package com.example.bookstore.Repository;

import com.example.bookstore.Entity.Login;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoginRepo extends JpaRepository<Login, Integer> {
    List<Login> findByUserIdOrderByLoginTimeDesc(Integer userId);
}
