package com.HotelManagement.dto;

public class RoomSelectionDto {
    private Integer roomTypeId;
    private Integer count;
    
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
}