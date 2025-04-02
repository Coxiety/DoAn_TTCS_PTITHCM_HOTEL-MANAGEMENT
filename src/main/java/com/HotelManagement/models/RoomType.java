package com.HotelManagement.models;

import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "Room_Types")
public class RoomType {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(nullable = false, length = 100)
    private String name;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column
    private Integer capacity;
    
    @Column(columnDefinition = "TEXT")
    private String amenities;
    
    @Column(name = "image_path")
    private String imagePath;
    
    @OneToMany(mappedBy = "roomType")
    private List<Room> rooms;
    
    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
    
    public String getImagePath() {
        return this.imagePath;
    }
}