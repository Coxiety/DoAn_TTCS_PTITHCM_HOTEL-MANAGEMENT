package com.HotelManagement.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    public Booking getBookingById(Integer id) {
        return bookingRepository.findById(id).orElse(null);
    }

    public List<BookingDetail> getBookingDetailsByBookingId(Integer bookingId) {
        Booking booking = bookingRepository.findById(bookingId).orElse(null);
        if (booking == null) {
            return new ArrayList<>();
        }
        return bookingDetailRepository.findByBookingId(bookingId);
    }

    public Booking createBooking(BookingRequestDto bookingRequest, User user) {
        // Create or get customer
        Customer customer;
        if (bookingRequest.getCustomerId() != null) {
            customer = customerRepository.findById(bookingRequest.getCustomerId()).orElse(null);
            if (customer == null) {
                throw new RuntimeException("Customer not found");
            }
        } else {
            // Create new customer if needed - 
            // Note: BookingRequestDto would need additional fields for this
            // In this simplified version, we'll require a customer ID
            throw new RuntimeException("Customer ID is required");
        }

        // Create booking
        Booking booking = new Booking();
        booking.setCustomer(customer); // Use entity reference
        // Parse the date string to a LocalDateTime object
        LocalDateTime checkInDate = LocalDate.parse(bookingRequest.getCheckInDate(), DATE_FORMATTER).atStartOfDay();
        booking.setCheckInDate(checkInDate);
        booking.setStatus("CONFIRMED");
        booking.setUser(user); // Use entity reference
        bookingRepository.save(booking);

        // Create booking details for each selected room
        for (RoomSelectionDto roomSelection : bookingRequest.getRoomSelections()) {
            // Get rooms by room type
            RoomType roomType = roomTypeRepository.findById(roomSelection.getRoomTypeId()).orElse(null);
            if (roomType == null) {
                throw new RuntimeException("Room type not found");
            }
            
            // Get available rooms of this type
            List<Room> availableRooms = roomRepository.findByStatus("AVAILABLE").stream()
                    .filter(r -> r.getRoomType().getId().equals(roomType.getId()))
                    .limit(roomSelection.getCount())
                    .collect(Collectors.toList());
            
            if (availableRooms.size() < roomSelection.getCount()) {
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

                // Update room status to BOOKED
                room.setStatus("BOOKED");
                roomRepository.save(room);
            }
        }

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

    public List<Room> getAvailableRoomsByType(Integer roomTypeId) {
        // Get all available rooms by type
        List<Room> availableRooms = roomRepository.findByStatus("AVAILABLE").stream()
                .filter(r -> r.getRoomType().getId().equals(roomTypeId))
                .collect(Collectors.toList());
        
        return availableRooms;
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
        // In a real application, you would check if the room is available for the specified date range
        // For now, we'll just return true if the room status is "AVAILABLE"
        Room room = roomRepository.findById(roomId).orElse(null);
        return room != null && "AVAILABLE".equals(room.getStatus());
    }

    public List<Booking> getBookingsByDate(LocalDate date) {
        return bookingRepository.findByCheckInDate(date);
    }

    public List<Booking> getBookingsByCustomerPhone(String phone) {
        return bookingRepository.findByCustomerPhone(phone);
    }
}