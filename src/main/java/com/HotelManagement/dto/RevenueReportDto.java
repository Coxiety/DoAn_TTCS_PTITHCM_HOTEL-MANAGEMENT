package com.HotelManagement.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class RevenueReportDto {
    private BigDecimal totalRevenue;
    private Integer totalBookings;
    private Integer totalRoomsBooked;
    private Map<String, BigDecimal> revenueByRoomType;
    private Map<String, Integer> bookingsByRoomType;
    private List<DailyRevenueDto> dailyRevenue;
    
    // Default constructor
    public RevenueReportDto() {
    }

    // Getters and setters
    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public Integer getTotalBookings() {
        return totalBookings;
    }

    public void setTotalBookings(Integer totalBookings) {
        this.totalBookings = totalBookings;
    }

    public Integer getTotalRoomsBooked() {
        return totalRoomsBooked;
    }

    public void setTotalRoomsBooked(Integer totalRoomsBooked) {
        this.totalRoomsBooked = totalRoomsBooked;
    }

    public Map<String, BigDecimal> getRevenueByRoomType() {
        return revenueByRoomType;
    }

    public void setRevenueByRoomType(Map<String, BigDecimal> revenueByRoomType) {
        this.revenueByRoomType = revenueByRoomType;
    }

    public Map<String, Integer> getBookingsByRoomType() {
        return bookingsByRoomType;
    }

    public void setBookingsByRoomType(Map<String, Integer> bookingsByRoomType) {
        this.bookingsByRoomType = bookingsByRoomType;
    }

    public List<DailyRevenueDto> getDailyRevenue() {
        return dailyRevenue;
    }

    public void setDailyRevenue(List<DailyRevenueDto> dailyRevenue) {
        this.dailyRevenue = dailyRevenue;
    }

    // Inner class for daily revenue breakdown
    public static class DailyRevenueDto {
        private String date;
        private BigDecimal revenue;
        private Integer bookingsCount;
        
        public DailyRevenueDto(String date, BigDecimal revenue, Integer bookingsCount) {
            this.date = date;
            this.revenue = revenue;
            this.bookingsCount = bookingsCount;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public BigDecimal getRevenue() {
            return revenue;
        }

        public void setRevenue(BigDecimal revenue) {
            this.revenue = revenue;
        }

        public Integer getBookingsCount() {
            return bookingsCount;
        }

        public void setBookingsCount(Integer bookingsCount) {
            this.bookingsCount = bookingsCount;
        }
    }
}