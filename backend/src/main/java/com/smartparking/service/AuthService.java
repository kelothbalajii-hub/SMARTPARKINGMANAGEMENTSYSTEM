package com.smartparking.service;

import com.smartparking.dto.AuthResponse;
import com.smartparking.dto.LoginRequest;
import com.smartparking.dto.RegisterRequest;
import com.smartparking.entity.User;
import com.smartparking.exception.BadRequestException;
import com.smartparking.exception.ResourceNotFoundException;
import com.smartparking.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * AuthService - Handles user registration and login business logic.
 *
 * The Service layer sits between the Controller and Repository.
 * Controller: Receives HTTP request → calls Service
 * Service:    Contains business logic → calls Repository
 * Repository: Talks to MySQL → returns data to Service
 *
 * @Service - Marks this class as a Spring Service component.
 *            Spring creates one instance and makes it available everywhere (Singleton pattern).
 */
@Service
public class AuthService {

    /**
     * @Autowired - Spring automatically injects the UserRepository instance here.
     * We don't create it with "new UserRepository()" - Spring manages the lifecycle.
     */
    @Autowired
    private UserRepository userRepository;

    /**
     * Register a new user in the system.
     *
     * Steps:
     * 1. Check if email is already taken
     * 2. Create a new User entity with the provided data
     * 3. Set role to "USER" (default)
     * 4. Save to MySQL
     * 5. Return success response
     *
     * @param request  - Contains name, email, password, phone from the registration form
     * @return AuthResponse - Contains the saved user's info and success message
     */
    public AuthResponse register(RegisterRequest request) {
        // Step 1: Check if email is already registered
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email is already registered. Please use a different email.");
        }

        // Step 2: Create a new User entity from the request data
        User newUser = new User();
        newUser.setName(request.getName());
        newUser.setEmail(request.getEmail());
        newUser.setPassword(request.getPassword()); // Plain text for simplicity
        newUser.setPhone(request.getPhone());
        newUser.setRole("USER"); // Default role for new registrations

        // Step 3: Save the user to MySQL (JPA generates INSERT SQL)
        User savedUser = userRepository.save(newUser);

        // Step 4: Build and return success response
        return new AuthResponse(
            savedUser.getId(),
            savedUser.getName(),
            savedUser.getEmail(),
            savedUser.getRole(),
            "Registration successful! Welcome to Smart Parking.",
            true
        );
    }

    /**
     * Login an existing user.
     *
     * Steps:
     * 1. Find the user by email
     * 2. Check if password matches
     * 3. Return user info on success
     *
     * @param request  - Contains email and password from the login form
     * @return AuthResponse - Contains user's info for the frontend to store
     */
    public AuthResponse login(LoginRequest request) {
        // Step 1: Find user by email
        // findByEmail returns Optional<User> - it may or may not have a value
        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new ResourceNotFoundException("No account found with email: " + request.getEmail()));

        // Step 2: Check if the password matches
        // In a real app, we'd use BCrypt: passwordEncoder.matches(request.getPassword(), user.getPassword())
        if (!user.getPassword().equals(request.getPassword())) {
            throw new BadRequestException("Incorrect password. Please try again.");
        }

        // Step 3: Return user info - frontend will store this in localStorage
        return new AuthResponse(
            user.getId(),
            user.getName(),
            user.getEmail(),
            user.getRole(),
            "Login successful! Welcome back, " + user.getName() + "!",
            true
        );
    }

    /**
     * Get all users (Admin only feature).
     * @return List of all users in the database
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Get a single user by their ID.
     * @param userId - The user's database ID
     * @return User entity
     */
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
    }
}
