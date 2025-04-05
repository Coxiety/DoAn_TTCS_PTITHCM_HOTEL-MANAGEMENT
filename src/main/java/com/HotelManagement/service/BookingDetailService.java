package com.HotelManagement.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.HotelManagement.models.BookingDetail;
import com.HotelManagement.repository.BookingDetailRepository;

@Service
public class BookingDetailService {
    
    @Autowired
    private BookingDetailRepository bookingDetailRepository;
    
    public List<BookingDetail> getBookingDetailsByBookingId(Integer bookingId) {
        return bookingDetailRepository.findByBookingId(bookingId);
    }
} 