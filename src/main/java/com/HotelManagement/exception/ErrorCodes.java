package com.HotelManagement.exception;

import java.util.HashMap;
import java.util.Map;

/**
 * Centralized error code definitions for the application
 */
public class ErrorCodes {
    // User related error codes (1xx)
    public static final String USER_NOT_FOUND = "E101";
    public static final String USERNAME_EXISTS = "E102";
    public static final String EMAIL_EXISTS = "E103";
    public static final String PHONE_EXISTS = "E104";
    public static final String INVALID_ROLE = "E105";
    public static final String LAST_ADMIN = "E106";
    public static final String SELF_DELETE = "E107";
    public static final String EDIT_OTHER_ADMIN = "E108";
    public static final String CHANGE_OWN_ADMIN_ROLE = "E109";
    public static final String USER_HAS_BOOKINGS = "E110";
    
    // Room related error codes (2xx)
    public static final String ROOM_NOT_FOUND = "E201";
    public static final String ROOM_NUMBER_EXISTS = "E202";
    public static final String ROOM_TYPE_NOT_FOUND = "E203";
    public static final String ROOM_HAS_BOOKINGS = "E204";
    public static final String ROOM_NOT_FOUND_BY_NUMBER = "E205";
    
    // Room type related error codes (3xx)
    public static final String INVALID_PRICE = "E301";
    public static final String ROOM_TYPE_HAS_ROOMS = "E302";
    public static final String INVALID_IMAGE_FORMAT = "E303";
    public static final String REQUIRED_FIELD_UNIQUE = "E304";
    
    // Booking related error codes (4xx)
    public static final String BOOKING_NOT_FOUND = "E401";
    public static final String INVALID_DATE_RANGE = "E402";
    public static final String ROOM_NOT_AVAILABLE = "E403";
    public static final String INVALID_STATUS_TRANSITION = "E404";
    public static final String CUSTOMER_NOT_FOUND = "E405";
    public static final String CUSTOMER_ID_REQUIRED = "E406";
    public static final String NOT_ENOUGH_ROOMS = "E407";
    public static final String WRONG_BOOKING_STATUS = "E408";
    public static final String EARLY_CHECKIN = "E409";
    public static final String NO_ACTIVE_BOOKING = "E410";
    
    // Customer related error codes (5xx)
    public static final String CUSTOMER_PHONE_EXISTS = "E501";
    public static final String CUSTOMER_EMAIL_EXISTS = "E502";
    
    // Map of error codes to error messages
    private static final Map<String, String> ERROR_MESSAGES = new HashMap<>();
    
    static {
        // User related errors
        ERROR_MESSAGES.put(USER_NOT_FOUND, "User not found");
        ERROR_MESSAGES.put(USERNAME_EXISTS, "Username already exists");
        ERROR_MESSAGES.put(EMAIL_EXISTS, "Email already exists");
        ERROR_MESSAGES.put(PHONE_EXISTS, "Phone number already exists");
        ERROR_MESSAGES.put(INVALID_ROLE, "Invalid role ID. Must be 1 (admin) or 2 (receptionist)");
        ERROR_MESSAGES.put(LAST_ADMIN, "Cannot delete or change role of the last administrator");
        ERROR_MESSAGES.put(SELF_DELETE, "Administrators cannot delete their own accounts");
        ERROR_MESSAGES.put(EDIT_OTHER_ADMIN, "Cannot edit information of another admin");
        ERROR_MESSAGES.put(CHANGE_OWN_ADMIN_ROLE, "Cannot change your own admin role");
        ERROR_MESSAGES.put(USER_HAS_BOOKINGS, "Cannot delete user with existing bookings");
        
        // Room related errors
        ERROR_MESSAGES.put(ROOM_NOT_FOUND, "Room not found");
        ERROR_MESSAGES.put(ROOM_NUMBER_EXISTS, "Room number already exists");
        ERROR_MESSAGES.put(ROOM_TYPE_NOT_FOUND, "Room type not found");
        ERROR_MESSAGES.put(ROOM_HAS_BOOKINGS, "Cannot delete room with active bookings");
        ERROR_MESSAGES.put(ROOM_NOT_FOUND_BY_NUMBER, "Room not found with number");
        
        // Room type related errors
        ERROR_MESSAGES.put(INVALID_PRICE, "Price must be greater than zero");
        ERROR_MESSAGES.put(ROOM_TYPE_HAS_ROOMS, "Cannot delete room type that has rooms assigned to it");
        ERROR_MESSAGES.put(INVALID_IMAGE_FORMAT, "Invalid image format. Only JPG or PNG files are allowed");
        ERROR_MESSAGES.put(REQUIRED_FIELD_UNIQUE, "There is already a room type with the same name");
        
        // Booking related errors
        ERROR_MESSAGES.put(BOOKING_NOT_FOUND, "Booking not found");
        ERROR_MESSAGES.put(INVALID_DATE_RANGE, "Invalid date range. Check-out date must be after check-in date");
        ERROR_MESSAGES.put(ROOM_NOT_AVAILABLE, "Room is not available for the selected dates");
        ERROR_MESSAGES.put(INVALID_STATUS_TRANSITION, "Invalid status transition");
        ERROR_MESSAGES.put(CUSTOMER_NOT_FOUND, "Customer not found");
        ERROR_MESSAGES.put(CUSTOMER_ID_REQUIRED, "Customer ID is required");
        ERROR_MESSAGES.put(NOT_ENOUGH_ROOMS, "Not enough rooms available for the selected type");
        ERROR_MESSAGES.put(WRONG_BOOKING_STATUS, "Booking must be in correct status to perform this action");
        ERROR_MESSAGES.put(EARLY_CHECKIN, "Cannot check in before the scheduled check-in date");
        ERROR_MESSAGES.put(NO_ACTIVE_BOOKING, "No active booking found for this room");
        
        // Customer related errors
        ERROR_MESSAGES.put(CUSTOMER_PHONE_EXISTS, "Customer with this phone already exists");
        ERROR_MESSAGES.put(CUSTOMER_EMAIL_EXISTS, "Customer with this email already exists");
    }
    
    /**
     * Get the error message for a given error code
     * @param code the error code
     * @return the corresponding error message
     */
    public static String getMessage(String code) {
        return ERROR_MESSAGES.getOrDefault(code, "An unknown error occurred");
    }
} 