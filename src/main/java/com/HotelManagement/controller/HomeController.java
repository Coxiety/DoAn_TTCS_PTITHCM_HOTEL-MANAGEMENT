package com.HotelManagement.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.HotelManagement.models.RoomType;
import com.HotelManagement.service.RoomService;

import jakarta.servlet.http.HttpSession;

@Controller
public class HomeController 
{
    @Autowired
    private RoomService roomService;

    @GetMapping({"/", "/hotelmanagement/home"})
    public String homePage(HttpSession session) 
    {
        // Check if user is logged in
        Integer userRole = (Integer) session.getAttribute("userRole");
        
        // If user is logged in as receptionist (role_id = 1), redirect to receptionist page
        if (userRole != null && userRole == 1) {
            return "redirect:/receptionist";
        }
        
        // If user is logged in as admin (role_id = 3), redirect to admin page
        if (userRole != null && userRole == 3) {
            return "redirect:/admin";
        }
        
        // Otherwise show regular home page
        return "HomePage";
    }

    @GetMapping("/booking")
    public String bookingPage(Model model, HttpSession session) 
    {
        // Check if user is logged in
        Integer userRole = (Integer) session.getAttribute("userRole");
        
        // If user is logged in as receptionist, redirect to receptionist page
        if (userRole != null && userRole == 1) {
            return "redirect:/receptionist";
        }
        
        // If user is logged in as admin (role_id = 3), redirect to admin page
        if (userRole != null && userRole == 3) {
            return "redirect:/admin";
        }
        
        // Get all room types with their available rooms
        List<RoomType> roomTypes = roomService.getAllRoomTypes();
        
        // Get available rooms count for each room type
        Map<Integer, Long> availableRoomCounts = roomService.getAllRooms().stream()
            .filter(room -> "AVAILABLE".equals(room.getStatus()))
            .collect(Collectors.groupingBy(
                room -> room.getRoomType().getId(),
                Collectors.counting()
            ));
            
        // Add data to model
        model.addAttribute("roomTypes", roomTypes);
        model.addAttribute("availableRoomCounts", availableRoomCounts);
        
        return "BookingPage";
    }
    
    @GetMapping("/contract")
    public String contractPage(HttpSession session) 
    {
        // Check if user is logged in
        Integer userRole = (Integer) session.getAttribute("userRole");
        
        // If user is logged in as receptionist, redirect to receptionist page
        if (userRole != null && userRole == 1) {
            return "redirect:/receptionist";
        }
        
        // If user is logged in as admin (role_id = 3), redirect to admin page
        if (userRole != null && userRole == 3) {
            return "redirect:/admin";
        }
        
        return "ContractPage";
    }
    
    @GetMapping("/intro")
    public String introductionPage(HttpSession session) 
    {
        // Check if user is logged in
        Integer userRole = (Integer) session.getAttribute("userRole");
        
        // If user is logged in as receptionist, redirect to receptionist page
        if (userRole != null && userRole == 1) {
            return "redirect:/receptionist";
        }
        
        // If user is logged in as admin (role_id = 3), redirect to admin page
        if (userRole != null && userRole == 3) {
            return "redirect:/admin";
        }
        
        return "IntroductionPage";
    }
}
