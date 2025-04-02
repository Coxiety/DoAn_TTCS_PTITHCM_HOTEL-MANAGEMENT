package com.HotelManagement.dto;

import java.math.BigDecimal;
import java.util.List;

import com.HotelManagement.models.Room;

public class RoomTypeDto {
    private Integer id;
    private String name;
    private String description;
    private Integer capacity;
    private BigDecimal price;
    private String amenities;
    private String imagePath;
    
    // Additional fields for the UI
    private String bedType;
    private String size;
    private List<RoomTypeAmenityDto> amenityList;
    private List<Room> availableRooms;
    private Integer availableCount;

    // Constructors
    public RoomTypeDto() {
    }

    public RoomTypeDto(Integer id, String name, String description, Integer capacity, BigDecimal price, String amenities, String imagePath) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.capacity = capacity;
        this.price = price;
        this.amenities = amenities;
        this.imagePath = imagePath;
    }

    // Getters and setters
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

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getAmenities() {
        return amenities;
    }

    public void setAmenities(String amenities) {
        this.amenities = amenities;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    // Additional getters and setters for UI fields
    public String getBedType() {
        return bedType;
    }

    public void setBedType(String bedType) {
        this.bedType = bedType;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public List<RoomTypeAmenityDto> getAmenityList() {
        return amenityList;
    }

    public void setAmenityList(List<RoomTypeAmenityDto> amenityList) {
        this.amenityList = amenityList;
    }

    public List<Room> getAvailableRooms() {
        return availableRooms;
    }

    public void setAvailableRooms(List<Room> availableRooms) {
        this.availableRooms = availableRooms;
    }

    public Integer getAvailableCount() {
        return availableCount;
    }

    public void setAvailableCount(Integer availableCount) {
        this.availableCount = availableCount;
    }
}