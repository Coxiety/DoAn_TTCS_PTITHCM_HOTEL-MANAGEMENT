package com.HotelManagement.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.HotelManagement.models.BookingDetail;
import com.HotelManagement.models.Room;
import com.HotelManagement.repository.BookingDetailRepository;
import com.HotelManagement.repository.RoomRepository;

@Service
public class BookingDetailService {
    
    @Autowired
    private BookingDetailRepository bookingDetailRepository;
    
    @Autowired
    private RoomRepository roomRepository;
    
    public List<BookingDetail> getBookingDetailsByBookingId(Integer bookingId) {
        return bookingDetailRepository.findByBookingId(bookingId);
    }
    
    public boolean isRoomAvailable(Integer roomId, LocalDateTime startDate, LocalDateTime endDate) {
        List<BookingDetail> conflictingBookings = bookingDetailRepository.findConflictingBookings(roomId, startDate, endDate);
        return conflictingBookings.isEmpty();
    }
    
    @Transactional
    public BookingDetail createBookingDetail(BookingDetail bookingDetail) {
        // Validate room availability
        if (!isRoomAvailable(bookingDetail.getRoom().getId(), 
                           bookingDetail.getBooking().getCheckInDate(),
                           bookingDetail.getBooking().getCheckOutDate())) {
            throw new RuntimeException("Room is not available for the selected dates");
        }
        
        // Update room status
        Room room = bookingDetail.getRoom();
        room.setStatus("BOOKED");
        roomRepository.save(room);
        
        return bookingDetailRepository.save(bookingDetail);
    }
    
    @Transactional
    public void deleteBookingDetail(Integer id) {
        BookingDetail bookingDetail = bookingDetailRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking detail not found"));
        
        // Update room status back to available
        Room room = bookingDetail.getRoom();
        room.setStatus("AVAILABLE");
        roomRepository.save(room);
        
        bookingDetailRepository.delete(bookingDetail);
    }
    
    @Transactional
    public BookingDetail updateBookingDetailStatus(Integer id, String newStatus) {
        BookingDetail bookingDetail = bookingDetailRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking detail not found"));
        
        bookingDetail.setStatus(newStatus);
        
        // Update room status if necessary
        Room room = bookingDetail.getRoom();
        switch (newStatus) {
            case "CHECKED_IN":
                room.setStatus("OCCUPIED");
                break;
            case "CHECKED_OUT":
                room.setStatus("AVAILABLE");
                break;
            case "CANCELLED":
                room.setStatus("AVAILABLE");
                break;
            default:
                // No room status change needed
                break;
        }
        roomRepository.save(room);
        
        return bookingDetailRepository.save(bookingDetail);
    }
    
    public BookingDetail getBookingDetailById(Integer id) {
        return bookingDetailRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking detail not found"));
    }

    public BookingDetail saveBookingDetail(BookingDetail bookingDetail) {
        return bookingDetailRepository.save(bookingDetail);
    }
} 