package com.HotelManagement.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.HotelManagement.models.RoomType;

@Repository
public interface RoomTypeRepository extends JpaRepository<RoomType, Integer> {
    RoomType findByName(String name);
}