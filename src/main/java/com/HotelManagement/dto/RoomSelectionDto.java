package com.HotelManagement.dto;

public class RoomSelectionDto {
    private Integer roomTypeId;
    private Integer count;
    private Integer roomId; // Added for direct room selection
    
    public Integer getRoomTypeId() {
        return roomTypeId;
    }
    
    public void setRoomTypeId(Integer roomTypeId) {
        this.roomTypeId = roomTypeId;
    }
    
    public Integer getCount() {
        return count;
    }
    
    public void setCount(Integer count) {
        this.count = count;
    }
    
    public Integer getRoomId() {
        return roomId;
    }
    
    public void setRoomId(Integer roomId) {
        this.roomId = roomId;
    }
}