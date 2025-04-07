package com.HotelManagement.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.HotelManagement.dto.RevenueReportDto;
import com.HotelManagement.dto.RevenueReportDto.DailyRevenueDto;
import com.HotelManagement.models.Booking;
import com.HotelManagement.models.BookingDetail;
import com.HotelManagement.models.Room;
import com.HotelManagement.models.RoomType;
import com.HotelManagement.repository.BookingDetailRepository;
import com.HotelManagement.repository.BookingRepository;
import com.HotelManagement.repository.RoomRepository;
import com.HotelManagement.repository.UserRepository;

@Service
public class AdminService {
    
    private final String UPLOAD_DIR = "src/main/resources/static/uploads/";
    
    @Autowired
    private BookingRepository bookingRepository;
    
    @Autowired
    private BookingDetailRepository bookingDetailRepository;
    
    @Autowired
    private RoomRepository roomRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    public Map<String, Object> getDashboardSummary() {
        Map<String, Object> summary = new HashMap<>();
        
        // Count of users by role
        long receptionistCount = userRepository.countByRoleId(1);
        long customerCount = userRepository.countByRoleId(2);
        long adminCount = userRepository.countByRoleId(3);
        
        // Room stats
        long totalRooms = roomRepository.count();
        long availableRooms = roomRepository.findByStatus("AVAILABLE").size();
        long occupiedRooms = roomRepository.findByStatus("OCCUPIED").size();
        long bookedRooms = roomRepository.findByStatus("BOOKED").size();
        
        // Add to summary
        summary.put("receptionistCount", receptionistCount);
        summary.put("customerCount", customerCount);
        summary.put("adminCount", adminCount);
        summary.put("totalRooms", totalRooms);
        summary.put("availableRooms", availableRooms);
        summary.put("occupiedRooms", occupiedRooms);
        summary.put("bookedRooms", bookedRooms);
        
        return summary;
    }
    
    /**
     * Save a room type image to the img/rooms directory
     */
    public String saveRoomTypeImage(MultipartFile file, String roomTypeName, Integer roomTypeId) throws IOException {
        // Create directory if it doesn't exist
        Path uploadPath = Paths.get("src/main/resources/static/img/rooms");
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        // Generate a filename based on room type
        String extension = getFileExtension(file.getOriginalFilename());
        String filename = "roomtype-" + roomTypeId + "-" + roomTypeName.replaceAll("\\s+", "-").toLowerCase() + extension;
        Path filePath = uploadPath.resolve(filename);
        
        // Save the file
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        
        // Return the relative path
        return "/img/rooms/" + filename;
    }
    
    /**
     * Get file extension from filename
     */
    private String getFileExtension(String filename) {
        if (filename == null) {
            return "";
        }
        int lastDotIndex = filename.lastIndexOf(".");
        if (lastDotIndex == -1) {
            return ""; // No extension
        }
        return filename.substring(lastDotIndex);
    }
    
    /**
     * Generate a revenue report for a given date range
     */
    public RevenueReportDto getRevenueReport(LocalDate startDate, LocalDate endDate) {
        // Validate date range
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Start date and end date cannot be null");
        }
        
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date cannot be after end date");
        }
        
        // Find all completed bookings in the date range
        List<Booking> bookings = bookingRepository.findAll().stream()
                .filter(b -> "COMPLETED".equals(b.getStatus()))
                .filter(b -> {
                    LocalDate bookingDate = b.getCheckInDate().toLocalDate();
                    return !bookingDate.isBefore(startDate) && !bookingDate.isAfter(endDate);
                })
                .collect(Collectors.toList());
        
        // Initialize the DTO
        RevenueReportDto report = new RevenueReportDto();
        report.setTotalBookings(bookings.size());
        
        // Calculate total revenue and rooms booked
        BigDecimal totalRevenue = BigDecimal.ZERO;
        int totalRoomsBooked = 0;
        
        // Maps for storing data by room type
        Map<String, BigDecimal> revenueByRoomType = new HashMap<>();
        Map<String, Integer> bookingsByRoomType = new HashMap<>();
        
        // Maps for daily data
        Map<LocalDate, BigDecimal> dailyRevenue = new HashMap<>();
        Map<LocalDate, Integer> dailyBookingsCount = new HashMap<>();
        
        // Process each booking
        for (Booking booking : bookings) {
            List<BookingDetail> details = bookingDetailRepository.findByBookingId(booking.getId());
            LocalDate bookingDate = booking.getCheckInDate().toLocalDate();
            
            // Update daily counters
            dailyBookingsCount.put(
                    bookingDate, 
                    dailyBookingsCount.getOrDefault(bookingDate, 0) + 1);
            
            // Process each booked room
            for (BookingDetail detail : details) {
                BigDecimal roomPrice = detail.getPrice();
                Room room = detail.getRoom();
                RoomType roomType = room.getRoomType();
                String typeName = roomType.getName();
                
                // Update totals
                totalRevenue = totalRevenue.add(roomPrice);
                totalRoomsBooked++;
                
                // Update by room type
                revenueByRoomType.put(
                        typeName, 
                        revenueByRoomType.getOrDefault(typeName, BigDecimal.ZERO).add(roomPrice));
                
                bookingsByRoomType.put(
                        typeName, 
                        bookingsByRoomType.getOrDefault(typeName, 0) + 1);
                
                // Update daily revenue
                dailyRevenue.put(
                        bookingDate, 
                        dailyRevenue.getOrDefault(bookingDate, BigDecimal.ZERO).add(roomPrice));
            }
        }
        
        // Set totals in report
        report.setTotalRevenue(totalRevenue);
        report.setTotalRoomsBooked(totalRoomsBooked);
        report.setRevenueByRoomType(revenueByRoomType);
        report.setBookingsByRoomType(bookingsByRoomType);
        
        // Set daily breakdown
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        List<DailyRevenueDto> dailyData = new ArrayList<>();
        
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            String formattedDate = date.format(formatter);
            BigDecimal revenue = dailyRevenue.getOrDefault(date, BigDecimal.ZERO);
            Integer bookingsCount = dailyBookingsCount.getOrDefault(date, 0);
            
            dailyData.add(new DailyRevenueDto(formattedDate, revenue, bookingsCount));
        }
        
        report.setDailyRevenue(dailyData);
        
        return report;
    }
}