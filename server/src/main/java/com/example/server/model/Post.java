package com.example.server.model;

import jakarta.persistence.*;

@Entity
@Table(name = "posts")
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_post", nullable = false)
    private Integer id;

    @Column(name = "name_post", nullable = false, length = Integer.MAX_VALUE)
    private String namePost;

    // Пустой конструктор (нужен для JPA)
    public Post() {
    }

    // Конструктор для создания роли без ID
    public Post(String namePost) {
        this.namePost = namePost;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNamePost() {
        return namePost;
    }

    public void setNamePost(String namePost) {
        this.namePost = namePost;
    }

}