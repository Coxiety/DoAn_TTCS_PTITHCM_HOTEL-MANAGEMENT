package com.HotelManagement.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController 
{
    @GetMapping({"/", "/hotelmanagement/home"})
    public String homePage() 
    {
        return "HomePage";
    }

    @GetMapping("/booking")
    public String bookingPage() 
    {
        return "BookingPage";
    }
    
    @GetMapping("/contract")
    public String contractPage() 
    {
        return "ContractPage";
    }
    
    @GetMapping("/intro")
    public String introductionPage() 
    {
        return "IntroductionPage";
    }
}
