package com.HotelManagement.dto;

public class RoomTypeAmenityDto {
    private String icon;
    private String name;
    
    public RoomTypeAmenityDto() {
    }
    
    public RoomTypeAmenityDto(String icon, String name) {
        this.icon = icon;
        this.name = name;
    }
    
    public String getIcon() {
        return icon;
    }
    
    public void setIcon(String icon) {
        this.icon = icon;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
}