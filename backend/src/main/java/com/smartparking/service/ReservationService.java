package com.smartparking.service;

import com.smartparking.dto.ReservationRequest;
import com.smartparking.entity.ParkingSlot;
import com.smartparking.entity.Reservation;
import com.smartparking.entity.User;
import com.smartparking.entity.Vehicle;
import com.smartparking.exception.BadRequestException;
import com.smartparking.exception.ResourceNotFoundException;
import com.smartparking.repository.ParkingSlotRepository;
import com.smartparking.repository.ReservationRepository;
import com.smartparking.repository.UserRepository;
import com.smartparking.repository.VehicleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * ReservationService - Handles the slot reservation business logic.
 *
 * This is a critical part of the parking workflow:
 * 1. User selects an AVAILABLE slot
 * 2. This service validates the slot is truly available
 * 3. Creates a Reservation record
 * 4. Changes slot status from AVAILABLE → RESERVED
 * 5. Returns confirmation to frontend
 *
 * @Transactional - Ensures that the reservation creation AND slot status update
 *                  happen together (atomically). If one fails, both roll back.
 *                  This prevents situations where reservation is created but slot
 *                  status doesn't update (data inconsistency).
 */
@Service
public class ReservationService {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ParkingSlotRepository parkingSlotRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    /**
     * Create a new reservation for a parking slot.
     *
     * Workflow:
     * AVAILABLE slot → User clicks Reserve → This method runs → RESERVED slot
     *
     * @param request - Contains userId, vehicleId, parkingSlotId
     * @return The created Reservation entity
     */
    @Transactional
    public Reservation createReservation(ReservationRequest request) {
        // Step 1: Find the parking slot the user wants to reserve
        ParkingSlot slot = parkingSlotRepository.findById(request.getParkingSlotId())
            .orElseThrow(() -> new ResourceNotFoundException("Parking slot not found with ID: " + request.getParkingSlotId()));

        // Step 2: Validate the slot is actually available
        // This is the critical business rule - we cannot reserve a non-available slot
        if (!slot.getStatus().equals("AVAILABLE")) {
            throw new BadRequestException("Parking slot '" + slot.getSlotNumber() + "' is not available. Current status: " + slot.getStatus());
        }

        // Step 3: Find the user making the reservation
        User user = userRepository.findById(request.getUserId())
            .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + request.getUserId()));

        // Step 4: Find the vehicle being parked
        Vehicle vehicle = vehicleRepository.findById(request.getVehicleId())
            .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with ID: " + request.getVehicleId()));

        // Step 5: Create the Reservation record
        Reservation reservation = new Reservation();
        reservation.setUser(user);
        reservation.setVehicle(vehicle);
        reservation.setParkingSlot(slot);
        reservation.setStatus("ACTIVE");
        // reservationTime is auto-set by @PrePersist in the Reservation entity

        // Step 6: Change slot status from AVAILABLE to RESERVED
        // This is the key status change in the parking workflow!
        slot.setStatus("RESERVED");
        parkingSlotRepository.save(slot); // Update the slot in MySQL

        // Step 7: Save the reservation to MySQL and return it
        return reservationRepository.save(reservation);
    }

    /**
     * Get all reservations for a specific user (for "My Reservations" page).
     */
    public List<Reservation> getReservationsByUser(Long userId) {
        return reservationRepository.findByUserId(userId);
    }

    /**
     * Get all reservations (Admin view).
     */
    public List<Reservation> getAllReservations() {
        return reservationRepository.findAll();
    }

    /**
     * Cancel a reservation.
     * Changes: Reservation status → CANCELLED, Slot status → AVAILABLE
     *
     * @param reservationId - ID of the reservation to cancel
     */
    @Transactional
    public Reservation cancelReservation(Long reservationId) {
        // Find the reservation
        Reservation reservation = reservationRepository.findById(reservationId)
            .orElseThrow(() -> new ResourceNotFoundException("Reservation not found with ID: " + reservationId));

        // Can only cancel ACTIVE reservations
        if (!reservation.getStatus().equals("ACTIVE")) {
            throw new BadRequestException("Only active reservations can be cancelled.");
        }

        // Update reservation status
        reservation.setStatus("CANCELLED");

        // Free up the parking slot - change back to AVAILABLE
        ParkingSlot slot = reservation.getParkingSlot();
        slot.setStatus("AVAILABLE");
        parkingSlotRepository.save(slot);

        return reservationRepository.save(reservation);
    }
}
