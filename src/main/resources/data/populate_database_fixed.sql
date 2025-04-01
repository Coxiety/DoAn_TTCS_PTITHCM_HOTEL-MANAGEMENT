USE [HotelManagement]
GO

-- Clear existing data
DELETE FROM [dbo].[Booking_Details]
GO
DELETE FROM [dbo].[Invoices]
GO
DELETE FROM [dbo].[Payments]
GO
DELETE FROM [dbo].[Bookings]
GO
DELETE FROM [dbo].[Rooms]
GO
DELETE FROM [dbo].[Room_Types]
GO
DELETE FROM [dbo].[Customers]
GO
DELETE FROM [dbo].[Users]
GO

-- Reset identity columns
DBCC CHECKIDENT ('[dbo].[Users]', RESEED, 0)
GO
DBCC CHECKIDENT ('[dbo].[Customers]', RESEED, 0)
GO
DBCC CHECKIDENT ('[dbo].[Room_Types]', RESEED, 0)
GO
DBCC CHECKIDENT ('[dbo].[Rooms]', RESEED, 0)
GO
DBCC CHECKIDENT ('[dbo].[Bookings]', RESEED, 0)
GO
DBCC CHECKIDENT ('[dbo].[Booking_Details]', RESEED, 0)
GO
DBCC CHECKIDENT ('[dbo].[Invoices]', RESEED, 0)
GO
DBCC CHECKIDENT ('[dbo].[Payments]', RESEED, 0)
GO

-- Insert Users data
PRINT 'Inserting Users data...'
INSERT INTO [dbo].[Users] ([username], [password], [full_name], [phone], [email], [role_id])
VALUES 
('receptionist', 'password123', 'John Receptionist', '0901234567', 'receptionist@hotel.com', 1),
('admin', 'admin123', 'Admin User', '0909876543', 'admin@hotel.com', 3),
('customer1', 'cust123', 'Alice Johnson', '0901112222', 'alice@example.com', 2),
('customer2', 'cust456', 'Bob Smith', '0903334444', 'bob@example.com', 2),
('customer3', 'cust789', 'Carol Davis', '0905556666', 'carol@example.com', 2)
GO

-- Insert Customers data
PRINT 'Inserting Customers data...'
INSERT INTO [dbo].[Customers] ([full_name], [phone], [email], [address], [booking_history])
VALUES
('Alice Johnson', '0901112233', 'alice.j@example.com', '789 Customer Road, City', NULL),
('Bob Smith', '0903334455', 'bob.s@example.com', '101 Guest Boulevard, Town', NULL),
('Carol Davis', '0905556677', 'carol.d@example.com', '202 Visitor Lane, Village', NULL),
('David Wilson', '0907778899', 'david.w@example.com', '303 Traveler Street, County', NULL),
('Eva Brown', '0909990011', 'eva.b@example.com', '404 Tourist Avenue, District', NULL)
GO

-- Insert Room Types data
PRINT 'Inserting Room Types data...'
INSERT INTO [dbo].[Room_Types] ([name], [description], [capacity], [amenities])
VALUES
('Standard', 'Comfortable standard room with essential amenities', 2, 'WiFi, TV, Air Conditioning, Private Bathroom'),
('Deluxe', 'Spacious deluxe room with premium furnishings', 3, 'WiFi, Smart TV, Climate Control, Mini Bar, Room Service, City View'),
('Suite', 'Luxurious suite with separate living area', 4, 'WiFi, 2 Smart TVs, Kitchen Area, Living Room, Premium View, Butler Service'),
('Family', 'Large room ideal for families with children', 5, 'WiFi, TV, Extra Beds, Family Amenities, Game Console'),
('Executive', 'Premium room with executive benefits and workspace', 2, 'WiFi, Business Desk, Meeting Area, Complimentary Breakfast')
GO

-- Insert Rooms data
PRINT 'Inserting Rooms data...'
INSERT INTO [dbo].[Rooms] ([room_number], [type_id], [price], [status])
VALUES
('101', 1, 100.00, 'AVAILABLE'),
('102', 1, 100.00, 'AVAILABLE'),
('103', 1, 100.00, 'AVAILABLE'),
('104', 1, 100.00, 'AVAILABLE'),
('105', 1, 100.00, 'AVAILABLE'),
('201', 2, 150.00, 'AVAILABLE'),
('202', 2, 150.00, 'AVAILABLE'),
('203', 2, 150.00, 'AVAILABLE'),
('301', 3, 200.00, 'AVAILABLE'),
('302', 3, 200.00, 'AVAILABLE'),
('401', 4, 250.00, 'AVAILABLE'),
('402', 4, 250.00, 'AVAILABLE'),
('501', 5, 180.00, 'AVAILABLE'),
('502', 5, 180.00, 'AVAILABLE')
GO

-- First booking: Future booking (tomorrow)
PRINT 'Creating booking with check-in tomorrow...'
BEGIN
    DECLARE @User1Id INT = (SELECT id FROM [dbo].[Users] WHERE username = 'receptionist')
    DECLARE @Customer1Id INT = (SELECT id FROM [dbo].[Customers] WHERE phone = '0901112233')
    DECLARE @Room1Id INT = (SELECT id FROM [dbo].[Rooms] WHERE room_number = '101')
    
    INSERT INTO [dbo].[Bookings] ([customer_id], [check_in_date], [status], [user_id])
    VALUES (@Customer1Id, DATEADD(day, 1, GETDATE()), 'CONFIRMED', @User1Id)
    
    DECLARE @Booking1Id INT = SCOPE_IDENTITY()
    
    INSERT INTO [dbo].[Booking_Details] ([booking_id], [room_id], [check_in_date], [price], [status])
    VALUES (@Booking1Id, @Room1Id, DATEADD(day, 1, GETDATE()), 100.00, 'CONFIRMED')
    
    UPDATE [dbo].[Rooms] SET [status] = 'BOOKED' WHERE [id] = @Room1Id
END
GO

-- Second booking: Today's booking
PRINT 'Creating booking with check-in today...'
BEGIN
    DECLARE @User1Id INT = (SELECT id FROM [dbo].[Users] WHERE username = 'receptionist')
    DECLARE @Customer2Id INT = (SELECT id FROM [dbo].[Customers] WHERE phone = '0903334455')
    DECLARE @Room2Id INT = (SELECT id FROM [dbo].[Rooms] WHERE room_number = '102')
    
    INSERT INTO [dbo].[Bookings] ([customer_id], [check_in_date], [status], [user_id])
    VALUES (@Customer2Id, GETDATE(), 'CONFIRMED', @User1Id)
    
    DECLARE @Booking2Id INT = SCOPE_IDENTITY()
    
    INSERT INTO [dbo].[Booking_Details] ([booking_id], [room_id], [check_in_date], [price], [status])
    VALUES (@Booking2Id, @Room2Id, GETDATE(), 100.00, 'CONFIRMED')
    
    UPDATE [dbo].[Rooms] SET [status] = 'BOOKED' WHERE [id] = @Room2Id
END
GO

-- Third booking: Active booking (checked-in)
PRINT 'Creating active booking (checked-in)...'
BEGIN
    DECLARE @User1Id INT = (SELECT id FROM [dbo].[Users] WHERE username = 'receptionist')
    DECLARE @Customer3Id INT = (SELECT id FROM [dbo].[Customers] WHERE phone = '0905556677')
    DECLARE @Room3Id INT = (SELECT id FROM [dbo].[Rooms] WHERE room_number = '103')
    
    INSERT INTO [dbo].[Bookings] ([customer_id], [check_in_date], [status], [user_id])
    VALUES (@Customer3Id, DATEADD(day, -2, GETDATE()), 'CHECKED_IN', @User1Id)
    
    DECLARE @Booking3Id INT = SCOPE_IDENTITY()
    
    INSERT INTO [dbo].[Booking_Details] ([booking_id], [room_id], [check_in_date], [price], [status])
    VALUES (@Booking3Id, @Room3Id, DATEADD(day, -2, GETDATE()), 100.00, 'OCCUPIED')
    
    UPDATE [dbo].[Rooms] SET [status] = 'OCCUPIED' WHERE [id] = @Room3Id
END
GO

-- Fourth booking: Historical booking (completed) with invoice and payment
PRINT 'Creating historical booking (completed)...'
BEGIN
    DECLARE @User1Id INT = (SELECT id FROM [dbo].[Users] WHERE username = 'receptionist')
    DECLARE @Customer4Id INT = (SELECT id FROM [dbo].[Customers] WHERE phone = '0907778899')
    DECLARE @Room4Id INT = (SELECT id FROM [dbo].[Rooms] WHERE room_number = '104')
    
    INSERT INTO [dbo].[Bookings] ([customer_id], [check_in_date], [status], [user_id])
    VALUES (@Customer4Id, DATEADD(day, -10, GETDATE()), 'COMPLETED', @User1Id)
    
    DECLARE @Booking4Id INT = SCOPE_IDENTITY()
    
    INSERT INTO [dbo].[Booking_Details] ([booking_id], [room_id], [check_in_date], [price], [status])
    VALUES (@Booking4Id, @Room4Id, DATEADD(day, -10, GETDATE()), 100.00, 'CHECKED_OUT')
    
    -- Create invoice for the completed booking
    INSERT INTO [dbo].[Invoices] ([booking_id], [total_amount], [user_id], [issued_date], [status])
    VALUES (@Booking4Id, 200.00, @User1Id, DATEADD(day, -8, GETDATE()), 'PAID')
    
    -- Create payment for the invoice
    INSERT INTO [dbo].[Payments] ([booking_id], [amount], [user_id], [payment_date], [payment_method], [status])
    VALUES (@Booking4Id, 200.00, @User1Id, DATEADD(day, -8, GETDATE()), 'CREDIT_CARD', 'COMPLETED')
END
GO

-- Fifth booking: Deluxe room booking
PRINT 'Creating deluxe room booking...'
BEGIN
    DECLARE @User1Id INT = (SELECT id FROM [dbo].[Users] WHERE username = 'receptionist')
    DECLARE @Customer5Id INT = (SELECT id FROM [dbo].[Customers] WHERE phone = '0909990011')
    DECLARE @Room5Id INT = (SELECT id FROM [dbo].[Rooms] WHERE room_number = '201')
    
    INSERT INTO [dbo].[Bookings] ([customer_id], [check_in_date], [status], [user_id])
    VALUES (@Customer5Id, DATEADD(day, 3, GETDATE()), 'CONFIRMED', @User1Id)
    
    DECLARE @Booking5Id INT = SCOPE_IDENTITY()
    
    INSERT INTO [dbo].[Booking_Details] ([booking_id], [room_id], [check_in_date], [price], [status])
    VALUES (@Booking5Id, @Room5Id, DATEADD(day, 3, GETDATE()), 150.00, 'CONFIRMED')
    
    UPDATE [dbo].[Rooms] SET [status] = 'BOOKED' WHERE [id] = @Room5Id
END
GO

PRINT 'Database has been successfully populated with test data.'
GO