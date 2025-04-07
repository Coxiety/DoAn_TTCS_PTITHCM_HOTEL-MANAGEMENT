USE [master]
GO
/****** Object:  Database [Hotel_Management]    Script Date: 06/04/2025 2:05:39 CH ******/
CREATE DATABASE [Hotel_Management]
 CONTAINMENT = NONE
 ON  PRIMARY 
( NAME = N'Hotel_Management', FILENAME = N'E:\Software\SQL2022\MSSQL16.MSSQLSERVER\MSSQL\DATA\Hotel_Management.mdf' , SIZE = 8192KB , MAXSIZE = UNLIMITED, FILEGROWTH = 65536KB )
 LOG ON 
( NAME = N'Hotel_Management_log', FILENAME = N'E:\Software\SQL2022\MSSQL16.MSSQLSERVER\MSSQL\DATA\Hotel_Management_log.ldf' , SIZE = 8192KB , MAXSIZE = 2048GB , FILEGROWTH = 65536KB )
 WITH CATALOG_COLLATION = DATABASE_DEFAULT, LEDGER = OFF
GO
ALTER DATABASE [Hotel_Management] SET COMPATIBILITY_LEVEL = 160
GO
IF (1 = FULLTEXTSERVICEPROPERTY('IsFullTextInstalled'))
begin
EXEC [Hotel_Management].[dbo].[sp_fulltext_database] @action = 'enable'
end
GO
ALTER DATABASE [Hotel_Management] SET ANSI_NULL_DEFAULT OFF 
GO
ALTER DATABASE [Hotel_Management] SET ANSI_NULLS OFF 
GO
ALTER DATABASE [Hotel_Management] SET ANSI_PADDING OFF 
GO
ALTER DATABASE [Hotel_Management] SET ANSI_WARNINGS OFF 
GO
ALTER DATABASE [Hotel_Management] SET ARITHABORT OFF 
GO
ALTER DATABASE [Hotel_Management] SET AUTO_CLOSE OFF 
GO
ALTER DATABASE [Hotel_Management] SET AUTO_SHRINK OFF 
GO
ALTER DATABASE [Hotel_Management] SET AUTO_UPDATE_STATISTICS ON 
GO
ALTER DATABASE [Hotel_Management] SET CURSOR_CLOSE_ON_COMMIT OFF 
GO
ALTER DATABASE [Hotel_Management] SET CURSOR_DEFAULT  GLOBAL 
GO
ALTER DATABASE [Hotel_Management] SET CONCAT_NULL_YIELDS_NULL OFF 
GO
ALTER DATABASE [Hotel_Management] SET NUMERIC_ROUNDABORT OFF 
GO
ALTER DATABASE [Hotel_Management] SET QUOTED_IDENTIFIER OFF 
GO
ALTER DATABASE [Hotel_Management] SET RECURSIVE_TRIGGERS OFF 
GO
ALTER DATABASE [Hotel_Management] SET  ENABLE_BROKER 
GO
ALTER DATABASE [Hotel_Management] SET AUTO_UPDATE_STATISTICS_ASYNC OFF 
GO
ALTER DATABASE [Hotel_Management] SET DATE_CORRELATION_OPTIMIZATION OFF 
GO
ALTER DATABASE [Hotel_Management] SET TRUSTWORTHY OFF 
GO
ALTER DATABASE [Hotel_Management] SET ALLOW_SNAPSHOT_ISOLATION OFF 
GO
ALTER DATABASE [Hotel_Management] SET PARAMETERIZATION SIMPLE 
GO
ALTER DATABASE [Hotel_Management] SET READ_COMMITTED_SNAPSHOT OFF 
GO
ALTER DATABASE [Hotel_Management] SET HONOR_BROKER_PRIORITY OFF 
GO
ALTER DATABASE [Hotel_Management] SET RECOVERY FULL 
GO
ALTER DATABASE [Hotel_Management] SET  MULTI_USER 
GO
ALTER DATABASE [Hotel_Management] SET PAGE_VERIFY CHECKSUM  
GO
ALTER DATABASE [Hotel_Management] SET DB_CHAINING OFF 
GO
ALTER DATABASE [Hotel_Management] SET FILESTREAM( NON_TRANSACTED_ACCESS = OFF ) 
GO
ALTER DATABASE [Hotel_Management] SET TARGET_RECOVERY_TIME = 60 SECONDS 
GO
ALTER DATABASE [Hotel_Management] SET DELAYED_DURABILITY = DISABLED 
GO
ALTER DATABASE [Hotel_Management] SET ACCELERATED_DATABASE_RECOVERY = OFF  
GO
EXEC sys.sp_db_vardecimal_storage_format N'Hotel_Management', N'ON'
GO
ALTER DATABASE [Hotel_Management] SET QUERY_STORE = ON
GO
ALTER DATABASE [Hotel_Management] SET QUERY_STORE (OPERATION_MODE = READ_WRITE, CLEANUP_POLICY = (STALE_QUERY_THRESHOLD_DAYS = 30), DATA_FLUSH_INTERVAL_SECONDS = 900, INTERVAL_LENGTH_MINUTES = 60, MAX_STORAGE_SIZE_MB = 1000, QUERY_CAPTURE_MODE = AUTO, SIZE_BASED_CLEANUP_MODE = AUTO, MAX_PLANS_PER_QUERY = 200, WAIT_STATS_CAPTURE_MODE = ON)
GO
USE [Hotel_Management]
GO
/****** Object:  User [admin]    Script Date: 06/04/2025 2:05:40 CH ******/
CREATE USER [admin] FOR LOGIN [admin] WITH DEFAULT_SCHEMA=[dbo]
GO
/****** Object:  DatabaseRole [HotelReceptionist]    Script Date: 06/04/2025 2:05:40 CH ******/
CREATE ROLE [HotelReceptionist]
GO
/****** Object:  DatabaseRole [HotelCustomer]    Script Date: 06/04/2025 2:05:40 CH ******/
CREATE ROLE [HotelCustomer]
GO
/****** Object:  DatabaseRole [HotelAdmin]    Script Date: 06/04/2025 2:05:40 CH ******/
CREATE ROLE [HotelAdmin]
GO
/****** Object:  Table [dbo].[room_type]    Script Date: 06/04/2025 2:05:40 CH ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[room_type](
	[id] [int] IDENTITY(1,1) NOT NULL,
	[name] [varchar](50) NOT NULL,
	[description] [varchar](max) NULL,
	[capacity] [int] NOT NULL,
	[price] [decimal](10, 2) NOT NULL,
	[amenities] [varchar](max) NULL,
	[image_path] [varchar](255) NULL,
	[created_at] [datetime] NULL,
	[updated_at] [datetime] NULL,
PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
GO
/****** Object:  Table [dbo].[room]    Script Date: 06/04/2025 2:05:40 CH ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[room](
	[id] [int] IDENTITY(1,1) NOT NULL,
	[room_number] [varchar](10) NOT NULL,
	[room_type_id] [int] NOT NULL,
	[status] [varchar](20) NOT NULL,
	[floor] [int] NULL,
	[created_at] [datetime] NULL,
	[updated_at] [datetime] NULL,
PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY],
UNIQUE NONCLUSTERED 
(
	[room_number] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  View [dbo].[vw_available_rooms]    Script Date: 06/04/2025 2:05:40 CH ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO

-- =====================================================
-- Create Views
-- =====================================================

CREATE VIEW [dbo].[vw_available_rooms] AS
SELECT r.id, r.room_number, r.status, r.floor,
       rt.id AS room_type_id, rt.name AS room_type_name, rt.description, rt.capacity, rt.price, rt.amenities, rt.image_path
FROM room r
JOIN room_type rt ON r.room_type_id = rt.id
WHERE r.status = 'AVAILABLE';
GO
/****** Object:  Table [dbo].[booking]    Script Date: 06/04/2025 2:05:40 CH ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[booking](
	[id] [int] IDENTITY(1,1) NOT NULL,
	[customer_id] [int] NOT NULL,
	[user_id] [int] NULL,
	[check_in_date] [datetime] NOT NULL,
	[check_out_date] [datetime] NULL,
	[status] [varchar](20) NOT NULL,
	[total_amount] [decimal](10, 2) NOT NULL,
	[payment_status] [varchar](20) NOT NULL,
	[payment_method] [varchar](50) NULL,
	[notes] [varchar](max) NULL,
	[created_at] [datetime] NULL,
	[updated_at] [datetime] NULL,
PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
GO
/****** Object:  Table [dbo].[users]    Script Date: 06/04/2025 2:05:40 CH ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[users](
	[id] [int] IDENTITY(1,1) NOT NULL,
	[username] [varchar](50) NOT NULL,
	[password] [varchar](255) NOT NULL,
	[email] [varchar](100) NOT NULL,
	[full_name] [varchar](100) NOT NULL,
	[phone] [varchar](20) NULL,
	[role_id] [int] NOT NULL,
	[created_at] [datetime] NULL,
	[updated_at] [datetime] NULL,
PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY],
 CONSTRAINT [UK_23y4gd49ajvbqgl3psjsvhff6] UNIQUE NONCLUSTERED 
(
	[username] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY],
UNIQUE NONCLUSTERED 
(
	[email] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY],
UNIQUE NONCLUSTERED 
(
	[username] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[customer]    Script Date: 06/04/2025 2:05:40 CH ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[customer](
	[id] [int] IDENTITY(1,1) NOT NULL,
	[user_id] [int] NULL,
	[full_name] [varchar](100) NOT NULL,
	[email] [varchar](100) NULL,
	[phone] [varchar](20) NOT NULL,
	[address] [varchar](max) NULL,
	[id_card] [varchar](50) NULL,
	[created_at] [datetime] NULL,
	[updated_at] [datetime] NULL,
PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
GO
/****** Object:  View [dbo].[vw_today_checkins]    Script Date: 06/04/2025 2:05:40 CH ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO

CREATE VIEW [dbo].[vw_today_checkins] AS
SELECT b.id, b.check_in_date, b.status, b.total_amount,
       c.id AS customer_id, c.full_name, c.email, c.phone,
       u.id AS receptionist_id, u.full_name AS receptionist_name
FROM booking b
JOIN customer c ON b.customer_id = c.id
LEFT JOIN users u ON b.user_id = u.id
WHERE CAST(b.check_in_date AS DATE) = CAST(GETDATE() AS DATE)
  AND b.status = 'CONFIRMED';
GO
/****** Object:  Table [dbo].[booking_detail]    Script Date: 06/04/2025 2:05:40 CH ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[booking_detail](
	[id] [int] IDENTITY(1,1) NOT NULL,
	[booking_id] [int] NOT NULL,
	[room_id] [int] NOT NULL,
	[price] [decimal](10, 2) NOT NULL,
	[status] [varchar](20) NOT NULL,
	[created_at] [datetime] NULL,
	[updated_at] [datetime] NULL,
	[check_in_date] [datetime2](6) NULL,
PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  View [dbo].[vw_customer_bookings]    Script Date: 06/04/2025 2:05:40 CH ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO

CREATE VIEW [dbo].[vw_customer_bookings] AS
SELECT b.id, b.check_in_date, b.check_out_date, b.status, b.total_amount, b.payment_status,
       c.id AS customer_id, c.full_name, c.email, c.phone,
       (SELECT COUNT(*) FROM booking_detail WHERE booking_id = b.id) AS room_count
FROM booking b
JOIN customer c ON b.customer_id = c.id;
GO
/****** Object:  View [dbo].[vw_room_occupancy]    Script Date: 06/04/2025 2:05:40 CH ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO

CREATE VIEW [dbo].[vw_room_occupancy] AS
SELECT r.id, r.room_number, r.status, r.floor,
       rt.name AS room_type_name, rt.capacity, rt.price,
       b.id AS booking_id, b.check_in_date, b.check_out_date, b.status AS booking_status,
       c.full_name AS customer_name, c.phone AS customer_phone
FROM room r
JOIN room_type rt ON r.room_type_id = rt.id
LEFT JOIN booking_detail bd ON r.id = bd.room_id
LEFT JOIN booking b ON bd.booking_id = b.id AND b.status IN ('CHECKED_IN', 'CONFIRMED')
LEFT JOIN customer c ON b.customer_id = c.id;
GO
/****** Object:  View [dbo].[vw_revenue_by_room_type]    Script Date: 06/04/2025 2:05:40 CH ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO

CREATE VIEW [dbo].[vw_revenue_by_room_type] AS
SELECT rt.id, rt.name AS room_type_name,
       COUNT(DISTINCT b.id) AS booking_count,
       SUM(b.total_amount) AS total_revenue,
       AVG(b.total_amount) AS average_revenue
FROM room_type rt
JOIN room r ON rt.id = r.room_type_id
JOIN booking_detail bd ON r.id = bd.room_id
JOIN booking b ON bd.booking_id = b.id
WHERE b.status = 'CHECKED_OUT'
GROUP BY rt.id, rt.name;
GO
/****** Object:  Table [dbo].[Invoices]    Script Date: 06/04/2025 2:05:40 CH ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[Invoices](
	[id] [int] IDENTITY(1,1) NOT NULL,
	[issued_date] [datetime2](6) NOT NULL,
	[status] [varchar](50) NOT NULL,
	[total_amount] [numeric](10, 2) NOT NULL,
	[booking_id] [int] NOT NULL,
	[user_id] [int] NULL,
PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[payment]    Script Date: 06/04/2025 2:05:40 CH ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[payment](
	[id] [int] IDENTITY(1,1) NOT NULL,
	[booking_id] [int] NOT NULL,
	[amount] [decimal](10, 2) NOT NULL,
	[payment_date] [datetime] NULL,
	[payment_method] [varchar](50) NULL,
	[status] [varchar](20) NOT NULL,
	[created_at] [datetime] NULL,
	[updated_at] [datetime] NULL,
PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Index [idx_booking_check_in_date]    Script Date: 06/04/2025 2:05:40 CH ******/
CREATE NONCLUSTERED INDEX [idx_booking_check_in_date] ON [dbo].[booking]
(
	[check_in_date] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [idx_booking_customer_id]    Script Date: 06/04/2025 2:05:40 CH ******/
CREATE NONCLUSTERED INDEX [idx_booking_customer_id] ON [dbo].[booking]
(
	[customer_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [idx_booking_status]    Script Date: 06/04/2025 2:05:40 CH ******/
CREATE NONCLUSTERED INDEX [idx_booking_status] ON [dbo].[booking]
(
	[status] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [idx_booking_user_id]    Script Date: 06/04/2025 2:05:40 CH ******/
CREATE NONCLUSTERED INDEX [idx_booking_user_id] ON [dbo].[booking]
(
	[user_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [idx_customer_phone]    Script Date: 06/04/2025 2:05:40 CH ******/
CREATE NONCLUSTERED INDEX [idx_customer_phone] ON [dbo].[customer]
(
	[phone] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [idx_payment_booking_id]    Script Date: 06/04/2025 2:05:40 CH ******/
CREATE NONCLUSTERED INDEX [idx_payment_booking_id] ON [dbo].[payment]
(
	[booking_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [idx_room_room_type_id]    Script Date: 06/04/2025 2:05:40 CH ******/
CREATE NONCLUSTERED INDEX [idx_room_room_type_id] ON [dbo].[room]
(
	[room_type_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [idx_room_status]    Script Date: 06/04/2025 2:05:40 CH ******/
CREATE NONCLUSTERED INDEX [idx_room_status] ON [dbo].[room]
(
	[status] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [idx_users_role_id]    Script Date: 06/04/2025 2:05:40 CH ******/
CREATE NONCLUSTERED INDEX [idx_users_role_id] ON [dbo].[users]
(
	[role_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [idx_users_username]    Script Date: 06/04/2025 2:05:40 CH ******/
CREATE NONCLUSTERED INDEX [idx_users_username] ON [dbo].[users]
(
	[username] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [UK_ncoa9bfasrql0x4nhmh1plxxy]    Script Date: 06/04/2025 2:05:40 CH ******/
CREATE UNIQUE NONCLUSTERED INDEX [UK_ncoa9bfasrql0x4nhmh1plxxy] ON [dbo].[users]
(
	[email] ASC
)
WHERE ([email] IS NOT NULL)
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
ALTER TABLE [dbo].[booking] ADD  DEFAULT ('CONFIRMED') FOR [status]
GO
ALTER TABLE [dbo].[booking] ADD  DEFAULT ('PENDING') FOR [payment_status]
GO
ALTER TABLE [dbo].[booking] ADD  DEFAULT (getdate()) FOR [created_at]
GO
ALTER TABLE [dbo].[booking] ADD  DEFAULT (getdate()) FOR [updated_at]
GO
ALTER TABLE [dbo].[booking_detail] ADD  DEFAULT ('BOOKED') FOR [status]
GO
ALTER TABLE [dbo].[booking_detail] ADD  DEFAULT (getdate()) FOR [created_at]
GO
ALTER TABLE [dbo].[booking_detail] ADD  DEFAULT (getdate()) FOR [updated_at]
GO
ALTER TABLE [dbo].[customer] ADD  DEFAULT (getdate()) FOR [created_at]
GO
ALTER TABLE [dbo].[customer] ADD  DEFAULT (getdate()) FOR [updated_at]
GO
ALTER TABLE [dbo].[payment] ADD  DEFAULT (getdate()) FOR [payment_date]
GO
ALTER TABLE [dbo].[payment] ADD  DEFAULT ('COMPLETED') FOR [status]
GO
ALTER TABLE [dbo].[payment] ADD  DEFAULT (getdate()) FOR [created_at]
GO
ALTER TABLE [dbo].[payment] ADD  DEFAULT (getdate()) FOR [updated_at]
GO
ALTER TABLE [dbo].[room] ADD  DEFAULT ('AVAILABLE') FOR [status]
GO
ALTER TABLE [dbo].[room] ADD  DEFAULT (getdate()) FOR [created_at]
GO
ALTER TABLE [dbo].[room] ADD  DEFAULT (getdate()) FOR [updated_at]
GO
ALTER TABLE [dbo].[room_type] ADD  DEFAULT (getdate()) FOR [created_at]
GO
ALTER TABLE [dbo].[room_type] ADD  DEFAULT (getdate()) FOR [updated_at]
GO
ALTER TABLE [dbo].[users] ADD  DEFAULT (getdate()) FOR [created_at]
GO
ALTER TABLE [dbo].[users] ADD  DEFAULT (getdate()) FOR [updated_at]
GO
ALTER TABLE [dbo].[booking]  WITH CHECK ADD FOREIGN KEY([customer_id])
REFERENCES [dbo].[customer] ([id])
GO
ALTER TABLE [dbo].[booking]  WITH CHECK ADD FOREIGN KEY([user_id])
REFERENCES [dbo].[users] ([id])
GO
ALTER TABLE [dbo].[booking_detail]  WITH CHECK ADD FOREIGN KEY([booking_id])
REFERENCES [dbo].[booking] ([id])
GO
ALTER TABLE [dbo].[booking_detail]  WITH CHECK ADD FOREIGN KEY([room_id])
REFERENCES [dbo].[room] ([id])
GO
ALTER TABLE [dbo].[customer]  WITH CHECK ADD FOREIGN KEY([user_id])
REFERENCES [dbo].[users] ([id])
ON DELETE SET NULL
GO
ALTER TABLE [dbo].[Invoices]  WITH CHECK ADD  CONSTRAINT [FK1176kiirslq9sjbva69h86dao] FOREIGN KEY([booking_id])
REFERENCES [dbo].[booking] ([id])
GO
ALTER TABLE [dbo].[Invoices] CHECK CONSTRAINT [FK1176kiirslq9sjbva69h86dao]
GO
ALTER TABLE [dbo].[Invoices]  WITH CHECK ADD  CONSTRAINT [FK7f40lrpb4q6f4kdc0xv5goe27] FOREIGN KEY([user_id])
REFERENCES [dbo].[users] ([id])
GO
ALTER TABLE [dbo].[Invoices] CHECK CONSTRAINT [FK7f40lrpb4q6f4kdc0xv5goe27]
GO
ALTER TABLE [dbo].[payment]  WITH CHECK ADD FOREIGN KEY([booking_id])
REFERENCES [dbo].[booking] ([id])
GO
ALTER TABLE [dbo].[room]  WITH CHECK ADD FOREIGN KEY([room_type_id])
REFERENCES [dbo].[room_type] ([id])
GO
/****** Object:  StoredProcedure [dbo].[sp_check_in_customer]    Script Date: 06/04/2025 2:05:40 CH ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO

CREATE PROCEDURE [dbo].[sp_check_in_customer]
    @booking_id INT,
    @user_id INT
AS
BEGIN
    BEGIN TRANSACTION;
    
    UPDATE booking
    SET status = 'CHECKED_IN',
        updated_at = GETDATE()
    WHERE id = @booking_id;
    
    UPDATE booking_detail
    SET status = 'CHECKED_IN',
        updated_at = GETDATE()
    WHERE booking_id = @booking_id;
    
    UPDATE r
    SET r.status = 'OCCUPIED',
        r.updated_at = GETDATE()
    FROM room r
    WHERE r.id IN (
        SELECT room_id FROM booking_detail WHERE booking_id = @booking_id
    );
    
    COMMIT;
END;
GO
/****** Object:  StoredProcedure [dbo].[sp_check_out_customer]    Script Date: 06/04/2025 2:05:40 CH ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO

CREATE PROCEDURE [dbo].[sp_check_out_customer]
    @booking_id INT,
    @user_id INT
AS
BEGIN
    BEGIN TRANSACTION;
    
    UPDATE booking
    SET status = 'CHECKED_OUT',
        check_out_date = GETDATE(),
        updated_at = GETDATE()
    WHERE id = @booking_id;
    
    UPDATE booking_detail
    SET status = 'CHECKED_OUT',
        updated_at = GETDATE()
    WHERE booking_id = @booking_id;
    
    UPDATE r
    SET r.status = 'AVAILABLE',
        r.updated_at = GETDATE()
    FROM room r
    WHERE r.id IN (
        SELECT room_id FROM booking_detail WHERE booking_id = @booking_id
    );
    
    COMMIT;
END;
GO
/****** Object:  StoredProcedure [dbo].[sp_get_available_rooms_by_type]    Script Date: 06/04/2025 2:05:40 CH ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO

CREATE PROCEDURE [dbo].[sp_get_available_rooms_by_type]
    @room_type_id INT
AS
BEGIN
    SELECT r.*, rt.name AS room_type_name, rt.description, rt.capacity, rt.price, rt.amenities
    FROM room r
    JOIN room_type rt ON r.room_type_id = rt.id
    WHERE r.room_type_id = @room_type_id AND r.status = 'AVAILABLE';
END;
GO
/****** Object:  StoredProcedure [dbo].[sp_get_booking_details]    Script Date: 06/04/2025 2:05:40 CH ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO

CREATE PROCEDURE [dbo].[sp_get_booking_details]
    @booking_id INT
AS
BEGIN
    SELECT b.*, c.full_name, c.email, c.phone, c.address,
           bd.id AS detail_id, bd.price AS room_price,
           r.room_number, rt.name AS room_type_name, rt.description AS room_type_description
    FROM booking b
    JOIN customer c ON b.customer_id = c.id
    JOIN booking_detail bd ON b.id = bd.booking_id
    JOIN room r ON bd.room_id = r.id
    JOIN room_type rt ON r.room_type_id = rt.id
    WHERE b.id = @booking_id;
END;
GO
/****** Object:  StoredProcedure [dbo].[sp_get_bookings_by_phone]    Script Date: 06/04/2025 2:05:40 CH ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO

-- =====================================================
-- Create Stored Procedures
-- =====================================================

CREATE PROCEDURE [dbo].[sp_get_bookings_by_phone]
    @phone VARCHAR(20)
AS
BEGIN
    SELECT b.*, c.full_name, c.email, c.phone
    FROM booking b
    JOIN customer c ON b.customer_id = c.id
    WHERE c.phone = @phone
    ORDER BY b.created_at DESC;
END;
GO
/****** Object:  StoredProcedure [dbo].[sp_get_customer_booking_history]    Script Date: 06/04/2025 2:05:40 CH ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO

CREATE PROCEDURE [dbo].[sp_get_customer_booking_history]
    @customer_id INT
AS
BEGIN
    SELECT b.*,
           (SELECT COUNT(*) FROM booking_detail WHERE booking_id = b.id) AS room_count
    FROM booking b
    WHERE b.customer_id = @customer_id
    ORDER BY b.created_at DESC;
END;
GO
/****** Object:  StoredProcedure [dbo].[sp_get_customer_statistics]    Script Date: 06/04/2025 2:05:40 CH ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO

CREATE PROCEDURE [dbo].[sp_get_customer_statistics]
AS
BEGIN
    SELECT 
        COUNT(DISTINCT c.id) AS total_customers,
        COUNT(DISTINCT b.id) AS total_bookings,
        SUM(b.total_amount) AS total_revenue,
        AVG(b.total_amount) AS average_booking_value,
        COUNT(DISTINCT CASE WHEN b.created_at >= DATEADD(MONTH, -1, GETDATE()) THEN c.id END) AS new_customers_last_month
    FROM customer c
    LEFT JOIN booking b ON c.id = b.customer_id;
END;
GO
/****** Object:  StoredProcedure [dbo].[sp_get_daily_revenue_report]    Script Date: 06/04/2025 2:05:40 CH ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO

CREATE PROCEDURE [dbo].[sp_get_daily_revenue_report]
    @start_date DATE,
    @end_date DATE
AS
BEGIN
    -- This report aggregates payment transactions
    SELECT CAST(payment_date AS DATE) AS PaymentDate,
           COUNT(*) AS PaymentCount,
           SUM(amount) AS TotalRevenue,
           AVG(amount) AS AveragePayment
    FROM payment
    WHERE CAST(payment_date AS DATE) BETWEEN @start_date AND @end_date
    GROUP BY CAST(payment_date AS DATE)
    ORDER BY PaymentDate;
END;
GO
/****** Object:  StoredProcedure [dbo].[sp_get_occupancy_rate]    Script Date: 06/04/2025 2:05:40 CH ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO

CREATE PROCEDURE [dbo].[sp_get_occupancy_rate]
    @start_date DATE,
    @end_date DATE
AS
BEGIN
    DECLARE @total_rooms INT;
    DECLARE @total_days INT;
    DECLARE @occupied_rooms INT;
    
    SELECT @total_rooms = COUNT(*) FROM room;
    SELECT @total_days = DATEDIFF(DAY, @start_date, @end_date) + 1;
    
    SELECT @occupied_rooms = COUNT(DISTINCT r.id)
    FROM room r
    JOIN booking_detail bd ON r.id = bd.room_id
    JOIN booking b ON bd.booking_id = b.id
    WHERE CAST(b.check_in_date AS DATE) <= @end_date
      AND (b.check_out_date IS NULL OR CAST(b.check_out_date AS DATE) >= @start_date)
      AND b.status IN ('CHECKED_IN', 'CONFIRMED');
    
    SELECT 
        @total_rooms AS total_rooms,
        @total_days AS total_days,
        @occupied_rooms AS occupied_rooms,
        CAST(@occupied_rooms AS FLOAT) / (@total_rooms * @total_days) * 100 AS occupancy_rate;
END;
GO
/****** Object:  StoredProcedure [dbo].[sp_get_payment_report]    Script Date: 06/04/2025 2:05:40 CH ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO

CREATE PROCEDURE [dbo].[sp_get_payment_report]
    @start_date DATE,
    @end_date DATE
AS
BEGIN
    SELECT CAST(payment_date AS DATE) AS PaymentDate,
           COUNT(*) AS PaymentCount,
           SUM(amount) AS TotalRevenue,
           AVG(amount) AS AveragePayment
    FROM payment
    WHERE CAST(payment_date AS DATE) BETWEEN @start_date AND @end_date
    GROUP BY CAST(payment_date AS DATE)
    ORDER BY PaymentDate;
END;
GO
/****** Object:  StoredProcedure [dbo].[sp_get_room_occupancy_report]    Script Date: 06/04/2025 2:05:40 CH ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO

CREATE PROCEDURE [dbo].[sp_get_room_occupancy_report]
    @start_date DATE,
    @end_date DATE
AS
BEGIN
    SELECT r.room_number, rt.name AS room_type_name,
           COUNT(DISTINCT b.id) AS booking_count,
           SUM(CASE WHEN b.status = 'CHECKED_IN' THEN 1 ELSE 0 END) AS check_in_count,
           SUM(CASE WHEN b.status = 'CHECKED_OUT' THEN 1 ELSE 0 END) AS check_out_count,
           SUM(b.total_amount) AS total_revenue
    FROM room r
    JOIN room_type rt ON r.room_type_id = rt.id
    LEFT JOIN booking_detail bd ON r.id = bd.room_id
    LEFT JOIN booking b ON bd.booking_id = b.id
    WHERE (b.check_in_date BETWEEN @start_date AND @end_date OR b.check_in_date IS NULL)
    GROUP BY r.room_number, rt.name
    ORDER BY r.room_number;
END;
GO
/****** Object:  StoredProcedure [dbo].[sp_get_room_type_popularity]    Script Date: 06/04/2025 2:05:40 CH ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO

CREATE PROCEDURE [dbo].[sp_get_room_type_popularity]
    @start_date DATE,
    @end_date DATE
AS
BEGIN
    SELECT rt.name AS room_type_name,
           COUNT(DISTINCT b.id) AS booking_count,
           SUM(b.total_amount) AS total_revenue,
           COUNT(DISTINCT b.customer_id) AS unique_customers
    FROM room_type rt
    JOIN room r ON rt.id = r.room_type_id
    JOIN booking_detail bd ON r.id = bd.room_id
    JOIN booking b ON bd.booking_id = b.id
    WHERE CAST(b.check_in_date AS DATE) BETWEEN @start_date AND @end_date
    GROUP BY rt.name
    ORDER BY booking_count DESC;
END;
GO
/****** Object:  StoredProcedure [dbo].[sp_get_today_checkins]    Script Date: 06/04/2025 2:05:40 CH ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO

CREATE PROCEDURE [dbo].[sp_get_today_checkins]
AS
BEGIN
    SELECT b.*, c.full_name, c.email, c.phone
    FROM booking b
    JOIN customer c ON b.customer_id = c.id
    WHERE CAST(b.check_in_date AS DATE) = CAST(GETDATE() AS DATE)
      AND b.status = 'CONFIRMED'
    ORDER BY b.check_in_date;
END;
GO
USE [master]
GO
ALTER DATABASE [Hotel_Management] SET  READ_WRITE 
GO
