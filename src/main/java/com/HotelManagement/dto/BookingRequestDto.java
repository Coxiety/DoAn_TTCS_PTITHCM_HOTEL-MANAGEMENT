package com.HotelManagement.dto;

import java.util.List;

public class BookingRequestDto {
    private Integer customerId;
    private String checkInDate;
    private List<RoomSelectionDto> roomSelections;
    
    public Integer getCustomerId() {
        return customerId;
    }
    
    public void setCustomerId(Integer customerId) {
        this.customerId = customerId;
    }
    
    public String getCheckInDate() {
        return checkInDate;
    }
    
    public void setCheckInDate(String checkInDate) {
        this.checkInDate = checkInDate;
    }
    
    public List<RoomSelectionDto> getRoomSelections() {
        return roomSelections;
    }
    
    public void setRoomSelections(List<RoomSelectionDto> roomSelections) {
        this.roomSelections = roomSelections;
    }
}