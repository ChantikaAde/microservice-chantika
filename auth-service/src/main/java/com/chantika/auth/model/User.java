package com.chantika.auth.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    // PRIMARY KEY
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // USERNAME
    @Column(unique = true, nullable = false)
    private String username;

    // EMAIL
    @Column(unique = true, nullable = false)
    private String email;

    // PASSWORD
    @Column(nullable = false)
    private String password;

    // FULL NAME
    @Column(name = "full_name")
    private String fullName;

    // ROLE USER / ADMIN
    @Column(nullable = false)
    private String role = "USER";

    // STATUS USER
    @Column(name = "is_active")
    private Boolean isActive = true;

    // CREATED DATE
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // UPDATED DATE
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // AUTO CREATE DATE
    @PrePersist
    protected void onCreate() {

        createdAt = LocalDateTime.now();

        updatedAt = LocalDateTime.now();
    }

    // AUTO UPDATE DATE
    @PreUpdate
    protected void onUpdate() {

        updatedAt = LocalDateTime.now();
    }
}