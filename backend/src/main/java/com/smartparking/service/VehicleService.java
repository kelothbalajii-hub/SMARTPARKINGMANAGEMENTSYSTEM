package com.smartparking.service;

import com.smartparking.dto.VehicleRequest;
import com.smartparking.entity.User;
import com.smartparking.entity.Vehicle;
import com.smartparking.exception.BadRequestException;
import com.smartparking.exception.ResourceNotFoundException;
import com.smartparking.repository.UserRepository;
import com.smartparking.repository.VehicleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * VehicleService - Handles all vehicle management business logic.
 *
 * Responsibilities:
 * - Add a new vehicle (linked to a user)
 * - Get all vehicles for a user
 * - Update vehicle information
 * - Delete a vehicle
 */
@Service
public class VehicleService {

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Add a new vehicle for a user.
     *
     * @param request - Contains vehicleNumber, vehicleType, model, userId
     * @return Saved Vehicle entity
     */
    public Vehicle addVehicle(VehicleRequest request) {
        // Check if vehicle number is already registered in the system
        if (vehicleRepository.existsByVehicleNumber(request.getVehicleNumber())) {
            throw new BadRequestException("Vehicle number '" + request.getVehicleNumber() + "' is already registered.");
        }

        // Find the user who owns this vehicle
        User owner = userRepository.findById(request.getUserId())
            .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + request.getUserId()));

        // Create new Vehicle entity
        Vehicle vehicle = new Vehicle();
        vehicle.setVehicleNumber(request.getVehicleNumber().toUpperCase()); // Store in uppercase
        vehicle.setVehicleType(request.getVehicleType());
        vehicle.setModel(request.getModel());
        vehicle.setUser(owner); // Link vehicle to its owner

        // Save to MySQL and return the saved entity (with auto-generated ID)
        return vehicleRepository.save(vehicle);
    }

    /**
     * Get all vehicles belonging to a specific user.
     * Used to show "My Vehicles" list on the user dashboard.
     *
     * @param userId - The user's ID
     * @return List of vehicles owned by that user
     */
    public List<Vehicle> getVehiclesByUserId(Long userId) {
        return vehicleRepository.findByUserId(userId);
    }

    /**
     * Get all vehicles (Admin view).
     */
    public List<Vehicle> getAllVehicles() {
        return vehicleRepository.findAll();
    }

    /**
     * Update an existing vehicle's information.
     *
     * @param vehicleId - ID of the vehicle to update
     * @param request   - New data to update with
     * @return Updated Vehicle entity
     */
    public Vehicle updateVehicle(Long vehicleId, VehicleRequest request) {
        // Find the existing vehicle
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
            .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with ID: " + vehicleId));

        // Check if the new vehicle number is taken by a different vehicle
        if (!vehicle.getVehicleNumber().equals(request.getVehicleNumber()) &&
            vehicleRepository.existsByVehicleNumber(request.getVehicleNumber())) {
            throw new BadRequestException("Vehicle number '" + request.getVehicleNumber() + "' is already registered.");
        }

        // Update the fields
        vehicle.setVehicleNumber(request.getVehicleNumber().toUpperCase());
        vehicle.setVehicleType(request.getVehicleType());
        vehicle.setModel(request.getModel());

        // Save and return updated vehicle
        return vehicleRepository.save(vehicle);
    }

    /**
     * Delete a vehicle by ID.
     *
     * @param vehicleId - ID of the vehicle to delete
     */
    public void deleteVehicle(Long vehicleId) {
        // Check if vehicle exists before trying to delete
        if (!vehicleRepository.existsById(vehicleId)) {
            throw new ResourceNotFoundException("Vehicle not found with ID: " + vehicleId);
        }
        vehicleRepository.deleteById(vehicleId);
    }
}
