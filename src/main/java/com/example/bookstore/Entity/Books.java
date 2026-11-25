package com.example.bookstore.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name ="books")
public class Books {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name ="title")
    private String title;

    @Column(name ="author")
    private String author;

    @Column(name ="price")
    private double price;

    @Column(name ="quantity")
    private int quantity;

    @Column(name ="description")
    private String description;

    @Column(name ="image_url")
    private String imageUrl;

    @ManyToOne
    @JoinColumn(name ="category_id", referencedColumnName = "id")
    private Categories categories;
}
