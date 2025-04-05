-- SQL Script for updating room image management
-- This script modifies the database schema to remove image_path from Rooms table
-- and ensures all room images are stored only at the room type level

USE [HotelManagement]
GO

-- Step 1: Create a backup of the current Rooms table
IF OBJECT_ID('dbo.Rooms_Backup', 'U') IS NOT NULL
    DROP TABLE [dbo].[Rooms_Backup];

SELECT *
INTO [dbo].[Rooms_Backup]
FROM [dbo].[Rooms];

PRINT 'Created backup of Rooms table.';
GO

-- Step 2: Remove the image_path column from Rooms table
IF EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('dbo.Rooms') AND name = 'image_path')
BEGIN
    ALTER TABLE [dbo].[Rooms]
    DROP COLUMN [image_path];
    
    PRINT 'Successfully removed image_path column from Rooms table.';
END
ELSE
BEGIN
    PRINT 'image_path column does not exist in Rooms table.';
END
GO

-- Step 3: Update any INSERT statements in any database initialization scripts
-- For example, in populate_database.sql, replace:
--   INSERT INTO [dbo].[Rooms] ([type_id], [room_number], [status], [image_path])
-- With:
--   INSERT INTO [dbo].[Rooms] ([type_id], [room_number], [status])

-- Step 4: Ensure all RoomTypes have an image_path
UPDATE [dbo].[Room_Types]
SET [image_path] = '/uploads/roomtypes/default_room_type.jpg'
WHERE [image_path] IS NULL;
PRINT 'Ensured all room types have an image path.';
GO

-- Step 5: Add a note explaining the change
PRINT 'Database schema updated to remove image_path from Rooms table.';
PRINT 'Rooms now inherit their images from their associated RoomType via the @Transient getImagePath() method.';
GO