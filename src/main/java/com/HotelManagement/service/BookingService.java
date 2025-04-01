package com.HotelManagement.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.HotelManagement.models.Booking;
import com.HotelManagement.models.BookingDetail;
import com.HotelManagement.models.Customer;
import com.HotelManagement.models.Room;
import com.HotelManagement.models.User;
import com.HotelManagement.repository.BookingDetailRepository;
import com.HotelManagement.repository.BookingRepository;
import com.HotelManagement.repository.CustomerRepository;
import com.HotelManagement.repository.RoomRepository;
import com.HotelManagement.repository.UserRepository;

import jakarta.transaction.Transactional;

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
    private UserRepository userRepository;
    
    public List<Booking> getTodaysCheckins() {
        LocalDateTime startOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime endOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
        
        return bookingRepository.findByCheckInDateBetweenAndStatus(startOfDay, endOfDay, "CONFIRMED");
    }
    
    @Transactional
    public void checkInBooking(Integer bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        
        if (!"CONFIRMED".equals(booking.getStatus())) {
            throw new RuntimeException("Booking is not in CONFIRMED status");
        }
        
        // Update booking status
        booking.setStatus("CHECKED_IN");
        bookingRepository.save(booking);
        
        // Update booking detail status and room status
        List<BookingDetail> details = bookingDetailRepository.findByBookingId(bookingId);
        for (BookingDetail detail : details) {
            detail.setStatus("OCCUPIED");
            bookingDetailRepository.save(detail);
            
            Room room = detail.getRoom();
            room.setStatus("OCCUPIED");
            roomRepository.save(room);
        }
    }
    
    @Transactional
    public void checkOutRoom(Integer roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));
        
        if (!"OCCUPIED".equals(room.getStatus())) {
            throw new RuntimeException("Room is not occupied");
        }
        
        // Find the active booking detail for this room
        BookingDetail detail = bookingDetailRepository.findByRoomIdAndStatus(roomId, "OCCUPIED")
                .orElseThrow(() -> new RuntimeException("No active booking found for this room"));
        
        // Update booking detail status
        detail.setStatus("CHECKED_OUT");
        bookingDetailRepository.save(detail);
        
        // Update room status
        room.setStatus("AVAILABLE");
        roomRepository.save(room);
        
        // Check if all rooms for this booking are checked out
        Booking booking = detail.getBooking();
        List<BookingDetail> allDetails = bookingDetailRepository.findByBookingId(booking.getId());
        boolean allCheckedOut = allDetails.stream()
                .allMatch(d -> "CHECKED_OUT".equals(d.getStatus()));
        
        if (allCheckedOut) {
            booking.setStatus("COMPLETED");
            bookingRepository.save(booking);
        }
    }
    
    @Transactional
    public Booking createBookingByReceptionist(Integer customerId, Integer roomId, String checkInDateStr, Integer userId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));
        
        if (!"AVAILABLE".equals(room.getStatus())) {
            throw new RuntimeException("Room is not available");
        }
        
        User receptionist = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Parse check-in date
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;
        LocalDate checkInDate = LocalDate.parse(checkInDateStr, formatter);
        LocalDateTime checkInDateTime = LocalDateTime.of(checkInDate, LocalTime.of(14, 0)); // Check-in at 2 PM
        
        // Create booking
        Booking booking = new Booking();
        booking.setCustomer(customer);
        booking.setCheckInDate(checkInDateTime);
        booking.setStatus("CONFIRMED");
        booking.setUser(receptionist);
        booking = bookingRepository.save(booking);
        
        // Create booking detail
        BookingDetail detail = new BookingDetail();
        detail.setBooking(booking);
        detail.setRoom(room);
        detail.setCheckInDate(checkInDateTime);
        detail.setPrice(room.getPrice());
        detail.setStatus("CONFIRMED");
        bookingDetailRepository.save(detail);
        
        // Update room status
        room.setStatus("BOOKED");
        roomRepository.save(room);
        
        return booking;
    }
}