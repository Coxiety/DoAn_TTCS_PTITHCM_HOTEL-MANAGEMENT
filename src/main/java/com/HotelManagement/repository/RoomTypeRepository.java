package com.HotelManagement.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.HotelManagement.models.RoomType;

@Repository
public interface RoomTypeRepository extends JpaRepository<RoomType, Integer> {
    RoomType findByName(String name);
    
    @Query("SELECT rt FROM RoomType rt WHERE rt.capacity >= :minCapacity")
    List<RoomType> findByCapacityGreaterThanEqual(@Param("minCapacity") Integer minCapacity);
    
    @Query("SELECT rt FROM RoomType rt WHERE rt.price BETWEEN :minPrice AND :maxPrice")
    List<RoomType> findByPriceBetween(@Param("minPrice") BigDecimal minPrice, @Param("maxPrice") BigDecimal maxPrice);
    
    @Query("SELECT rt FROM RoomType rt WHERE rt.amenities LIKE %:amenities%")
    List<RoomType> findByAmenitiesContaining(@Param("amenities") String amenities);
}