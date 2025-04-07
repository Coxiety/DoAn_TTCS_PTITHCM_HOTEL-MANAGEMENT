package com.HotelManagement.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.HotelManagement.models.Booking;
import com.HotelManagement.models.BookingDetail;
import com.HotelManagement.models.Payment;
import com.HotelManagement.models.Room;
import com.HotelManagement.models.RoomType;
import com.HotelManagement.repository.BookingDetailRepository;
import com.HotelManagement.repository.BookingRepository;
import com.HotelManagement.repository.PaymentRepository;

@Service
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;
    
    @Autowired
    private BookingRepository bookingRepository;
    
    @Autowired
    private BookingDetailRepository bookingDetailRepository;
    
    /**
     * Creates a payment record for a booking and updates the booking status
     */
    @Transactional
    public Payment createPayment(Integer bookingId, BigDecimal amount, String paymentMethod) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        
        // Create payment
        Payment payment = new Payment(booking, amount, paymentMethod);
        payment.setStatus("COMPLETED");
        paymentRepository.save(payment);
        
        // Update booking
        booking.setPaymentStatus("PAID");
        booking.setPaymentMethod(paymentMethod);
        bookingRepository.save(booking);
        
        return payment;
    }
    
    /**
     * Get all payments
     */
    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }
    
    /**
     * Get payments by status
     */
    public List<Payment> getPaymentsByStatus(String status) {
        return paymentRepository.findByStatus(status);
    }
    
    /**
     * Get payments by date range
     */
    public List<Payment> getPaymentsByDateRange(LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);
        return paymentRepository.findByPaymentDateBetween(startDateTime, endDateTime);
    }
    
    /**
     * Get revenue report data for a date range
     */
    public Map<String, Object> getRevenueReportData(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> reportData = new HashMap<>();
        
        // Get payments in date range
        List<Payment> payments = getPaymentsByDateRange(startDate, endDate);
        
        // Calculate totals
        BigDecimal totalRevenue = payments.stream()
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        int totalBookings = (int) payments.stream()
                .map(p -> p.getBooking().getId())
                .distinct()
                .count();
        
        // Get room counts from booking details
        List<Integer> bookingIds = payments.stream()
                .map(p -> p.getBooking().getId())
                .distinct()
                .collect(Collectors.toList());
        
        List<BookingDetail> allDetails = bookingDetailRepository.findByBookingIdIn(bookingIds);
        int totalRoomsBooked = allDetails.size();
        
        // Group revenue by room type
        Map<String, BigDecimal> revenueByRoomType = new HashMap<>();
        Map<String, Integer> roomsByRoomType = new HashMap<>();
        
        for (BookingDetail detail : allDetails) {
            Room room = detail.getRoom();
            RoomType roomType = room.getRoomType();
            String typeName = roomType.getName();
            
            // Add to revenue by room type
            BigDecimal typeRevenue = revenueByRoomType.getOrDefault(typeName, BigDecimal.ZERO);
            typeRevenue = typeRevenue.add(detail.getPrice());
            revenueByRoomType.put(typeName, typeRevenue);
            
            // Increment room count for this type
            int typeCount = roomsByRoomType.getOrDefault(typeName, 0);
            roomsByRoomType.put(typeName, typeCount + 1);
        }
        
        // Daily revenue calculation
        Map<String, BigDecimal> dailyRevenue = new HashMap<>();
        for (Payment payment : payments) {
            LocalDate paymentDate = payment.getPaymentDate().toLocalDate();
            String dateStr = paymentDate.toString();
            
            BigDecimal dateRevenue = dailyRevenue.getOrDefault(dateStr, BigDecimal.ZERO);
            dateRevenue = dateRevenue.add(payment.getAmount());
            dailyRevenue.put(dateStr, dateRevenue);
        }
        
        // Add all data to the report
        reportData.put("totalRevenue", totalRevenue);
        reportData.put("totalBookings", totalBookings);
        reportData.put("totalRoomsBooked", totalRoomsBooked);
        reportData.put("revenueByRoomType", revenueByRoomType);
        reportData.put("roomsByRoomType", roomsByRoomType);
        reportData.put("dailyRevenue", dailyRevenue);
        reportData.put("payments", payments);
        
        return reportData;
    }
    
    /**
     * Get a payment by ID
     */
    public Payment getPaymentById(Integer id) {
        return paymentRepository.findById(id)
                .orElse(null);
    }
    
    /**
     * Get payments for a booking
     */
    public List<Payment> getPaymentsForBooking(Integer bookingId) {
        return paymentRepository.findByBookingId(bookingId);
    }
} 