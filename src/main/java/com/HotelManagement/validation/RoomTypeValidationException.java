package com.HotelManagement.validation;

public class RoomTypeValidationException extends RuntimeException {
    private final RoomTypeValidationError error;

    public RoomTypeValidationException(RoomTypeValidationError error) {
        super(error.getMessage());
        this.error = error;
    }

    public RoomTypeValidationError getError() {
        return error;
    }
} 