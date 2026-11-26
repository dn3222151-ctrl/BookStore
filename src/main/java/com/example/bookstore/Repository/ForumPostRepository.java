package com.example.bookstore.Repository;

import com.example.bookstore.Entity.ForumPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ForumPostRepository extends JpaRepository<ForumPost, Integer> {
    List<ForumPost> findAllByOrderByCreatedAtDesc();
}