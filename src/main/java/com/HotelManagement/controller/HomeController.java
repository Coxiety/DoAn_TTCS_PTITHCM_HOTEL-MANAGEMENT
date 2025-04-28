package com.HotelManagement.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

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
        
        // If user is logged in as receptionist (role_id = 2), redirect to receptionist page
        if (userRole != null && userRole == 2) {
            return "redirect:/receptionist";
        }
        
        // If user is logged in as admin (role_id = 1), redirect to admin page
        if (userRole != null && userRole == 1) {
            return "redirect:/admin";
        }
        
        // Otherwise show regular home page
        return "homepage/HomePage";
    }

    @GetMapping("/contact")
    public String contactPage(HttpSession session) 
    {
        // Check if user is logged in
        Integer userRole = (Integer) session.getAttribute("userRole");
        
        // If user is logged in as receptionist, redirect to receptionist page
        if (userRole != null && Integer.valueOf(2).equals(userRole)) {
            return "redirect:/receptionist";
        }
        
        // If user is logged in as admin (role_id = 1), redirect to admin page
        if (userRole != null && Integer.valueOf(1).equals(userRole)) {
            return "redirect:/admin";
        }
        
        return "homepage/ContactPage";
    }
    
    @GetMapping("/intro")
    public String introductionPage(HttpSession session) 
    {
        // Check if user is logged in
        Integer userRole = (Integer) session.getAttribute("userRole");
        
        // If user is logged in as receptionist, redirect to receptionist page
        if (userRole != null && Integer.valueOf(2).equals(userRole)) {
            return "redirect:/receptionist";
        }
        
        // If user is logged in as admin (role_id = 1), redirect to admin page
        if (userRole != null && Integer.valueOf(1).equals(userRole)) {
            return "redirect:/admin";
        }
        
        return "homepage/IntroductionPage";
    }
}
