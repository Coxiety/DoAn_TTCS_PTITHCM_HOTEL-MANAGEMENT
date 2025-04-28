package com.HotelManagement.controller;

import java.math.BigDecimal;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.HotelManagement.dto.BookingRequestDto;
import com.HotelManagement.dto.RoomSelectionDto;
import com.HotelManagement.models.Booking;
import com.HotelManagement.models.BookingDetail;
import com.HotelManagement.models.Customer;
import com.HotelManagement.models.RoomType;
import com.HotelManagement.models.User;
import com.HotelManagement.repository.CustomerRepository;
import com.HotelManagement.service.BookingDetailService;
import com.HotelManagement.service.BookingService;
import com.fasterxml.jackson.databind.ObjectMapper;

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
            // Validate input
            if (bookingRequest == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "Booking data is required"));
            }

            if (bookingRequest.getCheckInDate() == null || bookingRequest.getCheckInDate().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Check-in date is required"));
            }

            if (bookingRequest.getCheckOutDate() == null || bookingRequest.getCheckOutDate().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Check-out date is required"));
            }

            if (bookingRequest.getRoomSelections() == null || bookingRequest.getRoomSelections().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "At least one room selection is required"));
            }

            // Find customer by user ID instead of phone number
            Customer customer = customerRepository.findByUserId(user.getId()).orElse(null);

            if (customer == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "Customer record not found. Please contact reception."));
            }

            // Set the customer ID in the booking request
            bookingRequest.setCustomerId(customer.getId());

            Booking booking = bookingService.createBooking(bookingRequest, user);
            Map<String, Object> response = new HashMap<>();
            response.put("bookingId", booking.getId());
            response.put("message", "Booking created successfully");
            response.put("redirectUrl", "/booking/confirmation/" + booking.getId());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/booking/confirmation/{id}")
    public String showBookingConfirmation(@PathVariable("id") Integer bookingId, Model model) {
        // Get the booking
        Booking booking = bookingService.getBookingById(bookingId);
        if (booking == null) {
            model.addAttribute("error", "Booking not found");
            return "Error";
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

        return "booking/BookingConfirmation";
    }

    @GetMapping("/booking")
    public String showBookingPage(Model model, HttpSession session) {
        // Check if user is logged in
        Integer userRole = (Integer) session.getAttribute("userRole");

        // If user is logged in as receptionist, redirect to receptionist page
        if (userRole != null && Integer.valueOf(2).equals(userRole)) {
            return "redirect:/receptionist";
        }

        // If user is logged in as admin, redirect to admin page
        if (userRole != null && Integer.valueOf(1).equals(userRole)) {
            return "redirect:/admin";
        }

        // Get all room types with counts of available rooms
        List<RoomType> roomTypes = bookingService.getAllRoomTypes();
        model.addAttribute("roomTypes", roomTypes);

        // Get available rooms count for each room type
        Map<Integer, Integer> availableRoomCounts = new HashMap<>();
        Map<String, Object> roomTypesJson = new HashMap<>();

        for (RoomType roomType : roomTypes) {
            int availableCount = bookingService.getAvailableRoomCountByType(roomType.getId());
            availableRoomCounts.put(roomType.getId(), availableCount);

            // Create room type data for JavaScript
            Map<String, Object> roomTypeData = new HashMap<>();
            roomTypeData.put("name", roomType.getName());
            roomTypeData.put("capacity", roomType.getCapacity());
            roomTypeData.put("price", roomType.getPrice());
            roomTypeData.put("description", roomType.getDescription());

            // Process amenities
            List<String> amenitiesList = new ArrayList<>();
            if (roomType.getAmenities() != null && !roomType.getAmenities().isEmpty()) {
                String[] amenities = roomType.getAmenities().split(",");
                for (String amenity : amenities) {
                    String trimmedAmenity = amenity.trim();
                    if (!trimmedAmenity.isEmpty()) {
                        amenitiesList.add(trimmedAmenity);
                    }
                }
            }
            roomTypeData.put("amenities", amenitiesList);
            roomTypeData.put("availableCount", availableCount);

            // Add to the JSON map using room type ID as key
            roomTypesJson.put(roomType.getId().toString(), roomTypeData);
        }

        model.addAttribute("availableRoomCounts", availableRoomCounts);
        model.addAttribute("roomTypesJson", roomTypesJson);

        return "booking/BookingPage";
    }

    @PostMapping("/booking/create")
    public String createBookingFromForm(
            @RequestParam("checkInDate") String checkInDate,
            @RequestParam("checkOutDate") String checkOutDate,
            @RequestParam("roomSelectionsJson") String roomSelectionsJson,
            @RequestParam(value = "totalPrice", required = false) String totalPrice,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        // Get user from session
        User user = (User) session.getAttribute("user");
        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "Please login to make a booking");
            return "redirect:/login";
        }

        try {
            // Parse room selections from JSON
            ObjectMapper objectMapper = new ObjectMapper();
            List<Map<String, Object>> roomSelections = objectMapper.readValue(
                roomSelectionsJson,
                objectMapper.getTypeFactory().constructCollectionType(List.class, Map.class)
            );

            // Create booking request DTO
            BookingRequestDto bookingRequest = new BookingRequestDto();
            bookingRequest.setCheckInDate(checkInDate);
            bookingRequest.setCheckOutDate(checkOutDate);

            // Find customer by user ID instead of phone number
            Customer customer = customerRepository.findByUserId(user.getId()).orElse(null);
            if (customer == null) {
                redirectAttributes.addFlashAttribute("error", "Customer record not found. Please contact reception.");
                return "redirect:/booking";
            }

            bookingRequest.setCustomerId(customer.getId());

            // Convert room selections to DTO format
            List<RoomSelectionDto> dtoSelections = roomSelections.stream()
                .map(selection -> {
                    RoomSelectionDto dto = new RoomSelectionDto();
                    dto.setRoomTypeId(((Number) selection.get("roomTypeId")).intValue());
                    dto.setCount(((Number) selection.get("count")).intValue());
                    return dto;
                })
                .collect(Collectors.toList());

            bookingRequest.setRoomSelections(dtoSelections);

            // Create booking
            Booking booking = bookingService.createBooking(bookingRequest, user);

            // Redirect to confirmation page
            return "redirect:/booking/confirmation/" + booking.getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error creating booking: " + e.getMessage());
            return "redirect:/booking";
        }
    }
}
