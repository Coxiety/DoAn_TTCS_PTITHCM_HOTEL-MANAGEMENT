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

import com.HotelManagement.models.Booking;
import com.HotelManagement.models.BookingDetail;
import com.HotelManagement.models.Customer;
import com.HotelManagement.models.Room;
import com.HotelManagement.models.RoomType;
import com.HotelManagement.models.User;
import com.HotelManagement.repository.BookingDetailRepository;
import com.HotelManagement.repository.CustomerRepository;
import com.HotelManagement.repository.RoomTypeRepository;
import com.HotelManagement.service.AuthService;
import com.HotelManagement.service.BookingService;
import com.HotelManagement.service.CustomerService;
import com.HotelManagement.service.PaymentService;
import com.HotelManagement.service.RoomService;

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

    @Autowired
    private AuthService authService;

    @Autowired
    private CustomerRepository customerRepository;

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
            // Generate username from email or phone
            String username = email.split("@")[0]; // Use part before @ in email as username
            // Generate a random password (customers can change it later)
            String password = "Customer" + phone.substring(Math.max(0, phone.length() - 4)); // Last 4 digits of phone
            
            // First create a user
            User newUser = new User();
            newUser.setUsername(username);
            newUser.setEmail(email);
            newUser.setPhone(phone);
            newUser.setFullName(fullName);
            newUser.setRoleId(0); // 0 for customer role
            
            // Use AuthService to handle password hashing and user creation
            User savedUser = authService.signup(username, password, email, fullName, phone);
            
            // The customer is already created by AuthService.signup
            // We just need to get the customer ID for redirection
            Customer customer = customerService.getCustomerByUserId(savedUser.getId());
            
            redirectAttributes.addFlashAttribute("success", 
                "Customer created successfully with username: " + username + " and temporary password: " + password);
            redirectAttributes.addFlashAttribute("customerId", customer.getId());
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

    @GetMapping("/search")
    public String searchBookings(@RequestParam(required = false) String phone, Model model) {
        // Since we removed the phone search from the UI, just show today's bookings by default
        List<Booking> bookings = bookingService.getTodayCheckIns();
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

    /**
     * Display booking page for a specific customer
     */
    @GetMapping("/book/{customerId}")
    public String bookForCustomer(@PathVariable Integer customerId, Model model, HttpSession session) {
        // Check if user is logged in and has receptionist role
        User user = (User) session.getAttribute("user");
        Integer userRole = (Integer) session.getAttribute("userRole");

        if (user == null || userRole == null || userRole != 2) {
            return "redirect:/";
        }

        try {
            // Get the customer
            Customer selectedCustomer = customerService.getCustomerById(customerId);
            if (selectedCustomer == null) {
                model.addAttribute("error", "Customer not found");
                return "redirect:/receptionist";
            }

            // Add customer to model
            model.addAttribute("selectedCustomer", selectedCustomer);

            // Get all room types
            List<RoomType> roomTypes = roomTypeRepository.findAll();
            Map<Integer, Integer> availableRoomCounts = new HashMap<>();
            Map<Integer, List<String>> roomTypeAmenities = new HashMap<>();

            // Process room types for display
            Map<String, Object> roomTypesJson = new HashMap<>();

            for (RoomType roomType : roomTypes) {
                // Get available rooms count for this type
                List<Room> availableRooms = roomService.getAvailableRoomsByType(roomType.getId());
                int availableCount = availableRooms.size();
                availableRoomCounts.put(roomType.getId(), availableCount);

                // Create room type data for JavaScript
                Map<String, Object> roomTypeData = new HashMap<>();
                roomTypeData.put("name", roomType.getName());
                roomTypeData.put("capacity", roomType.getCapacity());
                roomTypeData.put("price", roomType.getPrice());
                roomTypeData.put("description", roomType.getDescription());

                // Process amenities for display and JSON
                if (roomType.getAmenities() != null && !roomType.getAmenities().isEmpty()) {
                    List<String> amenityList = new ArrayList<>();
                    List<String> amenitiesForJson = new ArrayList<>();
                    String[] amenities = roomType.getAmenities().split(",");

                    for (String amenity : amenities) {
                        String trimmedAmenity = amenity.trim();
                        if (!trimmedAmenity.isEmpty()) {
                            amenityList.add(trimmedAmenity + ":" + getIconForAmenity(trimmedAmenity));
                            amenitiesForJson.add(trimmedAmenity);
                        }
                    }

                    roomTypeAmenities.put(roomType.getId(), amenityList);
                    roomTypeData.put("amenities", amenitiesForJson);
                } else {
                    roomTypeData.put("amenities", new ArrayList<>());
                }

                roomTypeData.put("availableCount", availableCount);

                // Add to the JSON map using room type ID as key
                roomTypesJson.put(roomType.getId().toString(), roomTypeData);
            }

            // Add to model
            model.addAttribute("roomTypes", roomTypes);
            model.addAttribute("availableRoomCounts", availableRoomCounts);
            model.addAttribute("roomTypeAmenities", roomTypeAmenities);
            model.addAttribute("roomTypesJson", roomTypesJson);

            return "receptionist/ReceptionistBookingPage";
        } catch (Exception e) {
            model.addAttribute("error", "Error loading booking page: " + e.getMessage());
            return "redirect:/receptionist";
        }
    }

    /**
     * Handle booking creation from receptionist booking page
     */
    @PostMapping("/create-booking-full")
    @ResponseBody
    public ResponseEntity<?> createBookingFull(@RequestBody Map<String, Object> bookingData) {
        try {
            // Extract data from bookingData
            String checkInDate = (String) bookingData.get("checkInDate");
            String checkOutDate = (String) bookingData.get("checkOutDate");
            
            // Handle customerId that can be either String or Number
            Integer customerId;
            Object rawCustomerId = bookingData.get("customerId");
            if (rawCustomerId instanceof Number) {
                customerId = ((Number) rawCustomerId).intValue();
            } else if (rawCustomerId instanceof String) {
                customerId = Integer.parseInt((String) rawCustomerId);
            } else {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid customer ID format"));
            }
            
            if (checkInDate == null || checkOutDate == null || customerId == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Missing required booking data"));
            }
            
            List<Map<String, Object>> roomSelections = (List<Map<String, Object>>) bookingData.get("roomSelections");
            
            // Extract room type IDs and counts
            List<Integer> roomTypeIds = new ArrayList<>();
            List<Integer> roomCounts = new ArrayList<>();
            
            for (Map<String, Object> selection : roomSelections) {
                // Handle roomTypeId that could be String or Number
                Object rawRoomTypeId = selection.get("roomTypeId");
                Integer roomTypeId;
                if (rawRoomTypeId instanceof Number) {
                    roomTypeId = ((Number) rawRoomTypeId).intValue();
                } else if (rawRoomTypeId instanceof String) {
                    roomTypeId = Integer.parseInt((String) rawRoomTypeId);
                } else {
                    throw new IllegalArgumentException("Invalid room type ID format");
                }
                
                // Handle count that could be String or Number
                Object rawCount = selection.get("count");
                Integer count;
                if (rawCount instanceof Number) {
                    count = ((Number) rawCount).intValue();
                } else if (rawCount instanceof String) {
                    count = Integer.parseInt((String) rawCount);
                } else {
                    throw new IllegalArgumentException("Invalid count format");
                }
                
                roomTypeIds.add(roomTypeId);
                roomCounts.add(count);
            }

            // Create booking (without user since receptionist is doing it)
            Booking booking = bookingService.createBooking(checkInDate, checkOutDate, customerId, roomTypeIds, roomCounts, null);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "bookingId", booking.getId(),
                "message", "Booking created successfully"
            ));
        } catch (Exception e) {
            logger.error("Error creating booking: ", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}