USE [Hotel_Management]
GO

-- Check if test users already exist
IF NOT EXISTS (SELECT * FROM [dbo].[users] WHERE [username] = 'testadmin')
BEGIN
    -- Create test admin user
    INSERT INTO [dbo].[users] ([username], [password], [email], [full_name], [phone], [role_id], [created_at], [updated_at])
    VALUES ('testadmin', 'test', 'testadmin@example.com', 'Test Admin', '0123456789', 1, GETDATE(), GETDATE());
    
    PRINT 'Test admin user created.'
END
ELSE
BEGIN
    PRINT 'Test admin user already exists.'
END

-- Check if test receptionist already exists
IF NOT EXISTS (SELECT * FROM [dbo].[users] WHERE [username] = 'testreceptionist')
BEGIN
    -- Create test receptionist user
    INSERT INTO [dbo].[users] ([username], [password], [email], [full_name], [phone], [role_id], [created_at], [updated_at])
    VALUES ('testreceptionist', 'test', 'testreceptionist@example.com', 'Test Receptionist', '0123456788', 2, GETDATE(), GETDATE());
    
    PRINT 'Test receptionist user created.'
END
ELSE
BEGIN
    PRINT 'Test receptionist user already exists.'
END

-- Check if test customer already exists
IF NOT EXISTS (SELECT * FROM [dbo].[users] WHERE [username] = 'testcustomer')
BEGIN
    -- Create test customer user
    INSERT INTO [dbo].[users] ([username], [password], [email], [full_name], [phone], [role_id], [created_at], [updated_at])
    VALUES ('testcustomer', 'test', 'testcustomer@example.com', 'Test Customer', '0123456787', 3, GETDATE(), GETDATE());
    
    -- Create matching customer record
    DECLARE @userId INT = SCOPE_IDENTITY();
    
    INSERT INTO [dbo].[customer] ([full_name], [email], [phone], [user_id], [created_at], [updated_at])
    VALUES ('Test Customer', 'testcustomer@example.com', '0123456787', @userId, GETDATE(), GETDATE());
    
    PRINT 'Test customer user created.'
END
ELSE
BEGIN
    PRINT 'Test customer user already exists.'
END

SELECT 'Test users created with password: test' AS [Result]
GO 