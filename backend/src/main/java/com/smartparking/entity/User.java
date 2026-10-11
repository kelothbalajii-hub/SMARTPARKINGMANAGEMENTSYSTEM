package com.smartparking.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * User Entity - Represents the 'users' table in MySQL database.
 *
 * This class maps to a database table. Each field maps to a column.
 * JPA annotations tell Hibernate how to create and manage the table.
 *
 * Annotations explained:
 * @Entity    - Marks this class as a JPA entity (database table)
 * @Table     - Specifies the exact table name in MySQL
 * @Id        - Marks the primary key column
 * @GeneratedValue - Auto-increments the ID (MySQL handles the numbering)
 * @Column    - Configures column properties (nullable, unique, length)
 *
 * Lombok annotations:
 * @Data          - Auto-generates getters, setters, toString, equals, hashCode
 * @NoArgsConstructor - Auto-generates a no-argument constructor
 * @AllArgsConstructor - Auto-generates a constructor with all fields
 */
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    // Primary key - auto-generated unique ID for each user
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // User's full name - cannot be null
    @Column(nullable = false)
    private String name;

    // Email must be unique (no two users can share same email) and cannot be null
    @Column(nullable = false, unique = true)
    private String email;

    // Password stored as plain text (for simplicity in college project)
    // In real apps, use BCrypt hashing
    @Column(nullable = false)
    private String password;

    // Phone number - optional field
    private String phone;

    // Role can be "USER" or "ADMIN"
    // This determines what the user can access in the application
    @Column(nullable = false)
    private String role;
}
