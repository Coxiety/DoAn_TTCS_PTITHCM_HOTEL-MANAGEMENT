package com.HotelManagement.controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import com.HotelManagement.dto.BookingRequestDto;
import com.HotelManagement.models.Booking;
import com.HotelManagement.models.BookingDetail;
import com.HotelManagement.models.Customer;
import com.HotelManagement.models.User;
import com.HotelManagement.repository.CustomerRepository;
import com.HotelManagement.service.BookingDetailService;
import com.HotelManagement.service.BookingService;

import jakarta.servlet.http.HttpSession;

@Controller
public class BookingController {
    
    @Autowired
    private BookingService bookingService;
    
    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private BookingDetailService bookingDetailService;

    @PostMapping("/api/booking/create")
    @ResponseBody
    public ResponseEntity<?> createBooking(@RequestBody BookingRequestDto bookingRequest, HttpSession session) {
        // Get user from session
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Please login to make a booking"));
        }
        
        try {
            // Find customer by phone number
            Customer customer = customerRepository.findByPhone(user.getPhone());
            
            if (customer == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "Customer record not found. Please contact reception."));
            }
            
            // Set the customer ID in the booking request
            bookingRequest.setCustomerId(customer.getId());
            
            Booking booking = bookingService.createBooking(bookingRequest, user);
            return ResponseEntity.ok(Map.of(
                "bookingId", booking.getId(),
                "message", "Booking created successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/booking/confirmation/{id}")
    public String showBookingConfirmation(@PathVariable("id") Integer bookingId, Model model) {
        // Get the booking
        Booking booking = bookingService.getBookingById(bookingId);
        if (booking == null) {
            return "redirect:/booking?error=Booking not found";
        }

        // Get booking details
        List<BookingDetail> bookingDetails = bookingDetailService.getBookingDetailsByBookingId(bookingId);
        
        // Calculate total amount
        BigDecimal totalAmount = bookingDetails.stream()
                .map(BookingDetail::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Add to model
        model.addAttribute("booking", booking);
        model.addAttribute("bookingDetails", bookingDetails);
        model.addAttribute("totalAmount", totalAmount);

        return "BookingConfirmation";
    }
}
