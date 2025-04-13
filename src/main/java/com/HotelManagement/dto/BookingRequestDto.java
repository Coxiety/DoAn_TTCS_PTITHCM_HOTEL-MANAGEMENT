package com.HotelManagement.dto;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;

public class BookingRequestDto {
    
    @NotBlank(message = "Check-in date is required")
    @Pattern(regexp = "\\d{2}-\\d{2}-\\d{4}", message = "Check-in date must be in format DD-MM-YYYY")
    private String checkInDate;
    
    @NotBlank(message = "Check-out date is required")
    @Pattern(regexp = "\\d{2}-\\d{2}-\\d{4}", message = "Check-out date must be in format DD-MM-YYYY")
    private String checkOutDate;
    
    private Integer customerId;
    
    @NotEmpty(message = "At least one room selection is required")
    private List<RoomSelectionDto> roomSelections;

    // Getters and Setters
    public String getCheckInDate() {
        return checkInDate;
    }

    public void setCheckInDate(String checkInDate) {
        this.checkInDate = checkInDate;
    }

    public String getCheckOutDate() {
        return checkOutDate;
    }

    public void setCheckOutDate(String checkOutDate) {
        this.checkOutDate = checkOutDate;
    }

    public Integer getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Integer customerId) {
        this.customerId = customerId;
    }

    public List<RoomSelectionDto> getRoomSelections() {
        return roomSelections;
    }

    public void setRoomSelections(List<RoomSelectionDto> roomSelections) {
        this.roomSelections = roomSelections;
    }
}