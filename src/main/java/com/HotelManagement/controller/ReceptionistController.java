package com.HotelManagement.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.HotelManagement.models.Booking;
import com.HotelManagement.models.BookingDetail;
import com.HotelManagement.models.Customer;
import com.HotelManagement.models.Room;
import com.HotelManagement.models.User;
import com.HotelManagement.repository.BookingDetailRepository;
import com.HotelManagement.service.BookingService;
import com.HotelManagement.service.CustomerService;
import com.HotelManagement.service.RoomService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/receptionist")
public class ReceptionistController {

    @Autowired
    private BookingService bookingService;
    
    @Autowired
    private CustomerService customerService;
    
    @Autowired
    private RoomService roomService;
    
    @Autowired
    private BookingDetailRepository bookingDetailRepository;
    
    @GetMapping("")
    public String dashboard(Model model, HttpSession session) {
        // Check if user is logged in and has receptionist role
        User user = (User) session.getAttribute("user");
        Integer userRole = (Integer) session.getAttribute("userRole");
        
        if (user == null || userRole == null || userRole != 1) {
            return "redirect:/";
        }
        
        // Get today's bookings for check-in
        List<Booking> todaysCheckins = bookingService.getTodaysCheckins();
        
        // For each booking, fetch its details and add them to the model
        Map<Integer, List<BookingDetail>> bookingDetailsMap = new HashMap<>();
        for (Booking booking : todaysCheckins) {
            List<BookingDetail> details = bookingDetailRepository.findByBookingId(booking.getId());
            bookingDetailsMap.put(booking.getId(), details);
        }
        
        model.addAttribute("checkins", todaysCheckins);
        model.addAttribute("bookingDetailsMap", bookingDetailsMap);
        
        // Get occupied rooms for check-out
        List<Room> occupiedRooms = roomService.getOccupiedRooms();
        model.addAttribute("occupiedRooms", occupiedRooms);
        
        // Get available rooms
        List<Room> availableRooms = roomService.getAvailableRooms();
        model.addAttribute("availableRooms", availableRooms);
        
        return "ReceptionistPage";
    }
    
    @PostMapping("/check-in")
    public String checkIn(@RequestParam Integer bookingId, RedirectAttributes redirectAttributes) {
        try {
            bookingService.checkInBooking(bookingId);
            redirectAttributes.addFlashAttribute("success", "Check-in successful!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Check-in failed: " + e.getMessage());
        }
        return "redirect:/receptionist";
    }
    
    @PostMapping("/check-out")
    public String checkOut(@RequestParam Integer roomId, RedirectAttributes redirectAttributes) {
        try {
            bookingService.checkOutRoom(roomId);
            redirectAttributes.addFlashAttribute("success", "Check-out successful!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Check-out failed: " + e.getMessage());
        }
        return "redirect:/receptionist";
    }
    
    @PostMapping("/create-customer")
    public String createCustomer(
            @RequestParam String fullName,
            @RequestParam String phone,
            @RequestParam String email,
            @RequestParam(required = false) String address,
            RedirectAttributes redirectAttributes) {
        
        try {
            Customer newCustomer = new Customer();
            newCustomer.setFullName(fullName);
            newCustomer.setPhone(phone);
            newCustomer.setEmail(email);
            newCustomer.setAddress(address);
            
            customerService.saveCustomer(newCustomer);
            redirectAttributes.addFlashAttribute("success", "Customer created successfully!");
            redirectAttributes.addFlashAttribute("customerId", newCustomer.getId());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to create customer: " + e.getMessage());
        }
        
        return "redirect:/receptionist";
    }
    
    @PostMapping("/create-booking")
    public String createBooking(
            @RequestParam Integer customerId,
            @RequestParam Integer roomId,
            @RequestParam String checkInDate,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        
        try {
            User user = (User) session.getAttribute("user");
            
            bookingService.createBookingByReceptionist(customerId, roomId, checkInDate, user.getId());
            redirectAttributes.addFlashAttribute("success", "Booking created successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to create booking: " + e.getMessage());
        }
        
        return "redirect:/receptionist";
    }
    
    @GetMapping("/search-customer")
    public String searchCustomer(@RequestParam String query, Model model) {
        List<Customer> customers = customerService.searchCustomers(query);
        model.addAttribute("customers", customers);
        return "fragments/customer-search-results :: results";
    }
}