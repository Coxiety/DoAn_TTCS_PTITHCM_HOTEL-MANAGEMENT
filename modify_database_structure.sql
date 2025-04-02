USE [master]
GO

-- First drop any existing database with the same name
IF EXISTS (SELECT name FROM sys.databases WHERE name = 'HotelManagement')
BEGIN
    ALTER DATABASE [HotelManagement] SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
    DROP DATABASE [HotelManagement];
END
GO

-- Create the database
CREATE DATABASE [HotelManagement]
GO

USE [HotelManagement]
GO

-- Create Room_Types table with price column
CREATE TABLE [dbo].[Room_Types](
    [id] [int] IDENTITY(1,1) NOT NULL,
    [name] [varchar](100) NOT NULL,
    [description] [text] NULL,
    [capacity] [int] NULL,
    [price] [numeric](10, 2) NOT NULL,  -- Added price column here
    [amenities] [text] NULL,
    [image_path] [varchar](255) NULL,
    PRIMARY KEY CLUSTERED ([id] ASC)
)
GO

-- Create Rooms table without price column (moved to Room_Types)
CREATE TABLE [dbo].[Rooms](
    [id] [int] IDENTITY(1,1) NOT NULL,
    [type_id] [int] NOT NULL,
    [room_number] [varchar](50) NOT NULL,
    [status] [varchar](50) NOT NULL,
    [image_path] [varchar](255) NULL,
    PRIMARY KEY CLUSTERED ([id] ASC),
    CONSTRAINT [UK_room_number] UNIQUE NONCLUSTERED ([room_number] ASC)
)
GO

-- Create Users table
CREATE TABLE [dbo].[Users](
    [id] [int] IDENTITY(1,1) NOT NULL,
    [username] [varchar](100) NOT NULL,
    [password] [varchar](255) NOT NULL,
    [full_name] [varchar](255) NULL,
    [phone] [varchar](12) NULL,
    [email] [varchar](255) NULL,
    [role_id] [int] NULL,
    [address] [varchar](255) NULL,
    PRIMARY KEY CLUSTERED ([id] ASC),
    CONSTRAINT [UK_username] UNIQUE NONCLUSTERED ([username] ASC)
)
GO

-- Create unique index for email in Users table
CREATE UNIQUE NONCLUSTERED INDEX [UK_user_email] ON [dbo].[Users]
(
    [email] ASC
)
WHERE ([email] IS NOT NULL)
GO

-- Create Customers table
CREATE TABLE [dbo].[Customers](
    [id] [int] IDENTITY(1,1) NOT NULL,
    [full_name] [varchar](255) NOT NULL,
    [phone] [varchar](12) NOT NULL,
    [email] [varchar](255) NULL,
    [address] [varchar](255) NULL,
    [booking_history] [text] NULL,
    PRIMARY KEY CLUSTERED ([id] ASC),
    CONSTRAINT [UK_customer_phone] UNIQUE NONCLUSTERED ([phone] ASC)
)
GO

-- Create unique index for email in Customers table
CREATE UNIQUE NONCLUSTERED INDEX [UK_customer_email] ON [dbo].[Customers]
(
    [email] ASC
)
WHERE ([email] IS NOT NULL)
GO

-- Create Bookings table
CREATE TABLE [dbo].[Bookings](
    [id] [int] IDENTITY(1,1) NOT NULL,
    [customer_id] [int] NOT NULL,
    [check_in_date] [datetime] NOT NULL,
    [status] [varchar](50) NOT NULL,
    [user_id] [int] NULL,
    PRIMARY KEY CLUSTERED ([id] ASC)
)
GO

-- Create Booking_Details table
CREATE TABLE [dbo].[Booking_Details](
    [id] [int] IDENTITY(1,1) NOT NULL,
    [booking_id] [int] NOT NULL,
    [room_id] [int] NOT NULL,
    [price] [numeric](10, 2) NOT NULL,  -- Keep price here to record the price at booking time
    [check_in_date] [datetime2](6) NOT NULL,
    [status] [varchar](50) NOT NULL,
    PRIMARY KEY CLUSTERED ([id] ASC)
)
GO

-- Create Invoices table
CREATE TABLE [dbo].[Invoices](
    [id] [int] IDENTITY(1,1) NOT NULL,
    [booking_id] [int] NOT NULL,
    [total_amount] [numeric](10, 2) NOT NULL,
    [user_id] [int] NULL,
    [issued_date] [datetime2](6) NOT NULL,
    [status] [varchar](50) NOT NULL,
    PRIMARY KEY CLUSTERED ([id] ASC)
)
GO

-- Create Payments table
CREATE TABLE [dbo].[Payments](
    [id] [int] IDENTITY(1,1) NOT NULL,
    [booking_id] [int] NOT NULL,
    [amount] [numeric](10, 2) NOT NULL,
    [user_id] [int] NULL,
    [payment_date] [datetime2](6) NOT NULL,
    [payment_method] [varchar](50) NOT NULL,
    [status] [varchar](50) NOT NULL,
    PRIMARY KEY CLUSTERED ([id] ASC)
)
GO

-- Add foreign key constraints
ALTER TABLE [dbo].[Rooms]  WITH CHECK ADD  CONSTRAINT [FK_Rooms_RoomTypes] FOREIGN KEY([type_id])
REFERENCES [dbo].[Room_Types] ([id])
GO
ALTER TABLE [dbo].[Rooms] CHECK CONSTRAINT [FK_Rooms_RoomTypes]
GO

ALTER TABLE [dbo].[Booking_Details]  WITH CHECK ADD  CONSTRAINT [FK_BookingDetails_Rooms] FOREIGN KEY([room_id])
REFERENCES [dbo].[Rooms] ([id])
GO
ALTER TABLE [dbo].[Booking_Details] CHECK CONSTRAINT [FK_BookingDetails_Rooms]
GO

ALTER TABLE [dbo].[Booking_Details]  WITH CHECK ADD  CONSTRAINT [FK_BookingDetails_Bookings] FOREIGN KEY([booking_id])
REFERENCES [dbo].[Bookings] ([id])
GO
ALTER TABLE [dbo].[Booking_Details] CHECK CONSTRAINT [FK_BookingDetails_Bookings]
GO

ALTER TABLE [dbo].[Bookings]  WITH CHECK ADD FOREIGN KEY([customer_id])
REFERENCES [dbo].[Customers] ([id])
GO

ALTER TABLE [dbo].[Bookings]  WITH CHECK ADD  CONSTRAINT [FK_Bookings_Users] FOREIGN KEY([user_id])
REFERENCES [dbo].[Users] ([id])
GO
ALTER TABLE [dbo].[Bookings] CHECK CONSTRAINT [FK_Bookings_Users]
GO

ALTER TABLE [dbo].[Invoices]  WITH CHECK ADD  CONSTRAINT [FK_Invoices_Users] FOREIGN KEY([user_id])
REFERENCES [dbo].[Users] ([id])
GO
ALTER TABLE [dbo].[Invoices] CHECK CONSTRAINT [FK_Invoices_Users]
GO

ALTER TABLE [dbo].[Invoices]  WITH CHECK ADD  CONSTRAINT [FK_Invoices_Bookings] FOREIGN KEY([booking_id])
REFERENCES [dbo].[Bookings] ([id])
GO
ALTER TABLE [dbo].[Invoices] CHECK CONSTRAINT [FK_Invoices_Bookings]
GO

ALTER TABLE [dbo].[Payments]  WITH CHECK ADD  CONSTRAINT [FK_Payments_Bookings] FOREIGN KEY([booking_id])
REFERENCES [dbo].[Bookings] ([id])
GO
ALTER TABLE [dbo].[Payments] CHECK CONSTRAINT [FK_Payments_Bookings]
GO

ALTER TABLE [dbo].[Payments]  WITH CHECK ADD  CONSTRAINT [FK_Payments_Users] FOREIGN KEY([user_id])
REFERENCES [dbo].[Users] ([id])
GO
ALTER TABLE [dbo].[Payments] CHECK CONSTRAINT [FK_Payments_Users]
GO

-- Insert initial admin user
INSERT INTO [dbo].[Users] ([username], [password], [full_name], [role_id])
VALUES ('admin', 'admin', 'System Administrator', 3)
GO

-- Insert some sample room types with pricing
INSERT INTO [dbo].[Room_Types] ([name], [description], [capacity], [price], [amenities])
VALUES 
('Standard', 'Comfortable standard room with basic amenities', 2, 100.00, 'WiFi, TV, Air Conditioning'),
('Deluxe', 'Spacious room with premium amenities', 2, 150.00, 'WiFi, TV, Air Conditioning, Mini Bar, Coffee Maker'),
('Suite', 'Luxury suite with separate living area', 4, 250.00, 'WiFi, TV, Air Conditioning, Mini Bar, Coffee Maker, Bathtub, Living Area'),
('Family', 'Large room designed for families', 6, 200.00, 'WiFi, TV, Air Conditioning, Mini Bar, Coffee Maker, Extra Beds')
GO