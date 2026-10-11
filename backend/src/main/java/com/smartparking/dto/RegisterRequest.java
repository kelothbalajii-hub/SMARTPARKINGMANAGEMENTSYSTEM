package com.smartparking.dto;

import lombok.Data;

/**
 * RegisterRequest DTO - Data sent from the frontend Registration form.
 *
 * DTO (Data Transfer Object) carries data between the frontend and backend.
 * We use a separate DTO instead of the User entity directly because:
 * 1. We don't want to expose all entity fields to the frontend
 * 2. We can add validation rules specific to the request
 * 3. Keeps the entity clean and focused on database mapping
 *
 * When a user fills in the registration form:
 * Frontend sends JSON → Spring deserializes into this class → Service processes it
 */
@Data
public class RegisterRequest {

    // User's full name from the form
    private String name;

    // Email address (used for login)
    private String email;

    // Password (plain text for simplicity - real apps would hash this)
    private String password;

    // Optional phone number
    private String phone;
}
