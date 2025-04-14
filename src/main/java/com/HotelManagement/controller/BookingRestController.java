package com.HotelManagement.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.HotelManagement.service.BookingService;

@RestController
@RequestMapping("/api/booking")
public class BookingRestController {
    
    @Autowired
    private BookingService bookingService;
    
    /**
     * API endpoint to check in a booking
     * 
     * @param bookingId the ID of the booking to check in
     * @return a response entity with success status and message
     */
    @PostMapping("/{bookingId}/check-in")
    public ResponseEntity<Map<String, Object>> checkIn(@PathVariable Integer bookingId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            bookingService.checkIn(bookingId);
            response.put("success", true);
            response.put("message", "Booking #" + bookingId + " has been checked in successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * API endpoint to check out a booking
     * 
     * @param bookingId the ID of the booking to check out
     * @return a response entity with success status and message
     */
    @PostMapping("/{bookingId}/check-out")
    public ResponseEntity<Map<String, Object>> checkOut(@PathVariable Integer bookingId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            bookingService.checkOut(bookingId);
            response.put("success", true);
            response.put("message", "Booking #" + bookingId + " has been checked out successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
} 