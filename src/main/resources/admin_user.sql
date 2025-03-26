USE [HotelManagement]
GO

-- Check if admin user exists
IF NOT EXISTS (SELECT * FROM Users WHERE username = 'admin')
BEGIN
    -- Insert admin user with role_id = 0 (Admin role)
    INSERT INTO Users (
        username,
        password,
        full_name,
        phone,
        email,
        role_id
    )
    VALUES (
        'admin',                    -- username
        'admin123',                -- password (you should use a hashed password in production)
        'Your Full Name',          -- full_name (replace with your actual name)
        '0123456789',             -- phone
        'admin@hotel.com',         -- email
        0                          -- role_id = 0 for Admin (1=Employee, 2=Customer)
    );
    PRINT 'Admin user created successfully';
END
ELSE
BEGIN
    PRINT 'Admin user already exists';
END
GO