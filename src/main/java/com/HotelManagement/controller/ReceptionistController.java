package com.HotelManagement.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.HotelManagement.dto.BookingRequestDto;
import com.HotelManagement.dto.RoomTypeAmenityDto;
import com.HotelManagement.dto.RoomTypeDto;
import com.HotelManagement.models.Booking;
import com.HotelManagement.models.Customer;
import com.HotelManagement.models.Room;
import com.HotelManagement.models.RoomType;
import com.HotelManagement.models.User;
import com.HotelManagement.repository.BookingDetailRepository;
import com.HotelManagement.repository.RoomTypeRepository;
import com.HotelManagement.service.BookingService;
import com.HotelManagement.service.CustomerService;
import com.HotelManagement.service.RoomService;
import com.fasterxml.jackson.databind.ObjectMapper;

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
    
    @Autowired
    private RoomTypeRepository roomTypeRepository;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    @GetMapping("")
    public String dashboard(Model model, HttpSession session) {
        // Check if user is logged in and has receptionist role
        User user = (User) session.getAttribute("user");
        Integer userRole = (Integer) session.getAttribute("userRole");
        
        if (user == null || userRole == null || userRole != 1) {
            return "redirect:/";
        }
        
        // Get all rooms for availability display
        List<Room> availableRooms = roomService.getAvailableRooms();
        model.addAttribute("availableRooms", availableRooms);
        
        List<Room> occupiedRooms = roomService.getOccupiedRooms();
        model.addAttribute("occupiedRooms", occupiedRooms);
        
        return "ReceptionistPage";
    }
    
    @GetMapping("/get-all-customers")
    @ResponseBody
    public List<Customer> getAllCustomers() {
        return customerService.getAllCustomers();
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
    
    @GetMapping("/search-customer")
    public String searchCustomer(@RequestParam String query, Model model) {
        List<Customer> customers = customerService.searchCustomers(query);
        model.addAttribute("customers", customers);
        return "fragments/customer-search-results :: results";
    }
    
    /**
     * Show the new booking page for receptionists 
     */
    @GetMapping("/book/{customerId}")
    public String showBookingPage(@PathVariable Integer customerId, Model model, HttpSession session) {
        // Check if user is logged in and has receptionist role
        User user = (User) session.getAttribute("user");
        Integer userRole = (Integer) session.getAttribute("userRole");
        
        if (user == null || userRole == null || userRole != 1) {
            return "redirect:/";
        }
        
        // Get the selected customer
        Customer selectedCustomer;
        try {
            selectedCustomer = customerService.getCustomerById(customerId);
        } catch (Exception e) {
            return "redirect:/receptionist";
        }
        
        // Get all room types with their available rooms
        List<RoomType> roomTypes = roomTypeRepository.findAll();
        List<RoomTypeDto> roomTypeDtos = new ArrayList<>();
        
        for (RoomType roomType : roomTypes) {
            RoomTypeDto dto = new RoomTypeDto();
            dto.setId(roomType.getId());
            dto.setName(roomType.getName());
            dto.setDescription(roomType.getDescription());
            dto.setCapacity(roomType.getCapacity());
            dto.setPrice(roomType.getPrice());  // Get price directly from room type
            
            // Set bed type based on capacity (this is a simplified example)
            switch (roomType.getCapacity()) {
                case 2:
                    dto.setBedType("Queen Bed");
                    dto.setSize("28");
                    break;
                case 3:
                    dto.setBedType("King Bed");
                    dto.setSize("35");
                    break;
                case 4:
                    dto.setBedType("King Bed + Sofa Bed");
                    dto.setSize("50");
                    break;
                default:
                    dto.setBedType("Twin Beds");
                    dto.setSize("25");
            }
            
            // Get available rooms of this type
            List<Room> rooms = roomService.getAvailableRoomsByType(roomType.getId());
            dto.setAvailableRooms(rooms);
            dto.setAvailableCount(rooms.size());
            
            // Add amenities
            List<RoomTypeAmenityDto> amenities = new ArrayList<>();
            
            // If roomType has amenities string, parse it and add to amenities list
            if (roomType.getAmenities() != null && !roomType.getAmenities().isEmpty()) {
                String[] amenityList = roomType.getAmenities().split(",");
                for (String amenity : amenityList) {
                    String icon = getIconForAmenity(amenity.trim());
                    amenities.add(new RoomTypeAmenityDto(icon, amenity.trim()));
                }
            } else {
                // Default amenities if none are specified
                amenities.add(new RoomTypeAmenityDto("fas fa-wifi", "Free Wi-Fi"));
                amenities.add(new RoomTypeAmenityDto("fas fa-tv", "Flat-screen TV"));
                amenities.add(new RoomTypeAmenityDto("fas fa-snowflake", "Air Conditioning"));
                amenities.add(new RoomTypeAmenityDto("fas fa-shower", "Private Bathroom"));
            }
            
            dto.setAmenityList(amenities);
            roomTypeDtos.add(dto);
        }
        
        // Create JSON representation of room types for JavaScript
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String roomTypesJson = objectMapper.writeValueAsString(
                roomTypeDtos.stream()
                    .collect(Collectors.toMap(
                        dto -> dto.getName().toLowerCase(), 
                        dto -> dto
                    ))
            );
            model.addAttribute("roomTypesJson", roomTypesJson);
        } catch (Exception e) {
            model.addAttribute("roomTypesJson", "{}");
        }
        
        model.addAttribute("selectedCustomer", selectedCustomer);
        model.addAttribute("roomTypes", roomTypeDtos);
        
        return "ReceptionistBookingPage";
    }
    
    /**
     * Helper method to get Font Awesome icon for an amenity
     */
    private String getIconForAmenity(String amenity) {
        amenity = amenity.toLowerCase();
        
        if (amenity.contains("wifi")) return "fas fa-wifi";
        if (amenity.contains("tv")) return "fas fa-tv";
        if (amenity.contains("air")) return "fas fa-snowflake";
        if (amenity.contains("bath")) return "fas fa-bath";
        if (amenity.contains("shower")) return "fas fa-shower";
        if (amenity.contains("coffee")) return "fas fa-coffee";
        if (amenity.contains("kitchen")) return "fas fa-utensils";
        if (amenity.contains("living")) return "fas fa-couch";
        if (amenity.contains("service")) return "fas fa-concierge-bell";
        if (amenity.contains("mini")) return "fas fa-glass-whiskey";
        
        // Default icon
        return "fas fa-check";
    }
    
    /**
     * Process a booking with the updated structure
     */
    @PostMapping("/create-booking-full")
    @ResponseBody
    public ResponseEntity<?> createFullBooking(@RequestBody BookingRequestDto bookingRequest, HttpSession session) {
        try {
            User user = (User) session.getAttribute("user");
            
            // Create the booking using our updated BookingService
            Booking booking = bookingService.createBooking(bookingRequest, user);
            
            // Return success response
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("bookingId", booking.getId());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/receptionist")
    public String showDashboard(Model model) {
        // Get today's bookings
        List<Booking> todayBookings = bookingService.getBookingsByDate(LocalDate.now());
        model.addAttribute("bookings", todayBookings);
        return "ReceptionistDashboard";
    }
    
    @GetMapping("/receptionist/search")
    public String searchBookings(@RequestParam(required = false) String phone, Model model) {
        List<Booking> bookings;
        if (phone != null && !phone.trim().isEmpty()) {
            // Search bookings by phone number
            bookings = bookingService.getBookingsByCustomerPhone(phone);
        } else {
            // If no phone number provided, show today's bookings
            bookings = bookingService.getBookingsByDate(LocalDate.now());
        }
        model.addAttribute("bookings", bookings);
        return "ReceptionistDashboard";
    }
}