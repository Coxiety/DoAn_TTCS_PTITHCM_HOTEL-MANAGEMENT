package com.HotelManagement.controller;

import java.time.LocalDate;
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
import com.HotelManagement.dto.RoomSelectionDto;
import com.HotelManagement.dto.RoomTypeAmenityDto;
import com.HotelManagement.dto.RoomTypeDto;
import com.HotelManagement.models.Booking;
import com.HotelManagement.models.BookingDetail;
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
    
    @GetMapping("")
    public String dashboard(
            @RequestParam(required = false) Boolean bookingSuccess,
            @RequestParam(required = false) Integer bookingId,
            Model model, 
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        
        // Check if user is logged in and has receptionist role
        User user = (User) session.getAttribute("user");
        Integer userRole = (Integer) session.getAttribute("userRole");
        
        if (user == null || userRole == null || userRole != 1) {
            return "redirect:/";
        }
        
        // Handle booking success message
        if (bookingSuccess != null && bookingSuccess && bookingId != null) {
            redirectAttributes.addFlashAttribute("success", 
                    "Booking #" + bookingId + " created successfully!");
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
    
    @GetMapping("/get-all-customers")
    @ResponseBody
    public List<Customer> getAllCustomers() {
        return customerService.getAllCustomers();
    }
    
    @GetMapping("/search-booking-by-phone")
    public String searchBookingByPhone(@RequestParam String phone, Model model) {
        List<Booking> bookings = bookingService.getBookingsByCustomerPhone(phone);
        
        // For each booking, fetch its details
        Map<Integer, List<BookingDetail>> bookingDetailsMap = new HashMap<>();
        for (Booking booking : bookings) {
            List<BookingDetail> details = bookingDetailRepository.findByBookingId(booking.getId());
            bookingDetailsMap.put(booking.getId(), details);
        }
        
        model.addAttribute("searchResults", bookings);
        model.addAttribute("bookingDetailsMap", bookingDetailsMap);
        model.addAttribute("searchPhone", phone);
        
        return "fragments/booking-search-results :: results";
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
    
    /**
     * Show the new booking page similar to customer booking page, but for receptionists
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
            
            // Get room price from the first room of this type (assuming all rooms of same type have same price)
            List<Room> rooms = roomService.getAvailableRoomsByType(roomType.getId());
            dto.setAvailableRooms(rooms);
            dto.setAvailableCount(rooms.size());
            
            if (!rooms.isEmpty()) {
                dto.setPrice(rooms.get(0).getPrice().intValue());
            } else {
                dto.setPrice(100); // Default price if no rooms available
            }
            
            // Add sample amenities
            List<RoomTypeAmenityDto> amenities = new ArrayList<>();
            amenities.add(new RoomTypeAmenityDto("fas fa-wifi", "Free Wi-Fi"));
            amenities.add(new RoomTypeAmenityDto("fas fa-tv", "Flat-screen TV"));
            amenities.add(new RoomTypeAmenityDto("fas fa-snowflake", "Air Conditioning"));
            amenities.add(new RoomTypeAmenityDto("fas fa-shower", "Private Bathroom"));
            
            if (roomType.getCapacity() > 2) {
                amenities.add(new RoomTypeAmenityDto("fas fa-coffee", "Premium Coffee Machine"));
                amenities.add(new RoomTypeAmenityDto("fas fa-bath", "Bathtub"));
            }
            
            if (roomType.getCapacity() == 4) {
                amenities.add(new RoomTypeAmenityDto("fas fa-couch", "Separate Living Room"));
                amenities.add(new RoomTypeAmenityDto("fas fa-concierge-bell", "Butler Service"));
            }
            
            dto.setAmenities(amenities);
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
     * Process a full booking with multiple rooms
     */
    @PostMapping("/create-booking-full")
    @ResponseBody
    public ResponseEntity<?> createFullBooking(@RequestBody BookingRequestDto bookingRequest, HttpSession session) {
        try {
            User user = (User) session.getAttribute("user");
            
            // Extract booking data
            Integer customerId = bookingRequest.getCustomerId();
            String checkInDateStr = bookingRequest.getCheckInDate();
            String checkOutDateStr = bookingRequest.getCheckOutDate();
            List<RoomSelectionDto> roomSelections = bookingRequest.getRoomSelections();
            
            // Create the booking
            Integer bookingId = bookingService.createFullBooking(
                    customerId, 
                    LocalDate.parse(checkInDateStr),
                    LocalDate.parse(checkOutDateStr), 
                    roomSelections, 
                    user.getId());
            
            // Return success response
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("bookingId", bookingId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}