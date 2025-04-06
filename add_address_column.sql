USE [Hotel_Management]
GO

-- Check if address column exists before adding
IF NOT EXISTS (
    SELECT * 
    FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_NAME = 'users' 
    AND COLUMN_NAME = 'address'
)
BEGIN
    -- Add address column to users table
    ALTER TABLE [dbo].[users]
    ADD [address] NVARCHAR(255) NULL;
    
    PRINT 'Address column added to users table.'
END
ELSE
BEGIN
    PRINT 'Address column already exists in users table.'
END
GO 