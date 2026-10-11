package com.smartparking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ParkingEntryRequest DTO - Data sent when a vehicle enters the parking area.
 *
 * This triggers:
 * 1. Creation of a new ParkingSession with entryTime = now()
 * 2. Reservation status changes from ACTIVE → COMPLETED
 * 3. ParkingSlot status changes from RESERVED → OCCUPIED
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParkingEntryRequest {

    // ID of the user whose vehicle is entering
    private Long userId;

    // ID of the vehicle entering
    private Long vehicleId;

    // ID of the parking slot (must currently be RESERVED)
    private Long parkingSlotId;

    // ID of the reservation (to mark it as completed)
    private Long reservationId;
}
