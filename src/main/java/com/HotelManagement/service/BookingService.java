package com.HotelManagement.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.HotelManagement.dto.BookingRequestDto;
import com.HotelManagement.dto.RoomSelectionDto;
import com.HotelManagement.dto.RoomTypeDto;
import com.HotelManagement.models.Booking;
import com.HotelManagement.models.BookingDetail;
import com.HotelManagement.models.Customer;
import com.HotelManagement.models.Room;
import com.HotelManagement.models.RoomType;
import com.HotelManagement.models.User;
import com.HotelManagement.repository.BookingDetailRepository;
import com.HotelManagement.repository.BookingRepository;
import com.HotelManagement.repository.CustomerRepository;
import com.HotelManagement.repository.RoomRepository;
import com.HotelManagement.repository.RoomTypeRepository;

@Service
public class BookingService {

    private static final Logger LOGGER = Logger.getLogger(BookingService.class.getName());
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private BookingDetailRepository bookingDetailRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private RoomTypeRepository roomTypeRepository;

    @Autowired
    private RoomService roomService;

    public List<Booking> getAllBookings() {
        LOGGER.info("Fetching all bookings");
        return bookingRepository.findAll();
    }

    public Booking getBookingById(Integer id) {
        if (id == null) {
            LOGGER.warning("Attempted to get booking with null id");
            return null;
        }
        LOGGER.info(String.format("Fetching booking with id: %d", id));
        return bookingRepository.findById(id).orElse(null);
    }

    public List<BookingDetail> getBookingDetailsByBookingId(Integer bookingId) {
        if (bookingId == null) {
            LOGGER.warning("Attempted to get booking details with null booking id");
            return new ArrayList<>();
        }
        
        LOGGER.info(String.format("Fetching booking details for booking id: %d", bookingId));
        Booking booking = bookingRepository.findById(bookingId).orElse(null);
        if (booking == null) {
            LOGGER.warning(String.format("No booking found with id: %d", bookingId));
            return new ArrayList<>();
        }
        return bookingDetailRepository.findByBookingId(bookingId);
    }

    @Transactional
    public Booking createBooking(BookingRequestDto bookingRequest, User user) {
        LOGGER.info(String.format("Creating new booking for user: %s", user.getUsername()));
        
        // Validate check-in date
        LocalDateTime checkInDate = LocalDate.parse(bookingRequest.getCheckInDate(), DATE_FORMATTER).atStartOfDay();
        // Compare dates only, not time
        if (checkInDate.toLocalDate().isBefore(LocalDate.now())) {
            LOGGER.warning(String.format("Attempt to create booking with past date: %s", checkInDate));
            throw new IllegalArgumentException("Check-in date cannot be in the past");
        }

        // Create or get customer
        Customer customer;
        if (bookingRequest.getCustomerId() != null) {
            LOGGER.fine(String.format("Using existing customer with id: %d", bookingRequest.getCustomerId()));
            customer = customerRepository.findById(bookingRequest.getCustomerId()).orElse(null);
            if (customer == null) {
                LOGGER.warning(String.format("Customer not found with id: %d", bookingRequest.getCustomerId()));
                throw new RuntimeException("Customer not found");
            }
        } else {
            LOGGER.warning("No customer ID provided in booking request");
            throw new RuntimeException("Customer ID is required");
        }

        // Create booking
        Booking booking = new Booking();
        booking.setCustomer(customer); // Use entity reference
        booking.setCheckInDate(checkInDate);
        booking.setStatus("CONFIRMED");
        booking.setPaymentStatus("PENDING"); // Set payment status to prevent NULL constraint error
        
        // Initialize total amount to zero
        BigDecimal totalAmount = BigDecimal.ZERO;
        booking.setTotalAmount(totalAmount); // Set initial amount (will be updated later)
        
        booking.setUser(user); // Use entity reference
        bookingRepository.save(booking);
        LOGGER.info(String.format("Created booking with id: %d", booking.getId()));

        // Create booking details for each selected room
        for (RoomSelectionDto roomSelection : bookingRequest.getRoomSelections()) {
            // Get rooms by room type
            RoomType roomType = roomTypeRepository.findById(roomSelection.getRoomTypeId()).orElse(null);
            if (roomType == null) {
                LOGGER.warning(String.format("Room type not found with id: %d", roomSelection.getRoomTypeId()));
                throw new RuntimeException("Room type not found");
            }
            
            LOGGER.fine(String.format("Processing room selection for room type: %s, count: %d", roomType.getName(), roomSelection.getCount()));
            
            // Get available rooms of this type using RoomService
            List<Room> availableRooms = roomService.getAvailableRoomsByType(roomType.getId())
                    .stream()
                    .limit(roomSelection.getCount())
                    .collect(Collectors.toList());
            
            if (availableRooms.size() < roomSelection.getCount()) {
                LOGGER.warning(String.format("Not enough rooms available of type: %s. Requested: %d, Available: %d", 
                              roomType.getName(), roomSelection.getCount(), availableRooms.size()));
                throw new RuntimeException("Not enough rooms available of type: " + roomType.getName());
            }
            
            // Create booking details for each available room
            for (Room room : availableRooms) {
                BookingDetail detail = new BookingDetail();
                detail.setBooking(booking); // Use entity reference
                detail.setRoom(room); // Use entity reference
                detail.setPrice(roomType.getPrice()); // Use room type price
                detail.setCheckInDate(checkInDate);
                detail.setStatus("BOOKED");
                bookingDetailRepository.save(detail);
                LOGGER.fine(String.format("Created booking detail for room: %s", room.getRoomNumber()));

                // Update room status to BOOKED
                room.setStatus("BOOKED");
                roomRepository.save(room);
                LOGGER.fine(String.format("Updated room status to BOOKED for room: %s", room.getRoomNumber()));
                
                // Add this room's price to the total amount
                totalAmount = totalAmount.add(roomType.getPrice());
            }
        }
        
        // Update the booking with the final total amount
        booking.setTotalAmount(totalAmount);
        bookingRepository.save(booking);
        LOGGER.info(String.format("Updated booking with total amount: %s", totalAmount));

        LOGGER.info(String.format("Booking creation completed successfully for booking id: %d", booking.getId()));
        return booking;
    }

    public List<RoomType> searchAvailableRoomTypes(LocalDate checkIn, LocalDate checkOut, int guestCount) {
        // Get all room types with sufficient capacity
        List<RoomType> allRoomTypes = roomTypeRepository.findAll().stream()
                .filter(rt -> rt.getCapacity() >= guestCount)
                .collect(Collectors.toList());
        
        // In a real application, you would filter based on availability
        // For now we'll just return room types with sufficient capacity
        return allRoomTypes;
    }

    public Booking processBookingUpdate(Integer bookingId, List<Integer> roomIds, User user) {
        Optional<Booking> optionalBooking = bookingRepository.findById(bookingId);
        if (!optionalBooking.isPresent()) {
            throw new RuntimeException("Booking not found");
        }
        
        Booking booking = optionalBooking.get();
        
        // Get existing details
        List<BookingDetail> existingDetails = bookingDetailRepository.findByBookingId(bookingId);
        
        // Release rooms that are no longer in the selection
        for (BookingDetail detail : existingDetails) {
            if (!roomIds.contains(detail.getRoom().getId())) {
                // Set room status back to AVAILABLE
                Room room = detail.getRoom();
                room.setStatus("AVAILABLE");
                roomRepository.save(room);
                
                // Delete the booking detail
                bookingDetailRepository.delete(detail);
            }
        }
        
        // Process new selections
        for (Integer roomId : roomIds) {
            // Check if this room is already in the booking
            boolean roomExists = existingDetails.stream()
                    .anyMatch(detail -> detail.getRoom().getId().equals(roomId));
            
            if (!roomExists) {
                // This is a new room, add it to the booking
                Room room = roomRepository.findById(roomId).orElse(null);
                if (room != null) {
                    BookingDetail detail = new BookingDetail();
                    detail.setBooking(booking);
                    detail.setRoom(room);
                    detail.setPrice(room.getRoomType().getPrice());
                    detail.setCheckInDate(LocalDateTime.now());
                    detail.setStatus("BOOKED");
                    bookingDetailRepository.save(detail);
                    
                    // Update room status
                    room.setStatus("BOOKED");
                    roomRepository.save(room);
                }
            }
        }
        
        // Update booking
        booking.setStatus("CONFIRMED");
        booking.setUser(user);
        return bookingRepository.save(booking);
    }
    
    public List<RoomTypeDto> getAvailableRoomTypesAsDto(LocalDate checkIn, LocalDate checkOut, int guestCount) {
        List<RoomType> availableRoomTypes = searchAvailableRoomTypes(checkIn, checkOut, guestCount);
        
        // Convert to DTOs
        return availableRoomTypes.stream()
                .map(rt -> {
                    RoomTypeDto dto = new RoomTypeDto();
                    dto.setId(rt.getId());
                    dto.setName(rt.getName());
                    dto.setDescription(rt.getDescription());
                    dto.setCapacity(rt.getCapacity());
                    dto.setPrice(rt.getPrice());
                    dto.setAmenities(rt.getAmenities());
                    dto.setImagePath(rt.getImagePath());
                    return dto;
                })
                .collect(Collectors.toList());
    }
    
    public void deleteBooking(Integer id) {
        // Find booking details and update room status
        List<BookingDetail> details = bookingDetailRepository.findByBookingId(id);
        for (BookingDetail detail : details) {
            Room room = detail.getRoom();
            room.setStatus("AVAILABLE");
            roomRepository.save(room);
            bookingDetailRepository.delete(detail);
        }
        
        // Delete booking
        bookingRepository.deleteById(id);
    }
    
    public boolean isRoomAvailable(Integer roomId, LocalDate checkInDate, LocalDate checkOutDate) {
        // Use RoomService to check availability
        if (checkInDate == null || checkOutDate == null) {
            throw new IllegalArgumentException("Check-in and check-out dates cannot be null");
        }
        
        return roomService.isRoomAvailableForDateRange(
            roomId,
            checkInDate.atStartOfDay(),
            checkOutDate.atTime(23, 59, 59)
        );
    }

    public List<Booking> getBookingsByDate(LocalDate date) {
        if (date == null) {
            LOGGER.warning("Attempted to get bookings with null date");
            throw new IllegalArgumentException("Date cannot be null");
        }
        LOGGER.info(String.format("Fetching bookings for date: %s", date));
        return bookingRepository.findByCheckInDate(date);
    }

    public List<Booking> getBookingsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate == null || endDate == null) {
            LOGGER.warning("Attempted to get bookings with null date range");
            throw new IllegalArgumentException("Start date and end date cannot be null");
        }
        
        if (startDate.isAfter(endDate)) {
            LOGGER.warning(String.format("Attempted to get bookings with invalid date range: %s to %s", 
                          startDate, endDate));
            throw new IllegalArgumentException("Start date cannot be after end date");
        }
        
        LOGGER.info(String.format("Fetching bookings for date range: %s to %s", startDate, endDate));
        return bookingRepository.findByCheckInDateBetween(startDate, endDate);
    }

    public List<Booking> getBookingsByCustomerPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            LOGGER.warning("Attempted to get bookings with null or empty phone");
            throw new IllegalArgumentException("Phone number cannot be empty");
        }
        LOGGER.info(String.format("Fetching bookings for customer phone: %s", phone));
        return bookingRepository.findByCustomerPhone(phone);
    }

    public List<Booking> getBookingsByStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            throw new IllegalArgumentException("Status cannot be empty");
        }
        return bookingRepository.findByStatus(status);
    }

    public BigDecimal calculateTotalAmount(Integer bookingId) {
        List<BookingDetail> details = getBookingDetailsByBookingId(bookingId);
        return details.stream()
                .map(BookingDetail::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Transactional
    public Booking updateBookingStatus(Integer bookingId, String newStatus) {
        if (newStatus == null || newStatus.trim().isEmpty()) {
            throw new IllegalArgumentException("Status cannot be empty");
        }

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        // Validate status transition
        validateStatusTransition(booking.getStatus(), newStatus);

        booking.setStatus(newStatus);

        // Update related booking details status
        List<BookingDetail> details = bookingDetailRepository.findByBookingId(bookingId);
        for (BookingDetail detail : details) {
            detail.setStatus(newStatus);
            
            // Update room status based on booking status
            Room room = detail.getRoom();
            switch (newStatus) {
                case "CHECKED_IN":
                    room.setStatus("OCCUPIED");
                    break;
                case "CHECKED_OUT":
                case "CANCELLED":
                    room.setStatus("AVAILABLE");
                    break;
                default:
                    // No room status change needed
                    break;
            }
            roomRepository.save(room);
            bookingDetailRepository.save(detail);
        }

        return bookingRepository.save(booking);
    }

    private void validateStatusTransition(String currentStatus, String newStatus) {
        // Define valid status transitions
        switch (currentStatus) {
            case "CONFIRMED":
                if (!newStatus.equals("CHECKED_IN") && !newStatus.equals("CANCELLED")) {
                    throw new IllegalStateException(String.format("Invalid status transition from CONFIRMED to %s", newStatus));
                }
                break;
            case "CHECKED_IN":
                if (!newStatus.equals("CHECKED_OUT")) {
                    throw new IllegalStateException(String.format("Invalid status transition from CHECKED_IN to %s", newStatus));
                }
                break;
            case "CHECKED_OUT":
            case "CANCELLED":
                throw new IllegalStateException(String.format("Cannot change status from %s", currentStatus));
            default:
                throw new IllegalStateException(String.format("Invalid current status: %s", currentStatus));
        }
    }

    @Transactional
    public void cancelBooking(Integer bookingId) {
        updateBookingStatus(bookingId, "CANCELLED");
    }

    @Transactional
    public Booking checkIn(Integer bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (!booking.getStatus().equals("CONFIRMED")) {
            throw new RuntimeException("Booking must be in CONFIRMED status to check in");
        }

        // Allow check-in on or after the check-in date, regardless of time
        LocalDate now = LocalDate.now();
        if (now.isBefore(booking.getCheckInDate().toLocalDate())) {
            throw new RuntimeException("Cannot check in before the scheduled check-in date");
        }

        return updateBookingStatus(bookingId, "CHECKED_IN");
    }

    @Transactional
    public Booking checkOut(Integer bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (!booking.getStatus().equals("CHECKED_IN")) {
            throw new RuntimeException("Booking must be in CHECKED_IN status to check out");
        }

        return updateBookingStatus(bookingId, "CHECKED_OUT");
    }

    public List<Booking> getUpcomingBookings() {
        LocalDateTime now = LocalDateTime.now();
        return bookingRepository.findByCheckInDateAfterAndStatus(now, "CONFIRMED");
    }

    public List<Booking> getTodaysBookings() {
        LocalDate today = LocalDate.now();
        return bookingRepository.findByCheckInDate(today);
    }

    public Long countBookingsByStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            throw new IllegalArgumentException("Status cannot be empty");
        }
        return bookingRepository.countByStatus(status);
    }

    public boolean hasConflictingBooking(Integer roomId, LocalDateTime checkInDate, LocalDateTime checkOutDate) {
        if (checkInDate.isAfter(checkOutDate)) {
            throw new IllegalArgumentException("Check-in date cannot be after check-out date");
        }
        List<BookingDetail> conflictingBookings = bookingDetailRepository.findConflictingBookings(roomId, checkInDate, checkOutDate);
        return !conflictingBookings.isEmpty();
    }

    public List<Booking> getBookingsByCustomerId(Integer customerId) {
        if (customerId == null) {
            throw new IllegalArgumentException("Customer ID cannot be null");
        }
        return bookingRepository.findByCustomerId(customerId);
    }

    public List<Booking> getBookingsByPaymentStatus(String paymentStatus) {
        if (paymentStatus == null || paymentStatus.trim().isEmpty()) {
            throw new IllegalArgumentException("Payment status cannot be empty");
        }
        return bookingRepository.findByPaymentStatus(paymentStatus);
    }

    public Long countBookingsByStatusAndDate(String status, LocalDate date) {
        if (status == null || status.trim().isEmpty()) {
            throw new IllegalArgumentException("Status cannot be empty");
        }
        if (date == null) {
            throw new IllegalArgumentException("Date cannot be null");
        }
        return bookingRepository.countByStatusAndDate(status, date);
    }

    public List<Booking> getTodayCheckIns() {
        return bookingRepository.findTodayCheckIns();
    }

    public List<Booking> getAllActiveBookings() {
        return bookingRepository.findAllActiveBookings();
    }

    @Transactional
    public Booking updatePaymentStatus(Integer bookingId, String newPaymentStatus) {
        if (newPaymentStatus == null || newPaymentStatus.trim().isEmpty()) {
            throw new IllegalArgumentException("Payment status cannot be empty");
        }

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        // Validate payment status
        if (!List.of("PENDING", "PAID", "REFUNDED", "CANCELLED").contains(newPaymentStatus)) {
            throw new IllegalArgumentException(String.format("Invalid payment status: %s", newPaymentStatus));
        }

        booking.setPaymentStatus(newPaymentStatus);
        return bookingRepository.save(booking);
    }
}