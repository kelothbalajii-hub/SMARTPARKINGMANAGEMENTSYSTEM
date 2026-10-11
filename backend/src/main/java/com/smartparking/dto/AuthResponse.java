package com.smartparking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AuthResponse DTO - Sent back to the frontend after successful login or registration.
 *
 * After login, we send back the user's information so the frontend can:
 * 1. Show a personalized welcome message
 * 2. Know the user's role (USER or ADMIN) for navigation
 * 3. Store the user's ID for subsequent API calls
 *
 * We do NOT send the password back for security.
 * In a real app, we would send a JWT token here.
 * For this college project, we keep it simple - frontend stores user info in localStorage.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    // User's database ID (used in API calls like /api/vehicles/user/{userId})
    private Long id;

    // User's name (for welcome message: "Welcome, John!")
    private String name;

    // User's email
    private String email;

    // Role: "USER" or "ADMIN" (frontend uses this to show correct navigation)
    private String role;

    // Success or error message
    private String message;

    // Whether the request was successful
    private boolean success;
}
