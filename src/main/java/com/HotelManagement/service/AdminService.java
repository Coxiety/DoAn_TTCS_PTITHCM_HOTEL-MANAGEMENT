package com.HotelManagement.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.HotelManagement.repository.RoomRepository;
import com.HotelManagement.repository.UserRepository;

@Service
public class AdminService {
    
    @Autowired
    private RoomRepository roomRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    public Map<String, Object> getDashboardSummary() {
        Map<String, Object> summary = new HashMap<>();
        
        // Count of users by role
        long adminCount = userRepository.countByRoleId(1);
        long receptionistCount = userRepository.countByRoleId(2);
        long customerCount = userRepository.countByRoleId(0);
        
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
}