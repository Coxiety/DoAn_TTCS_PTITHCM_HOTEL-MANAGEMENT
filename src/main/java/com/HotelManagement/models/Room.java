package com.HotelManagement.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table(name = "Rooms")
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "room_number", nullable = false, unique = true, length = 50)
    private String roomNumber;
    
    @ManyToOne
    @JoinColumn(name = "type_id", nullable = false)
    private RoomType roomType;
    
    @Column(nullable = false, length = 50)
    private String status;
    
    // Constructors
    public Room() {
    }
    
    public Room(String roomNumber, RoomType roomType, String status) {
        this.roomNumber = roomNumber;
        this.roomType = roomType;
        this.status = status;
    }
    
    // Getters and Setters
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public String getRoomNumber() {
        return roomNumber;
    }
    
    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }
    
    public RoomType getRoomType() {
        return roomType;
    }
    
    public void setRoomType(RoomType roomType) {
        this.roomType = roomType;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    /**
     * Gets the image path from the room type.
     * Using @Transient to indicate this is not a database column.
     * 
     * @return the image path from the room type
     */
    @Transient
    public String getImagePath() {
        return roomType != null ? roomType.getImagePath() : null;
    }
}