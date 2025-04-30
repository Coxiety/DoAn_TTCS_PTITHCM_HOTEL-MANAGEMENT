# Data Consistency Between User, Customer, and Booking Tables

This document explains the data consistency mechanisms implemented to ensure that user data remains synchronized between the `users`, `customer`, and `booking` tables.

## Problem Statement

The system previously had inconsistencies:
1. Customer full_name and User full_name were not synchronized, creating discrepancies in the data
2. Booking records only referenced customer_id and were missing user_id in some cases
3. No systematic way to keep data consistent when updating either User or Customer information

## Solution

The following changes have been implemented:

### 1. Database Triggers

Three SQL Server triggers have been created:
- `trg_User_UpdateCustomer`: Updates Customer data when User data changes
- `trg_Customer_UpdateUser`: Updates User data when Customer data changes
- `trg_Booking_SetUserIdFromCustomer`: Automatically sets the user_id in booking records from the customer's linked user

### 2. Java Service Layer Updates

The service layer has been updated to maintain consistency:
- `CustomerService.saveCustomer()`: Updates the linked User's data when saving a Customer
- `CustomerService.updateCustomer()`: Updates the linked User's data when updating a Customer
- `CustomerService.linkCustomerToUser()`: Synchronizes Customer data with User data when linking
- `UserService.saveUser()`: Updates or creates a linked Customer when saving a User as a customer (roleId=0)

### 3. Booking Service Updates

The BookingService has been updated to:
- Always set both customer_id and user_id when creating a booking
- If a booking is created without an explicit user (e.g., by a receptionist), use the customer's linked user_id

### 4. Data Migration Scripts

Two SQL scripts are provided:
- `update_relationship.sql`: Updates existing data to fix inconsistencies
- `create_consistency_triggers.sql`: Creates database triggers for ongoing consistency

## How to Apply These Changes

1. Run the `update_relationship.sql` script to fix existing data
2. Run the `create_consistency_triggers.sql` script to create database triggers
3. Deploy the updated service classes

## Verification

After applying these changes, you can verify the consistency:
1. Update a User's full_name and check that linked Customer records are updated
2. Update a Customer's full_name and check that linked User records are updated
3. Create a new booking through the receptionist interface and verify both customer_id and user_id are set correctly 