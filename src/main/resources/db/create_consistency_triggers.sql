USE [Hotel_Management]
GO

-- Drop existing triggers if they exist
IF EXISTS (SELECT * FROM sys.triggers WHERE name = 'trg_User_UpdateCustomer')
DROP TRIGGER [dbo].[trg_User_UpdateCustomer];
GO

IF EXISTS (SELECT * FROM sys.triggers WHERE name = 'trg_Customer_UpdateUser')
DROP TRIGGER [dbo].[trg_Customer_UpdateUser];
GO

-- Create trigger to update Customer data when User is updated
CREATE TRIGGER [dbo].[trg_User_UpdateCustomer]
ON [dbo].[users]
AFTER UPDATE
AS
BEGIN
    SET NOCOUNT ON;
    
    -- Only proceed if relevant columns are changed
    IF UPDATE(full_name) OR UPDATE(email) OR UPDATE(phone)
    BEGIN
        -- Update customer records with matching user_id
        UPDATE c
        SET c.full_name = i.full_name,
            c.email = i.email,
            c.phone = i.phone,
            c.updated_at = GETDATE()
        FROM [dbo].[customer] c
        INNER JOIN inserted i ON c.user_id = i.id;
    END
END
GO

-- Create trigger to update User data when Customer is updated
CREATE TRIGGER [dbo].[trg_Customer_UpdateUser]
ON [dbo].[customer]
AFTER UPDATE
AS
BEGIN
    SET NOCOUNT ON;
    
    -- Only proceed if relevant columns are changed and user_id is not null
    IF (UPDATE(full_name) OR UPDATE(email) OR UPDATE(phone))
    BEGIN
        -- Update user records linked to the updated customers
        UPDATE u
        SET u.full_name = i.full_name,
            u.email = i.email,
            u.phone = i.phone,
            u.updated_at = GETDATE()
        FROM [dbo].[users] u
        INNER JOIN inserted i ON u.id = i.user_id
        WHERE i.user_id IS NOT NULL;
    END
END
GO

-- Drop existing trigger if it exists
IF EXISTS (SELECT * FROM sys.triggers WHERE name = 'trg_Booking_SetUserIdFromCustomer')
DROP TRIGGER [dbo].[trg_Booking_SetUserIdFromCustomer];
GO

-- Create trigger to set user_id from customer when creating a booking
CREATE TRIGGER [dbo].[trg_Booking_SetUserIdFromCustomer]
ON [dbo].[booking]
AFTER INSERT
AS
BEGIN
    SET NOCOUNT ON;
    
    -- Update booking user_id from customer's user_id if user_id is null
    UPDATE b
    SET b.user_id = c.user_id,
        b.updated_at = GETDATE()
    FROM [dbo].[booking] b
    INNER JOIN inserted i ON b.id = i.id
    INNER JOIN [dbo].[customer] c ON b.customer_id = c.id
    WHERE b.user_id IS NULL AND c.user_id IS NOT NULL;
END
GO

PRINT 'Database triggers for data consistency created successfully.'
GO 