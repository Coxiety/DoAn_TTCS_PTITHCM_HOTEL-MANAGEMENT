# Hotel Management System

A comprehensive hotel management application that allows administrators, receptionists, and customers to manage bookings, rooms, and other hotel operations.

## System Requirements

- Java 17 or higher
- Maven
- SQL Server
- Web browser (Chrome, Firefox, or Edge recommended)

## Setup Instructions

### 1. Database Setup

1. Open SQL Server Management Studio (SSMS) or your preferred SQL Server client
2. Connect to your SQL Server instance
3. Execute the database creation script:
   ```
   src\main\resources\db\CreateDatabase_With_Data_Generation_06042025.sql
   ```
   This will create the database schema and populate it with initial data including:
   - Room types (Standard, Deluxe, Suite)
   - Sample rooms
   - User accounts (Admin, Receptionist, Customer)
   - Test bookings

### 2. Configure Database Connection

1. Open `src\main\resources\application.properties`
2. Update the following properties with your SQL Server information:
   ```
   spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=hotel_management;encrypt=true;trustServerCertificate=true
   spring.datasource.username=your_username
   spring.datasource.password=your_password
   ```

### 3. Build and Run the Application

#### Using Maven:

1. Open a command prompt or terminal
2. Navigate to the project root directory
3. Run the following command:
   ```
   mvn spring-boot:run
   ```
4. The application will start and be accessible at: http://localhost:8080

#### Using an IDE:

1. Import the project into your IDE (IntelliJ IDEA, Eclipse, VS Code)
2. Run the `HotelManagementApplication.java` file as a Java application

## Default User Accounts

The application comes with pre-configured user accounts:

| Username | Password | Role |
|----------|----------|------|
| admin    | admin123 | Administrator |
| reception| pass123  | Receptionist |
| customer | pass123  | Customer |

## Key Features

- **Administrator:**
  - Manage rooms and room types
  - Create and manage user accounts
  - View all bookings and reports

- **Receptionist:**
  - Check guests in/out
  - Search for bookings by phone number
  - Make new bookings on behalf of customers
  - Cancel bookings
  - View dashboard of today's check-ins

- **Customer:**
  - Make new bookings
  - View booking history
  - Cancel bookings

## Important Notes

- Customers can check in at any time on their scheduled check-in date
- Rooms must be available to be booked
- Images for room types are stored in the `/uploads/roomtypes/` directory

## Troubleshooting

- If you encounter port conflicts, you can change the server port in `application.properties`:
  ```
  server.port=8081
  ```
- For any database connection issues, ensure SQL Server is running and the connection details are correct
- Check application logs for detailed error information