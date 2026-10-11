package com.smartparking.service;

import com.smartparking.entity.ParkingArea;
import com.smartparking.entity.ParkingSlot;
import com.smartparking.exception.ResourceNotFoundException;
import com.smartparking.repository.ParkingAreaRepository;
import com.smartparking.repository.ParkingSlotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * ParkingAreaService - Handles parking area and parking slot management.
 *
 * This service manages:
 * 1. Parking areas (like "Anurag University Main Parking")
 * 2. Parking slots within those areas (like A01, A02, ... A10)
 *
 * Admin uses this to:
 * - Add new parking areas
 * - Add slots to areas
 * - Update or delete areas/slots
 *
 * Users use this to:
 * - View available parking areas
 * - See slot availability in an area
 */
@Service
public class ParkingAreaService {

    @Autowired
    private ParkingAreaRepository parkingAreaRepository;

    @Autowired
    private ParkingSlotRepository parkingSlotRepository;

    // ===== PARKING AREA METHODS =====

    /**
     * Get all parking areas (shown on the "Parking Areas" page).
     */
    public List<ParkingArea> getAllParkingAreas() {
        return parkingAreaRepository.findAll();
    }

    /**
     * Get a specific parking area by its ID.
     */
    public ParkingArea getParkingAreaById(Long id) {
        return parkingAreaRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Parking area not found with ID: " + id));
    }

    /**
     * Add a new parking area (Admin only).
     * @param parkingArea - Area data from the Admin form
     * @return Saved ParkingArea with auto-generated ID
     */
    public ParkingArea addParkingArea(ParkingArea parkingArea) {
        return parkingAreaRepository.save(parkingArea);
    }

    /**
     * Update an existing parking area (Admin only).
     */
    public ParkingArea updateParkingArea(Long id, ParkingArea updatedArea) {
        // Check it exists first
        ParkingArea existingArea = parkingAreaRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Parking area not found with ID: " + id));

        // Update the fields
        existingArea.setName(updatedArea.getName());
        existingArea.setLocation(updatedArea.getLocation());
        existingArea.setTotalSlots(updatedArea.getTotalSlots());
        existingArea.setPricePerHour(updatedArea.getPricePerHour());
        existingArea.setDescription(updatedArea.getDescription());

        return parkingAreaRepository.save(existingArea);
    }

    /**
     * Delete a parking area (Admin only).
     */
    public void deleteParkingArea(Long id) {
        if (!parkingAreaRepository.existsById(id)) {
            throw new ResourceNotFoundException("Parking area not found with ID: " + id);
        }
        parkingAreaRepository.deleteById(id);
    }

    // ===== PARKING SLOT METHODS =====

    /**
     * Get all parking slots for a specific parking area.
     * This is used to render the visual slot grid on the frontend.
     *
     * @param parkingAreaId - The ID of the parking area
     * @return List of all slots in that area with their current status
     */
    public List<ParkingSlot> getSlotsByArea(Long parkingAreaId) {
        return parkingSlotRepository.findByParkingAreaId(parkingAreaId);
    }

    /**
     * Get only AVAILABLE slots in a parking area.
     * Used to show which slots the user can actually reserve.
     */
    public List<ParkingSlot> getAvailableSlots(Long parkingAreaId) {
        return parkingSlotRepository.findByParkingAreaIdAndStatus(parkingAreaId, "AVAILABLE");
    }

    /**
     * Add a new parking slot to a parking area (Admin only).
     * Initial status is always "AVAILABLE".
     */
    public ParkingSlot addParkingSlot(Long areaId, ParkingSlot parkingSlot) {
        // Find the parking area this slot belongs to
        ParkingArea area = parkingAreaRepository.findById(areaId)
            .orElseThrow(() -> new ResourceNotFoundException("Parking area not found with ID: " + areaId));

        // Set the parking area relationship
        parkingSlot.setParkingArea(area);

        // New slots always start as AVAILABLE
        parkingSlot.setStatus("AVAILABLE");

        return parkingSlotRepository.save(parkingSlot);
    }

    /**
     * Update a parking slot's information (Admin only).
     */
    public ParkingSlot updateParkingSlot(Long slotId, ParkingSlot updatedSlot) {
        ParkingSlot existingSlot = parkingSlotRepository.findById(slotId)
            .orElseThrow(() -> new ResourceNotFoundException("Parking slot not found with ID: " + slotId));

        existingSlot.setSlotNumber(updatedSlot.getSlotNumber());
        existingSlot.setSlotType(updatedSlot.getSlotType());
        existingSlot.setStatus(updatedSlot.getStatus());

        return parkingSlotRepository.save(existingSlot);
    }

    /**
     * Delete a parking slot (Admin only).
     */
    public void deleteParkingSlot(Long slotId) {
        if (!parkingSlotRepository.existsById(slotId)) {
            throw new ResourceNotFoundException("Parking slot not found with ID: " + slotId);
        }
        parkingSlotRepository.deleteById(slotId);
    }

    /**
     * Get all parking slots (Admin view - all areas).
     */
    public List<ParkingSlot> getAllSlots() {
        return parkingSlotRepository.findAll();
    }

    /**
     * Get a single parking slot by ID.
     */
    public ParkingSlot getSlotById(Long slotId) {
        return parkingSlotRepository.findById(slotId)
            .orElseThrow(() -> new ResourceNotFoundException("Parking slot not found with ID: " + slotId));
    }
}
