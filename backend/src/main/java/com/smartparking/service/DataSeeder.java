package com.smartparking.service;

import com.smartparking.entity.ParkingArea;
import com.smartparking.entity.ParkingSlot;
import com.smartparking.entity.User;
import com.smartparking.repository.ParkingAreaRepository;
import com.smartparking.repository.ParkingSlotRepository;
import com.smartparking.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * DataSeeder - Automatically inserts sample/demo data when the application starts.
 *
 * CommandLineRunner is a Spring Boot interface.
 * The run() method is called automatically after the Spring context is fully loaded.
 *
 * This ensures the application has demo data immediately for:
 * 1. Admin account to log in
 * 2. Sample parking areas
 * 3. Pre-configured parking slots
 *
 * We check if data already exists before inserting to avoid duplicates on restart.
 *
 * @Component - Marks this as a Spring-managed component (similar to @Service or @Controller)
 */
@Component
public class DataSeeder implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ParkingAreaRepository parkingAreaRepository;

    @Autowired
    private ParkingSlotRepository parkingSlotRepository;

    /**
     * This method runs automatically when Spring Boot finishes starting.
     * It seeds sample data so the application is ready for demo immediately.
     */
    @Override
    public void run(String... args) {
        System.out.println("Checking and seeding sample data...");
        seedUsers();
        seedParkingAreas();
        System.out.println("Sample data seeding complete!");
    }

    /**
     * Create admin and sample user accounts if they don't exist.
     */
    private void seedUsers() {
        // Create Admin account if it doesn't exist
        if (!userRepository.existsByEmail("admin@smartparking.com")) {
            User admin = new User();
            admin.setName("System Admin");
            admin.setEmail("admin@smartparking.com");
            admin.setPassword("admin123");
            admin.setPhone("9000000000");
            admin.setRole("ADMIN");
            userRepository.save(admin);
            System.out.println("Admin account created: admin@smartparking.com / admin123");
        }

        // Create a demo user account
        if (!userRepository.existsByEmail("demo@smartparking.com")) {
            User demoUser = new User();
            demoUser.setName("Demo User");
            demoUser.setEmail("demo@smartparking.com");
            demoUser.setPassword("demo123");
            demoUser.setPhone("9876543210");
            demoUser.setRole("USER");
            userRepository.save(demoUser);
            System.out.println("Demo user created: demo@smartparking.com / demo123");
        }
    }

    /**
     * Create sample parking areas and their slots if they don't exist.
     */
    private void seedParkingAreas() {
        // Only seed if no parking areas exist yet
        if (parkingAreaRepository.count() == 0) {

            // ===== Parking Area 1: Anurag University Main Parking =====
            ParkingArea mainParking = new ParkingArea();
            mainParking.setName("Anurag University Main Parking");
            mainParking.setLocation("Main Block, Gate 1");
            mainParking.setTotalSlots(10);
            mainParking.setPricePerHour(20.0); // ₹20 per hour
            mainParking.setDescription("Primary parking area near the main entrance. Available for students, staff, and visitors.");
            ParkingArea savedMain = parkingAreaRepository.save(mainParking);

            // Create 10 slots for this area with some pre-set statuses for demo
            String[] mainSlotStatuses = {
                "AVAILABLE", "AVAILABLE", "AVAILABLE", "AVAILABLE", "AVAILABLE",
                "RESERVED",  "AVAILABLE", "OCCUPIED",  "AVAILABLE", "AVAILABLE"
            };
            for (int i = 1; i <= 10; i++) {
                ParkingSlot slot = new ParkingSlot();
                slot.setSlotNumber("A" + String.format("%02d", i)); // A01, A02, ... A10
                slot.setSlotType("CAR");
                slot.setStatus(mainSlotStatuses[i - 1]);
                slot.setParkingArea(savedMain);
                parkingSlotRepository.save(slot);
            }
            System.out.println("Created Anurag University Main Parking with 10 slots (A01-A10)");

            // ===== Parking Area 2: Anurag University Library Parking =====
            ParkingArea libraryParking = new ParkingArea();
            libraryParking.setName("Library Block Parking");
            libraryParking.setLocation("Library Building, East Wing");
            libraryParking.setTotalSlots(8);
            libraryParking.setPricePerHour(15.0); // ₹15 per hour
            libraryParking.setDescription("Parking area adjacent to the library. Ideal for researchers and students.");
            ParkingArea savedLibrary = parkingAreaRepository.save(libraryParking);

            // Create 8 slots for library parking (4 car, 4 bike)
            for (int i = 1; i <= 4; i++) {
                ParkingSlot carSlot = new ParkingSlot();
                carSlot.setSlotNumber("B" + String.format("%02d", i)); // B01, B02, B03, B04
                carSlot.setSlotType("CAR");
                carSlot.setStatus("AVAILABLE");
                carSlot.setParkingArea(savedLibrary);
                parkingSlotRepository.save(carSlot);
            }
            for (int i = 5; i <= 8; i++) {
                ParkingSlot bikeSlot = new ParkingSlot();
                bikeSlot.setSlotNumber("B" + String.format("%02d", i)); // B05, B06, B07, B08
                bikeSlot.setSlotType("BIKE");
                bikeSlot.setStatus("AVAILABLE");
                bikeSlot.setParkingArea(savedLibrary);
                parkingSlotRepository.save(bikeSlot);
            }
            System.out.println("Created Library Block Parking with 8 slots (B01-B08)");

            // ===== Parking Area 3: Sports Complex Parking =====
            ParkingArea sportsParking = new ParkingArea();
            sportsParking.setName("Sports Complex Parking");
            sportsParking.setLocation("Sports Complex, South Gate");
            sportsParking.setTotalSlots(6);
            sportsParking.setPricePerHour(10.0); // ₹10 per hour
            sportsParking.setDescription("Parking area near the sports complex. For athletes and sports event attendees.");
            ParkingArea savedSports = parkingAreaRepository.save(sportsParking);

            // Create 6 slots
            for (int i = 1; i <= 6; i++) {
                ParkingSlot slot = new ParkingSlot();
                slot.setSlotNumber("C" + String.format("%02d", i)); // C01 to C06
                slot.setSlotType(i <= 3 ? "CAR" : "BIKE"); // First 3 for cars, last 3 for bikes
                slot.setStatus("AVAILABLE");
                slot.setParkingArea(savedSports);
                parkingSlotRepository.save(slot);
            }
            System.out.println("Created Sports Complex Parking with 6 slots (C01-C06)");
        }
    }
}
