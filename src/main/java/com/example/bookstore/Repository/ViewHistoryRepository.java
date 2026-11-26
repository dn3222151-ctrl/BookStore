package com.example.bookstore.Repository;

import com.example.bookstore.Entity.ViewHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ViewHistoryRepository extends JpaRepository<ViewHistory, Integer> {
    Optional<ViewHistory> findByUserIdAndBookId(int userId, int bookId);
    Page<ViewHistory> findByUserIdOrderByViewedAtDesc(int userId, Pageable pageable);
}