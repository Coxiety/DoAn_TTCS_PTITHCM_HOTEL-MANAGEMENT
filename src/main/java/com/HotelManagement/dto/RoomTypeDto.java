package com.HotelManagement.dto;

import java.math.BigDecimal;

public class RoomTypeDto {
    private Integer id;
    private String name;
    private String description;
    private Integer capacity;
    private BigDecimal price;
    private String amenities;
    private String imagePath;

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
}