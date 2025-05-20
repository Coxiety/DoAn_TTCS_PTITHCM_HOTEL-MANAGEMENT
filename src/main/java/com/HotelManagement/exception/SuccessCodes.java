package com.HotelManagement.exception;

import java.util.HashMap;
import java.util.Map;

/**
 * Centralized success code definitions for the application
 */
public class SuccessCodes {
    // User related success codes (1xx)
    public static final String USER_CREATED = "S101";
    public static final String USER_UPDATED = "S102";
    public static final String USER_DELETED = "S103";
    public static final String PASSWORD_RESET = "S104";
    
    // Staff related success codes (2xx)
    public static final String STAFF_CREATED = "S201";
    public static final String STAFF_UPDATED = "S202";
    public static final String STAFF_DELETED = "S203";
    
    // Room related success codes (3xx)
    public static final String ROOM_CREATED = "S301";
    public static final String ROOM_UPDATED = "S302";
    public static final String ROOM_DELETED = "S303";
    
    // Room type related success codes (4xx)
    public static final String ROOM_TYPE_CREATED = "S401";
    public static final String ROOM_TYPE_UPDATED = "S402";
    public static final String ROOM_TYPE_DELETED = "S403";
    
    // Booking related success codes (5xx)
    public static final String BOOKING_CREATED = "S501";
    public static final String BOOKING_UPDATED = "S502";
    public static final String BOOKING_DELETED = "S503";
    public static final String BOOKING_CHECKED_IN = "S504";
    public static final String BOOKING_CHECKED_OUT = "S505";
    public static final String PAYMENT_PROCESSED = "S506";
    
    // Map of success codes to success messages
    private static final Map<String, String> SUCCESS_MESSAGES = new HashMap<>();
    
    static {
        // User related success messages
        SUCCESS_MESSAGES.put(USER_CREATED, "User created successfully");
        SUCCESS_MESSAGES.put(USER_UPDATED, "User updated successfully");
        SUCCESS_MESSAGES.put(USER_DELETED, "User deleted successfully");
        SUCCESS_MESSAGES.put(PASSWORD_RESET, "Password reset successfully");
        
        // Staff related success messages
        SUCCESS_MESSAGES.put(STAFF_CREATED, "Staff member created successfully");
        SUCCESS_MESSAGES.put(STAFF_UPDATED, "Staff member updated successfully");
        SUCCESS_MESSAGES.put(STAFF_DELETED, "Staff member deleted successfully");
        
        // Room related success messages
        SUCCESS_MESSAGES.put(ROOM_CREATED, "Room created successfully");
        SUCCESS_MESSAGES.put(ROOM_UPDATED, "Room updated successfully");
        SUCCESS_MESSAGES.put(ROOM_DELETED, "Room deleted successfully");
        
        // Room type related success messages
        SUCCESS_MESSAGES.put(ROOM_TYPE_CREATED, "Room type created successfully");
        SUCCESS_MESSAGES.put(ROOM_TYPE_UPDATED, "Room type updated successfully");
        SUCCESS_MESSAGES.put(ROOM_TYPE_DELETED, "Room type deleted successfully");
        
        // Booking related success messages
        SUCCESS_MESSAGES.put(BOOKING_CREATED, "Booking created successfully");
        SUCCESS_MESSAGES.put(BOOKING_UPDATED, "Booking updated successfully");
        SUCCESS_MESSAGES.put(BOOKING_DELETED, "Booking deleted successfully");
        SUCCESS_MESSAGES.put(BOOKING_CHECKED_IN, "Check-in processed successfully");
        SUCCESS_MESSAGES.put(BOOKING_CHECKED_OUT, "Check-out processed successfully");
        SUCCESS_MESSAGES.put(PAYMENT_PROCESSED, "Payment processed successfully");
    }
    
    /**
     * Get the success message for a given success code
     * @param code the success code
     * @return the corresponding success message
     */
    public static String getMessage(String code) {
        return SUCCESS_MESSAGES.getOrDefault(code, "Operation completed successfully");
    }
} 