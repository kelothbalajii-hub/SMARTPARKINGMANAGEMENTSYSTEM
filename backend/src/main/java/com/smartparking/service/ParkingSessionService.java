package com.smartparking.service;

import com.smartparking.dto.ParkingEntryRequest;
import com.smartparking.entity.ParkingSession;
import com.smartparking.entity.ParkingSlot;
import com.smartparking.entity.Reservation;
import com.smartparking.entity.User;
import com.smartparking.entity.Vehicle;
import com.smartparking.exception.BadRequestException;
import com.smartparking.exception.ResourceNotFoundException;
import com.smartparking.repository.ParkingSessionRepository;
import com.smartparking.repository.ParkingSlotRepository;
import com.smartparking.repository.ReservationRepository;
import com.smartparking.repository.UserRepository;
import com.smartparking.repository.VehicleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

/**
 * ParkingSessionService - The core service handling vehicle entry, exit, and fee calculation.
 *
 * This service manages the complete parking lifecycle:
 * RESERVED slot → Vehicle Entry → OCCUPIED slot → Vehicle Exit → AVAILABLE slot
 *                                 ↓                               ↓
 *                         Active Session created         Fee calculated + Session completed
 *
 * THE MOST IMPORTANT SERVICE in the application for the demo workflow.
 */
@Service
public class ParkingSessionService {

    @Autowired
    private ParkingSessionRepository sessionRepository;

    @Autowired
    private ParkingSlotRepository slotRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    /**
     * Record vehicle ENTRY into the parking area.
     *
     * What happens:
     * 1. Find the reservation (must be ACTIVE)
     * 2. Create a new ParkingSession with entryTime = now
     * 3. Mark the reservation as COMPLETED
     * 4. Change slot status from RESERVED → OCCUPIED
     *
     * @param request - Contains userId, vehicleId, parkingSlotId, reservationId
     * @return The newly created active ParkingSession
     */
    @Transactional
    public ParkingSession recordEntry(ParkingEntryRequest request) {
        // Step 1: Find and validate the parking slot
        ParkingSlot slot = slotRepository.findById(request.getParkingSlotId())
            .orElseThrow(() -> new ResourceNotFoundException("Parking slot not found with ID: " + request.getParkingSlotId()));

        // The slot must be RESERVED before entry (user must have reserved it first)
        if (!slot.getStatus().equals("RESERVED")) {
            throw new BadRequestException("Slot '" + slot.getSlotNumber() + "' is not reserved. Please reserve the slot first.");
        }

        // Step 2: Find the associated reservation and mark it COMPLETED
        if (request.getReservationId() != null) {
            Optional<Reservation> reservationOpt = reservationRepository.findById(request.getReservationId());
            if (reservationOpt.isPresent()) {
                Reservation reservation = reservationOpt.get();
                reservation.setStatus("COMPLETED");
                reservationRepository.save(reservation);
            }
        }

        // Step 3: Find the user and vehicle
        User user = userRepository.findById(request.getUserId())
            .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + request.getUserId()));

        Vehicle vehicle = vehicleRepository.findById(request.getVehicleId())
            .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with ID: " + request.getVehicleId()));

        // Step 4: Create a new Parking Session
        ParkingSession session = new ParkingSession();
        session.setUser(user);
        session.setVehicle(vehicle);
        session.setParkingSlot(slot);
        session.setEntryTime(LocalDateTime.now()); // Record when vehicle entered
        session.setStatus("ACTIVE"); // Session is now active
        // exitTime, durationMinutes, totalAmount will be set when vehicle exits

        // Step 5: Change slot status from RESERVED → OCCUPIED
        slot.setStatus("OCCUPIED");
        slotRepository.save(slot);

        // Step 6: Save the session and return it
        return sessionRepository.save(session);
    }

    /**
     * Record vehicle EXIT from the parking area.
     *
     * This is where the AUTOMATIC FEE CALCULATION happens!
     *
     * What happens:
     * 1. Find the active parking session
     * 2. Record exit time
     * 3. Calculate duration (in minutes and hours)
     * 4. Calculate fee (hours × pricePerHour, rounded up to nearest hour)
     * 5. Mark session as COMPLETED
     * 6. Change slot status from OCCUPIED → AVAILABLE
     *
     * @param sessionId - The ID of the active parking session
     * @return The completed ParkingSession with fee information
     */
    @Transactional
    public ParkingSession recordExit(Long sessionId) {
        // Step 1: Find the active session
        ParkingSession session = sessionRepository.findById(sessionId)
            .orElseThrow(() -> new ResourceNotFoundException("Parking session not found with ID: " + sessionId));

        // Validate the session is still active
        if (!session.getStatus().equals("ACTIVE")) {
            throw new BadRequestException("This parking session is already completed.");
        }

        // Step 2: Record the exit time (current time)
        LocalDateTime exitTime = LocalDateTime.now();
        session.setExitTime(exitTime);

        // =====================================================================
        // AUTOMATIC FEE CALCULATION
        // This is the key feature - calculating parking charges automatically
        // =====================================================================

        // Step 3: Calculate duration in minutes
        // ChronoUnit.MINUTES.between() calculates the difference between two times in minutes
        // Example: Entry at 10:00 AM, Exit at 11:30 AM → 90 minutes
        long durationInMinutes = ChronoUnit.MINUTES.between(session.getEntryTime(), exitTime);

        // Ensure at least 1 minute is recorded (even if user exits immediately)
        if (durationInMinutes < 1) {
            durationInMinutes = 1;
        }

        // Store duration in minutes
        session.setDurationMinutes(durationInMinutes);

        // Step 4: Calculate the number of hours (rounded UP to nearest hour)
        // This is the billing rule: partial hours are charged as full hours
        // Example: 90 minutes → 2 hours (1 full hour + 30 min partial → round up to 2)
        // Math.ceil() rounds UP: Math.ceil(1.5) = 2.0, Math.ceil(2.0) = 2.0
        double durationInHours = Math.ceil((double) durationInMinutes / 60.0);

        // Step 5: Get the price per hour from the parking area
        // slot → parkingArea → pricePerHour
        double pricePerHour = session.getParkingSlot().getParkingArea().getPricePerHour();

        // Step 6: Calculate the total fee
        // Formula: Total Fee = Number of Billable Hours × Price Per Hour
        // Example: 2 hours × ₹20/hour = ₹40
        double totalFee = durationInHours * pricePerHour;

        // Store the calculated fee
        session.setTotalAmount(totalFee);

        // Step 7: Mark session as COMPLETED
        session.setStatus("COMPLETED");

        // Step 8: Free up the parking slot - change back to AVAILABLE
        // OCCUPIED → AVAILABLE
        ParkingSlot slot = session.getParkingSlot();
        slot.setStatus("AVAILABLE");
        slotRepository.save(slot);

        // Step 9: Save the completed session with all fee details
        return sessionRepository.save(session);
    }

    /**
     * Get the currently active parking session for a user.
     * Used to show the "Active Session" card on the user dashboard.
     *
     * @param userId - The user's ID
     * @return Optional<ParkingSession> - Present if user has an active session, empty if not
     */
    public Optional<ParkingSession> getActiveSession(Long userId) {
        return sessionRepository.findByUserIdAndStatus(userId, "ACTIVE");
    }

    /**
     * Get the complete parking history for a user.
     * Shows all completed sessions with duration and fees paid.
     *
     * @param userId - The user's ID
     * @return List of completed sessions, newest first
     */
    public List<ParkingSession> getParkingHistory(Long userId) {
        return sessionRepository.findByUserIdOrderByEntryTimeDesc(userId);
    }

    /**
     * Get all active parking sessions (Admin Dashboard view).
     */
    public List<ParkingSession> getAllActiveSessions() {
        return sessionRepository.findByStatus("ACTIVE");
    }

    /**
     * Get all completed parking sessions (Admin Dashboard view).
     */
    public List<ParkingSession> getAllCompletedSessions() {
        return sessionRepository.findByStatus("COMPLETED");
    }

    /**
     * Get a specific parking session by its ID.
     */
    public ParkingSession getSessionById(Long sessionId) {
        return sessionRepository.findById(sessionId)
            .orElseThrow(() -> new ResourceNotFoundException("Parking session not found with ID: " + sessionId));
    }
}
