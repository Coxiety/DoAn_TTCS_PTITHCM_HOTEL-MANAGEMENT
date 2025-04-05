USE [HotelManagement]
GO

-- Disable foreign key constraints temporarily
EXEC sp_MSforeachtable "ALTER TABLE ? NOCHECK CONSTRAINT all"

-- Clear existing data
DELETE FROM [dbo].[Payments]
DELETE FROM [dbo].[Invoices]
DELETE FROM [dbo].[Booking_Details]
DELETE FROM [dbo].[Bookings]
DELETE FROM [dbo].[Customers]
DELETE FROM [dbo].[Rooms]
DELETE FROM [dbo].[Room_Types]
DELETE FROM [dbo].[Users]

-- Reset identity columns
DBCC CHECKIDENT ('[dbo].[Payments]', RESEED, 0)
DBCC CHECKIDENT ('[dbo].[Invoices]', RESEED, 0)
DBCC CHECKIDENT ('[dbo].[Booking_Details]', RESEED, 0)
DBCC CHECKIDENT ('[dbo].[Bookings]', RESEED, 0)
DBCC CHECKIDENT ('[dbo].[Customers]', RESEED, 0)
DBCC CHECKIDENT ('[dbo].[Rooms]', RESEED, 0)
DBCC CHECKIDENT ('[dbo].[Room_Types]', RESEED, 0)
DBCC CHECKIDENT ('[dbo].[Users]', RESEED, 0)

-- 1. Insert User Data
INSERT INTO [dbo].[Users] ([username], [password], [full_name], [phone], [email], [role_id], [address])
VALUES 
    ('admin', 'YWRtaW4=', 'System Administrator', '0123456789', 'admin@luxuryhotel.com', 3, 'Hotel Address'),
    ('receptionist1', 'cGFzc3dvcmQ=', 'Jane Smith', '0123456788', 'jane@luxuryhotel.com', 1, '123 Staff St'),
    ('receptionist2', 'cGFzc3dvcmQ=', 'John Doe', '0123456787', 'john@luxuryhotel.com', 1, '456 Staff Ave'),
    ('customer1', 'cGFzc3dvcmQ=', 'Alice Johnson', '0987654321', 'alice@example.com', 2, '789 Customer Rd'),
    ('customer2', 'cGFzc3dvcmQ=', 'Bob Williams', '0987654322', 'bob@example.com', 2, '012 Client St'),
    ('customer3', 'cGFzc3dvcmQ=', 'Carol Brown', '0987654323', 'carol@example.com', 2, '345 Guest Blvd');

-- 2. Insert Room Types
INSERT INTO [dbo].[Room_Types] ([name], [description], [capacity], [price], [amenities], [image_path])
VALUES 
    ('Standard Room', 'Comfortable room with basic amenities. Perfect for short stays and budget travelers.', 2, 100.00, 'WiFi, TV, Air Conditioning, Mini Fridge', '/img/rooms/standard.jpg'),
    ('Deluxe Room', 'Spacious room with premium amenities and city view. Ideal for business travelers.', 2, 150.00, 'WiFi, Smart TV, Air Conditioning, Mini Bar, Coffee Maker, Premium Toiletries', '/img/rooms/deluxe.jpg'),
    ('Suite', 'Luxurious suite with separate living area and panoramic views. Perfect for extended stays.', 4, 250.00, 'WiFi, Smart TV, Air Conditioning, Full Bar, Coffee Maker, Premium Toiletries, Bathtub, Living Room', '/img/rooms/suite.jpg'),
    ('Family Room', 'Large room designed for families with children. Offers comfort for the whole family.', 6, 200.00, 'WiFi, TV, Air Conditioning, Mini Fridge, Extra Beds, Child-friendly Amenities', '/img/rooms/family.jpg'),
    ('Executive Suite', 'Premium suite with office space and luxury amenities. Ideal for business executives.', 2, 300.00, 'WiFi, Smart TV, Air Conditioning, Full Bar, Coffee Machine, Premium Toiletries, Office Desk, Living Room', '/img/rooms/executive.jpg');

-- 3. Insert Rooms
INSERT INTO [dbo].[Rooms] ([type_id], [room_number], [status], [image_path])
VALUES 
    -- Standard Rooms (101-110)
    (1, '101', 'AVAILABLE', '/img/rooms/101.jpg'),
    (1, '102', 'AVAILABLE', '/img/rooms/102.jpg'),
    (1, '103', 'AVAILABLE', '/img/rooms/103.jpg'),
    (1, '104', 'AVAILABLE', '/img/rooms/104.jpg'),
    (1, '105', 'AVAILABLE', '/img/rooms/105.jpg'),
    (1, '106', 'MAINTENANCE', '/img/rooms/106.jpg'),
    (1, '107', 'AVAILABLE', '/img/rooms/107.jpg'),
    (1, '108', 'AVAILABLE', '/img/rooms/108.jpg'),
    (1, '109', 'AVAILABLE', '/img/rooms/109.jpg'),
    (1, '110', 'AVAILABLE', '/img/rooms/110.jpg'),
    
    -- Deluxe Rooms (201-210)
    (2, '201', 'AVAILABLE', '/img/rooms/201.jpg'),
    (2, '202', 'AVAILABLE', '/img/rooms/202.jpg'),
    (2, '203', 'AVAILABLE', '/img/rooms/203.jpg'),
    (2, '204', 'AVAILABLE', '/img/rooms/204.jpg'),
    (2, '205', 'AVAILABLE', '/img/rooms/205.jpg'),
    (2, '206', 'AVAILABLE', '/img/rooms/206.jpg'),
    (2, '207', 'AVAILABLE', '/img/rooms/207.jpg'),
    (2, '208', 'MAINTENANCE', '/img/rooms/208.jpg'),
    (2, '209', 'AVAILABLE', '/img/rooms/209.jpg'),
    (2, '210', 'AVAILABLE', '/img/rooms/210.jpg'),
    
    -- Suites (301-305)
    (3, '301', 'AVAILABLE', '/img/rooms/301.jpg'),
    (3, '302', 'AVAILABLE', '/img/rooms/302.jpg'),
    (3, '303', 'AVAILABLE', '/img/rooms/303.jpg'),
    (3, '304', 'AVAILABLE', '/img/rooms/304.jpg'),
    (3, '305', 'AVAILABLE', '/img/rooms/305.jpg'),
    
    -- Family Rooms (401-405)
    (4, '401', 'AVAILABLE', '/img/rooms/401.jpg'),
    (4, '402', 'AVAILABLE', '/img/rooms/402.jpg'),
    (4, '403', 'AVAILABLE', '/img/rooms/403.jpg'),
    (4, '404', 'AVAILABLE', '/img/rooms/404.jpg'),
    (4, '405', 'MAINTENANCE', '/img/rooms/405.jpg'),
    
    -- Executive Suites (501-503)
    (5, '501', 'AVAILABLE', '/img/rooms/501.jpg'),
    (5, '502', 'AVAILABLE', '/img/rooms/502.jpg'),
    (5, '503', 'AVAILABLE', '/img/rooms/503.jpg');

-- 4. Insert Customers
INSERT INTO [dbo].[Customers] ([full_name], [phone], [email], [address], [booking_history])
VALUES 
    ('Alice Johnson', '0987654321', 'alice@example.com', '789 Customer Rd, City', NULL),
    ('Bob Williams', '0987654322', 'bob@example.com', '012 Client St, Town', NULL),
    ('Carol Brown', '0987654323', 'carol@example.com', '345 Guest Blvd, Village', NULL),
    ('David Miller', '0987654324', 'david@example.com', '678 Visitor Ave, City', NULL),
    ('Emma Wilson', '0987654325', 'emma@example.com', '901 Traveler St, Town', NULL),
    ('Frank Davis', '0987654326', 'frank@example.com', '234 Tourist Rd, City', NULL),
    ('Grace Taylor', '0987654327', 'grace@example.com', '567 Lodger Ave, Village', NULL),
    ('Henry Clark', '0987654328', 'henry@example.com', '890 Boarder St, City', NULL),
    ('Ivy Garcia', '0987654329', 'ivy@example.com', '123 Resident Rd, Town', NULL),
    ('Jack Martinez', '0987654330', 'jack@example.com', '456 Tenant Ave, City', NULL);

-- 5. Insert Bookings (Some past, some current, some future)
-- Past bookings
INSERT INTO [dbo].[Bookings] ([customer_id], [check_in_date], [status], [user_id])
VALUES 
    (1, '2025-03-01', 'COMPLETED', 2),
    (2, '2025-03-05', 'COMPLETED', 2),
    (3, '2025-03-10', 'COMPLETED', 3),
    (4, '2025-03-15', 'COMPLETED', 2);

-- Current bookings
INSERT INTO [dbo].[Bookings] ([customer_id], [check_in_date], [status], [user_id])
VALUES 
    (5, '2025-04-01', 'ACTIVE', 3),
    (6, '2025-04-02', 'ACTIVE', 2);

-- Future bookings
INSERT INTO [dbo].[Bookings] ([customer_id], [check_in_date], [status], [user_id])
VALUES 
    (7, '2025-04-10', 'CONFIRMED', 3),
    (8, '2025-04-15', 'CONFIRMED', 2),
    (9, '2025-04-20', 'CONFIRMED', 3),
    (10, '2025-04-25', 'CONFIRMED', 2);

-- 6. Insert Booking Details
-- Past bookings
INSERT INTO [dbo].[Booking_Details] ([booking_id], [room_id], [price], [check_in_date], [status])
VALUES 
    (1, 3, 100.00, '2025-03-01 14:00:00', 'COMPLETED'),
    (1, 4, 100.00, '2025-03-01 14:00:00', 'COMPLETED'),
    (2, 12, 150.00, '2025-03-05 15:00:00', 'COMPLETED'),
    (3, 22, 250.00, '2025-03-10 13:00:00', 'COMPLETED'),
    (4, 27, 200.00, '2025-03-15 14:00:00', 'COMPLETED');

-- Current bookings (rooms marked as occupied)
INSERT INTO [dbo].[Booking_Details] ([booking_id], [room_id], [price], [check_in_date], [status])
VALUES 
    (5, 32, 300.00, '2025-04-01 12:00:00', 'OCCUPIED'),
    (6, 23, 250.00, '2025-04-02 13:00:00', 'OCCUPIED');

-- Update room status for occupied rooms
UPDATE [dbo].[Rooms] SET [status] = 'OCCUPIED' WHERE [id] IN (32, 23);

-- Future bookings (rooms marked as booked)
INSERT INTO [dbo].[Booking_Details] ([booking_id], [room_id], [price], [check_in_date], [status])
VALUES 
    (7, 13, 150.00, '2025-04-10 14:00:00', 'BOOKED'),
    (8, 28, 200.00, '2025-04-15 15:00:00', 'BOOKED'),
    (9, 33, 300.00, '2025-04-20 16:00:00', 'BOOKED'),
    (10, 24, 250.00, '2025-04-25 13:00:00', 'BOOKED');

-- Update room status for booked rooms
UPDATE [dbo].[Rooms] SET [status] = 'BOOKED' WHERE [id] IN (13, 28, 33, 24);

-- 7. Insert Invoices for completed bookings
INSERT INTO [dbo].[Invoices] ([booking_id], [total_amount], [user_id], [issued_date], [status])
VALUES 
    (1, 200.00, 2, '2025-03-03 11:00:00', 'PAID'),
    (2, 150.00, 2, '2025-03-07 10:30:00', 'PAID'),
    (3, 250.00, 3, '2025-03-12 12:00:00', 'PAID'),
    (4, 200.00, 2, '2025-03-17 11:45:00', 'PAID');

-- 8. Insert Payments for paid invoices
INSERT INTO [dbo].[Payments] ([booking_id], [amount], [user_id], [payment_date], [payment_method], [status])
VALUES 
    (1, 200.00, 2, '2025-03-03 11:15:00', 'CREDIT_CARD', 'COMPLETED'),
    (2, 150.00, 2, '2025-03-07 10:45:00', 'CASH', 'COMPLETED'),
    (3, 250.00, 3, '2025-03-12 12:15:00', 'CREDIT_CARD', 'COMPLETED'),
    (4, 200.00, 2, '2025-03-17 12:00:00', 'DEBIT_CARD', 'COMPLETED');

-- Re-enable foreign key constraints
EXEC sp_MSforeachtable "ALTER TABLE ? CHECK CONSTRAINT all"

-- Confirmation message
PRINT 'Sample data has been successfully inserted into the HotelManagement database.'