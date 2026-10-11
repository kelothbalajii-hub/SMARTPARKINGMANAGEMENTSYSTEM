package com.smartparking.dto;

import lombok.Data;

/**
 * VehicleRequest DTO - Data sent when adding or updating a vehicle.
 */
@Data
public class VehicleRequest {

    // Vehicle registration number (e.g., "TS09AB1234")
    private String vehicleNumber;

    // Type: "CAR", "BIKE", or "TRUCK"
    private String vehicleType;

    // Model name (e.g., "Honda City")
    private String model;

    // ID of the user who owns this vehicle (passed from frontend's localStorage)
    private Long userId;
}
