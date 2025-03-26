package com.HotelManagement.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class BookingController {

    @GetMapping("/hotelmanagement/booking")
    public String bookingPage() {
        // This will return the Thymeleaf template "BookingPage.html"
        return "BookingPage";
    }
}
