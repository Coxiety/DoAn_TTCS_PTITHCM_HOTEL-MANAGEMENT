# Hotel Management System

A comprehensive hotel management system built with Spring Boot, designed to streamline hotel operations including room bookings, customer management, and administrative tasks.

## Features

- **Multi-role Access**: Different functionalities for administrators, receptionists, and customers
- **Room Management**: Create, update, and manage different room types and individual rooms
- **Booking System**: Comprehensive booking workflow for both customers and receptionists
- **Customer Management**: Register and manage customer information
- **Admin Dashboard**: Monitor hotel statistics, manage staff, and generate revenue reports
- **Responsive UI**: Clean, modern interface built with Thymeleaf and Bootstrap

## Prerequisites

- Java 17 or higher
- Maven 3.6.3 or higher
- Microsoft SQL Server 2019 or higher
- Git (optional, for cloning the repository)

## Getting Started

### 1. Clone the Repository

```bash
git clone https://github.com/yourusername/hotel-management.git
cd hotel-management
```

### 2. Database Setup

1. Create a new database in SQL Server named `HotelManagement`
2. Make sure you have a user with username `admin` and password `admin` that has access to this database
   - Alternatively, modify the connection settings in `application.properties`

```sql
-- Run these commands in SQL Server Management Studio
CREATE DATABASE HotelManagement;
GO

USE HotelManagement;
GO

-- If you need to create a new login
CREATE LOGIN admin WITH PASSWORD = 'admin';
GO

-- Create a user for the database
CREATE USER admin FOR LOGIN admin;
GO

-- Grant necessary permissions
EXEC sp_addrolemember 'db_owner', 'admin';
GO
```

3. Run the database initialization script (if you're setting up for the first time)
   - The application uses Hibernate's `ddl-auto=update`, which will create tables automatically
   - For a complete setup with sample data, you can use the SQL scripts located in the project root

```bash
# Using sqlcmd (comes with SQL Server)
sqlcmd -S localhost -U admin -P admin -d HotelManagement -i db_update_room_images.sql
```

### 3. Configure the Application

The application is pre-configured in `src/main/resources/application.properties`. If you need to change any settings:

- **Database Connection**:
```properties
spring.datasource.url=jdbc:sqlserver://localhost;databaseName=HotelManagement;trustServerCertificate=true;encrypt=false;integratedSecurity=false
spring.datasource.username=admin
spring.datasource.password=admin
```

- **Server Port** (default is 8080):
```properties
server.port=8080
```

### 4. Build and Run the Application

```bash
# Build the application
mvn clean package

# Run the application
mvn spring-boot:run
```

Alternatively, you can run it directly with Java:

```bash
java -jar target/HotelProperties-0.0.1-SNAPSHOT.jar
```

### 5. Access the Application

Once the application is running, access it in your web browser:

- **URL**: `http://localhost:8080`
- **Default Admin Login**: Create an admin user through SQL or see below for default credentials

## Default User Accounts

For testing purposes, you can use these default user accounts:

| Username | Password | Role |
|----------|----------|------|
| admin    | admin    | Administrator |
| receptionist | receptionist | Receptionist |
| customer | customer | Customer |

## Project Structure

```
hotel-management/
├── src/main/
│   ├── java/com/HotelManagement/
│   │   ├── controller/         # Web controllers
│   │   ├── dto/                # Data Transfer Objects
│   │   ├── models/             # Entity classes
│   │   ├── repository/         # Data access layer
│   │   ├── service/            # Business logic layer
│   │   └── HotelPropertiesApplication.java  # Main class
│   └── resources/
│       ├── static/             # Static resources (CSS, JS, images)
│       ├── templates/          # Thymeleaf templates
│       └── application.properties  # Application configuration
├── db_update_room_images.sql   # Database update script
└── README.md                   # Project documentation
```

## User Roles and Functionality

### Admin
- Dashboard with statistics
- User and staff management
- Room and room type management
- Revenue reporting

### Receptionist
- Customer check-in and check-out
- Room booking management
- Customer management

### Customer / Guest
- View room availability
- Make room reservations
- View booking history

## Development

### Extending the Application

To add new features:
1. Create entity classes in the `models` package
2. Create repositories in the `repository` package
3. Add business logic in the `service` package
4. Create controllers in the `controller` package
5. Add UI templates in the `resources/templates` directory

### Running Tests

```bash
mvn test
```

## Troubleshooting

### Common Issues

1. **Database Connection Errors**:
   - Verify SQL Server is running
   - Check connection credentials in `application.properties`
   - Ensure the database exists and the user has appropriate permissions

2. **Application Won't Start**:
   - Check if port 8080 is already in use
   - Review logs for error messages
   - Verify Java 17 is installed correctly

3. **Login Issues**:
   - Ensure the database has user records
   - Check password encoding (system uses Base64 encoding for demo purposes)

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Acknowledgments

- Spring Boot framework and its community
- Bootstrap for responsive UI components
- All contributors and testers