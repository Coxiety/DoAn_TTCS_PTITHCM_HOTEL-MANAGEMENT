package com.HotelManagement.dto;

import java.util.List;

import com.HotelManagement.models.Room;

public class RoomTypeDto {
    private Integer id;
    private String name;
    private String description;
    private Integer capacity;
    private String size;
    private String bedType;
    private Integer price;
    private Integer availableCount;
    private List<Room> availableRooms;
    private List<RoomTypeAmenityDto> amenities;
    
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Integer getCapacity() {
        return capacity;
    }
    
    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }
    
    public String getSize() {
        return size;
    }
    
    public void setSize(String size) {
        this.size = size;
    }
    
    public String getBedType() {
        return bedType;
    }
    
    public void setBedType(String bedType) {
        this.bedType = bedType;
    }
    
    public Integer getPrice() {
        return price;
    }
    
    public void setPrice(Integer price) {
        this.price = price;
    }
    
    public Integer getAvailableCount() {
        return availableCount;
    }
    
    public void setAvailableCount(Integer availableCount) {
        this.availableCount = availableCount;
    }
    
    public List<Room> getAvailableRooms() {
        return availableRooms;
    }
    
    public void setAvailableRooms(List<Room> availableRooms) {
        this.availableRooms = availableRooms;
    }
    
    public List<RoomTypeAmenityDto> getAmenities() {
        return amenities;
    }
    
    public void setAmenities(List<RoomTypeAmenityDto> amenities) {
        this.amenities = amenities;
    }
}