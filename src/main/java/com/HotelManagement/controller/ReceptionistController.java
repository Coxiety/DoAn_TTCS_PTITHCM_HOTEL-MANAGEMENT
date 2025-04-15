package com.HotelManagement.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import com.HotelManagement.models.BookingDetail;
import com.HotelManagement.models.Customer;
import com.HotelManagement.models.Room;
import com.HotelManagement.models.RoomType;
import com.HotelManagement.models.User;
import com.HotelManagement.repository.BookingDetailRepository;
import com.HotelManagement.repository.RoomTypeRepository;
import com.HotelManagement.service.BookingService;
import com.HotelManagement.service.CustomerService;
import com.HotelManagement.service.PaymentService;
import com.HotelManagement.service.RoomService;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/receptionist")
public class ReceptionistController {

    private static final Logger logger = LoggerFactory.getLogger(ReceptionistController.class);

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
    
    @Autowired
    private PaymentService paymentService;
    
    @GetMapping("")
    public String dashboard(Model model, HttpSession session, 
                           @RequestParam(required = false) Boolean bookingSuccess,
                           @RequestParam(required = false) Integer bookingId) {
        // Check if user is logged in and has receptionist role
        User user = (User) session.getAttribute("user");
        Integer userRole = (Integer) session.getAttribute("userRole");
        
        logger.debug("Dashboard accessed - User: {}, Role: {}", user, userRole);
        
        if (user == null || userRole == null || userRole != 2) {
            logger.warn("Unauthorized access attempt to receptionist dashboard - User: {}, Role: {}", user, userRole);
            return "redirect:/";
        }
        
        try {
            // Add success message for booking if applicable
            if (bookingSuccess != null && bookingSuccess && bookingId != null) {
                model.addAttribute("success", "Booking #" + bookingId + " has been created successfully!");
            }
            
            // Get all rooms for availability display
            List<Room> availableRooms = roomService.getAvailableRooms();
            logger.debug("Available rooms count: {}", availableRooms.size());
            model.addAttribute("availableRooms", availableRooms);
            
            // Get occupied rooms and their booking details
            List<Room> occupiedRooms = roomService.getOccupiedRooms();
            logger.debug("Occupied rooms count: {}", occupiedRooms.size());
            Map<Integer, BookingDetail> roomBookingDetails = new HashMap<>();
            Map<Integer, Booking> roomBookings = new HashMap<>();
            
            // Maps to group rooms by booking
            Map<Integer, List<Room>> roomsByBooking = new HashMap<>();
            Map<Integer, Booking> bookingsMap = new HashMap<>();
            
            for (Room room : occupiedRooms) {
                Optional<BookingDetail> bookingDetail = bookingDetailRepository.findByRoomIdAndStatus(room.getId(), "CHECKED_IN");
                if (bookingDetail.isPresent()) {
                    BookingDetail detail = bookingDetail.get();
                    roomBookingDetails.put(room.getId(), detail);
                    
                    // Also store the booking object to access check-in and check-out dates
                    Booking booking = detail.getBooking();
                    roomBookings.put(room.getId(), booking);
                    
                    // Group rooms by booking ID
                    Integer roomBookingId = booking.getId();
                    if (!roomsByBooking.containsKey(roomBookingId)) {
                        roomsByBooking.put(roomBookingId, new ArrayList<>());
                        bookingsMap.put(roomBookingId, booking);
                    }
                    roomsByBooking.get(roomBookingId).add(room);
                }
            }
            
            model.addAttribute("occupiedRooms", occupiedRooms);
            model.addAttribute("roomBookingDetails", roomBookingDetails);
            model.addAttribute("roomBookings", roomBookings);
            model.addAttribute("roomsByBooking", roomsByBooking);
            model.addAttribute("bookingsMap", bookingsMap);
            
            // Get today's check-ins
            List<Booking> todayCheckIns = bookingService.getTodayCheckIns();
            logger.debug("Today's check-ins count: {}", todayCheckIns.size());
            model.addAttribute("checkins", todayCheckIns);
            
            // Create a map of booking IDs to their details for room information
            if (!todayCheckIns.isEmpty()) {
                Map<Integer, List<BookingDetail>> bookingDetailsMap = new HashMap<>();
                for (Booking booking : todayCheckIns) {
                    List<BookingDetail> details = bookingDetailRepository.findByBookingId(booking.getId());
                    bookingDetailsMap.put(booking.getId(), details);
                }
                model.addAttribute("bookingDetailsMap", bookingDetailsMap);
            }
            
            return "receptionist/ReceptionistPage";
        } catch (Exception e) {
            logger.error("Error in dashboard method", e);
            throw e;
        }
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
            RedirectAttributes redirectAttributes) {
        
        try {
            Customer newCustomer = new Customer();
            newCustomer.setFullName(fullName);
            newCustomer.setPhone(phone);
            newCustomer.setEmail(email);
            
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
     * Search for bookings by phone number
     */
    @GetMapping("/searchBookingByPhone")
    public String searchBookingByPhone(@RequestParam String phone, Model model) {
        try {
            if (phone == null || phone.trim().isEmpty()) {
                model.addAttribute("error", "Phone number cannot be empty");
                return "receptionist/booking-search-results";
            }
            
            List<Booking> bookings = bookingService.getBookingsByCustomerPhone(phone);
            model.addAttribute("bookings", bookings);
            model.addAttribute("searchType", "phone");
            model.addAttribute("searchValue", phone);
            return "receptionist/booking-search-results";
        } catch (Exception e) {
            model.addAttribute("error", "Error searching for bookings: " + e.getMessage());
            return "receptionist/ReceptionistDashboard";
        }
    }
    
    /**
     * Show the new booking page for receptionists 
     */
    @GetMapping("/book/{customerId}")
    public String showBookingPage(@PathVariable Integer customerId, Model model, HttpSession session) {
        // Check if user is logged in and has receptionist role
        User user = (User) session.getAttribute("user");
        Integer userRole = (Integer) session.getAttribute("userRole");
        
        if (user == null || userRole == null || userRole != 2) {
            return "redirect:/";
        }
        
        // Get the selected customer
        Customer selectedCustomer;
        try {
            selectedCustomer = customerService.getCustomerById(customerId);
            if (selectedCustomer == null) {
                throw new RuntimeException("Customer not found");
            }
            // Add selected customer to the model
            model.addAttribute("selectedCustomer", selectedCustomer);
        } catch (Exception e) {
            model.addAttribute("error", "Customer not found: " + e.getMessage());
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
                String[] amenityArray = roomType.getAmenities().split(",");
                for (String amenity : amenityArray) {
                    String trimmedAmenity = amenity.trim();
                    RoomTypeAmenityDto amenityDto = new RoomTypeAmenityDto();
                    amenityDto.setName(trimmedAmenity);
                    amenityDto.setIcon(getIconForAmenity(trimmedAmenity));
                    amenities.add(amenityDto);
                }
            }
            
            dto.setAmenityList(amenities);
            roomTypeDtos.add(dto);
        }
        
        model.addAttribute("roomTypes", roomTypeDtos);
        
        // Convert room types to JSON for JavaScript use
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String roomTypesJson = objectMapper.writeValueAsString(roomTypeDtos);
            model.addAttribute("roomTypesJson", roomTypesJson);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return "receptionist/ReceptionistBookingPage";
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

    @GetMapping("/search")
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
        return "receptionist/ReceptionistDashboard";
    }
    
    /**
     * Handle booking cancellation by receptionist
     */
    @PostMapping("/cancel-booking")
    public String cancelBooking(@RequestParam Integer bookingId, RedirectAttributes redirectAttributes) {
        try {
            // Get the booking to check its status
            Booking booking = bookingService.getBookingById(bookingId);
            
            if (booking == null) {
                redirectAttributes.addFlashAttribute("error", "Booking not found");
                return "redirect:/receptionist";
            }
            
            // Can only cancel if status is CONFIRMED
            if (!"CONFIRMED".equals(booking.getStatus())) {
                redirectAttributes.addFlashAttribute("error", 
                    "Cannot cancel booking with status: " + booking.getStatus() + 
                    ". Only CONFIRMED bookings can be cancelled.");
                return "redirect:/receptionist";
            }
            
            // Process the cancellation
            bookingService.cancelBooking(bookingId);
            
            redirectAttributes.addFlashAttribute("success", "Booking #" + bookingId + " has been cancelled successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to cancel booking: " + e.getMessage());
        }
        
        return "redirect:/receptionist";
    }
    
    /**
     * Handle check-in by receptionist
     */
    @PostMapping("/check-in")
    public String checkInBooking(@RequestParam Integer bookingId, RedirectAttributes redirectAttributes) {
        try {
            // Process the check-in
            bookingService.checkIn(bookingId);
            
            redirectAttributes.addFlashAttribute("success", "Booking #" + bookingId + " has been checked in successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to check in: " + e.getMessage());
        }
        
        return "redirect:/receptionist";
    }
    
    /**
     * Handle check-out by receptionist from the occupied rooms view
     */
    @PostMapping("/check-out")
    public String checkOutRoom(@RequestParam Integer roomId, 
                              @RequestParam String paymentMethod, 
                              @RequestParam(required = false, defaultValue = "false") String earlyCheckoutConfirmed,
                              RedirectAttributes redirectAttributes) {
        try {
            // Find the active booking detail for this room
            Optional<BookingDetail> detailOpt = bookingDetailRepository.findByRoomIdAndStatus(roomId, "CHECKED_IN");
            if (!detailOpt.isPresent()) {
                redirectAttributes.addFlashAttribute("error", "No active booking found for this room");
                return "redirect:/receptionist";
            }
            
            // Get the booking detail and booking ID
            BookingDetail detail = detailOpt.get();
            Booking booking = detail.getBooking();
            Integer bookingId = booking.getId();
            
            // Check if there are other rooms in this booking
            List<BookingDetail> allBookingDetails = bookingDetailRepository.findByBookingId(bookingId);
            
            // Verify all rooms in the booking are checked in
            boolean allCheckedIn = allBookingDetails.stream()
                    .allMatch(bd -> "CHECKED_IN".equals(bd.getStatus()));
                    
            if (!allCheckedIn) {
                redirectAttributes.addFlashAttribute("error", 
                    "Cannot check out. Some rooms in this booking aren't checked in.");
                return "redirect:/receptionist";
            }
            
            // Check if it's an early checkout
            LocalDate today = LocalDate.now();
            LocalDate plannedCheckoutDate = booking.getCheckOutDate().toLocalDate();
            boolean isEarlyCheckout = today.isBefore(plannedCheckoutDate);
            
            // If it's an early checkout but not confirmed, redirect back
            if (isEarlyCheckout && !"true".equals(earlyCheckoutConfirmed)) {
                redirectAttributes.addFlashAttribute("error", 
                    "Early checkout must be confirmed. Please try again and confirm the early checkout.");
                return "redirect:/receptionist";
            }
            
            // Log early checkout if applicable
            if (isEarlyCheckout) {
                logger.info("Early checkout for booking #{}: Checkout date was {}, checking out on {}", 
                    bookingId, plannedCheckoutDate, today);
            }
            
            // Create payment record for the total booking amount
            paymentService.createPayment(bookingId, booking.getTotalAmount(), paymentMethod);
            
            // Check out the entire booking
            bookingService.checkOut(bookingId);
            
            String successMessage = "Booking #" + bookingId + " has been checked out successfully. " +
                "Total payment: $" + booking.getTotalAmount() + " (" + allBookingDetails.size() + " rooms)";
                
            if (isEarlyCheckout) {
                successMessage += " (Early checkout: " + (plannedCheckoutDate.getDayOfMonth() - today.getDayOfMonth()) + 
                    " days before planned date)";
            }
            
            redirectAttributes.addFlashAttribute("success", successMessage);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to check out: " + e.getMessage());
        }
        
        return "redirect:/receptionist";
    }
    
    /**
     * Handle check-out by receptionist from booking details with payment method selection
     */
    @PostMapping("/checkout-booking")
    public String checkOutBooking(@RequestParam Integer bookingId, 
                                 @RequestParam String paymentMethod, 
                                 @RequestParam(required = false, defaultValue = "false") String earlyCheckoutConfirmed,
                                 RedirectAttributes redirectAttributes) {
        try {
            // Get the booking
            Booking booking = bookingService.getBookingById(bookingId);
            
            if (booking == null) {
                redirectAttributes.addFlashAttribute("error", "Booking not found");
                return "redirect:/receptionist";
            }
            
            // Can only check out if status is CHECKED_IN
            if (!"CHECKED_IN".equals(booking.getStatus())) {
                redirectAttributes.addFlashAttribute("error", 
                    "Cannot check out booking with status: " + booking.getStatus() + 
                    ". Only CHECKED_IN bookings can be checked out.");
                return "redirect:/receptionist";
            }
            
            // Check if it's an early checkout
            LocalDate today = LocalDate.now();
            LocalDate plannedCheckoutDate = booking.getCheckOutDate().toLocalDate();
            boolean isEarlyCheckout = today.isBefore(plannedCheckoutDate);
            
            // If it's an early checkout but not confirmed, show confirmation dialog
            if (isEarlyCheckout && !"true".equals(earlyCheckoutConfirmed)) {
                redirectAttributes.addFlashAttribute("showEarlyCheckoutConfirmation", true);
                redirectAttributes.addFlashAttribute("bookingId", bookingId);
                redirectAttributes.addFlashAttribute("paymentMethod", paymentMethod);
                redirectAttributes.addFlashAttribute("warningMessage", 
                    "You are checking out before the planned check-out date (" + 
                    plannedCheckoutDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")) + 
                    "). Please confirm to proceed.");
                return "redirect:/receptionist";
            }
            
            // Log early checkout if applicable
            if (isEarlyCheckout) {
                logger.info("Early checkout for booking #{}: Checkout date was {}, checking out on {}", 
                    bookingId, plannedCheckoutDate, today);
            }
            
            // Create payment record
            paymentService.createPayment(bookingId, booking.getTotalAmount(), paymentMethod);
            
            // Check out the booking
            bookingService.checkOut(bookingId);
            
            // Redirect to invoice page instead of dashboard
            return "redirect:/receptionist/invoice/" + bookingId + "?paymentMethod=" + paymentMethod;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to check out: " + e.getMessage());
            return "redirect:/receptionist";
        }
    }
    
    /**
     * Display invoice after successful checkout
     */
    @GetMapping("/invoice/{bookingId}")
    public String showInvoice(@PathVariable Integer bookingId, 
                             @RequestParam String paymentMethod,
                             Model model, 
                             HttpSession session) {
        // Check if user is logged in and has receptionist role
        User user = (User) session.getAttribute("user");
        Integer userRole = (Integer) session.getAttribute("userRole");
        
        if (user == null || userRole == null || userRole != 2) {
            return "redirect:/";
        }
        
        try {
            // Get the booking details
            Booking booking = bookingService.getBookingById(bookingId);
            if (booking == null) {
                model.addAttribute("error", "Booking not found");
                return "redirect:/receptionist";
            }
            
            // Get all booking details (rooms)
            List<BookingDetail> bookingDetails = bookingDetailRepository.findByBookingId(bookingId);
            
            // Calculate nights stayed
            long nightsStayed = java.time.temporal.ChronoUnit.DAYS
                .between(booking.getCheckInDate().toLocalDate(), LocalDate.now());
            
            // Ensure at least 1 night is charged
            if (nightsStayed < 1) {
                nightsStayed = 1;
            }
            
            // Add to model
            model.addAttribute("booking", booking);
            model.addAttribute("bookingDetails", bookingDetails);
            model.addAttribute("paymentMethod", paymentMethod);
            model.addAttribute("nightsStayed", nightsStayed);
            model.addAttribute("checkoutDate", LocalDate.now());
            
            // Format total
            model.addAttribute("formattedTotal", String.format("%.2f", booking.getTotalAmount()));
            
            return "invoice/InvoicePage";
        } catch (Exception e) {
            model.addAttribute("error", "Error generating invoice: " + e.getMessage());
            return "redirect:/receptionist";
        }
    }
    
    @PostMapping("/updatePaymentStatus")
    public String updatePaymentStatus(@RequestParam Integer bookingId, 
                                     @RequestParam String paymentStatus,
                                     RedirectAttributes redirectAttributes) {
        try {
            bookingService.updatePaymentStatus(bookingId, paymentStatus);
            redirectAttributes.addFlashAttribute("success", 
                "Payment status updated to " + paymentStatus + " for booking #" + bookingId);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/receptionist";
    }

    /**
     * Get booking details by room ID for the checkout modal
     */
    @GetMapping("/booking-by-room")
    @ResponseBody
    public Map<String, Object> getBookingByRoom(@RequestParam Integer roomId) {
        // Find the booking detail for this room
        Optional<BookingDetail> detailOpt = bookingDetailRepository.findByRoomIdAndStatus(roomId, "CHECKED_IN");
        if (!detailOpt.isPresent()) {
            throw new RuntimeException("No active booking found for this room");
        }
        
        // Get the booking and all its details
        BookingDetail detail = detailOpt.get();
        Booking booking = detail.getBooking();
        List<BookingDetail> allDetails = bookingDetailRepository.findByBookingId(booking.getId());
        
        // Convert to simple room objects for the response
        List<Map<String, Object>> rooms = allDetails.stream()
            .map(bd -> {
                Map<String, Object> room = new HashMap<>();
                room.put("id", bd.getRoom().getId());
                room.put("roomNumber", bd.getRoom().getRoomNumber());
                room.put("price", bd.getPrice());
                return room;
            })
            .collect(Collectors.toList());
        
        // Build the response
        Map<String, Object> response = new HashMap<>();
        response.put("bookingId", booking.getId());
        response.put("totalAmount", booking.getTotalAmount());
        response.put("rooms", rooms);
        response.put("checkInDate", booking.getCheckInDate());
        response.put("checkOutDate", booking.getCheckOutDate());
        response.put("customerName", booking.getCustomer().getFullName());
        
        return response;
    }
}