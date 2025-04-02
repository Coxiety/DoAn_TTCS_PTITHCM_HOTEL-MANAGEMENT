package com.HotelManagement.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpSession;

@Controller
public class HomeController 
{
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
    public String bookingPage(HttpSession session) 
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
