-- This script updates the database schema to remove the image_path column from the Rooms table
-- since rooms will now inherit their images from their associated room type

USE [HotelManagement]
GO

-- First, check if the column exists
IF EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('dbo.Rooms') AND name = 'image_path')
BEGIN
    -- Remove the image_path column from the Rooms table
    ALTER TABLE [dbo].[Rooms]
    DROP COLUMN [image_path];
    
    PRINT 'Successfully removed image_path column from Rooms table.';
END
ELSE
BEGIN
    PRINT 'image_path column does not exist in Rooms table.';
END
GO

-- The Room entity will now use a transient property to get the image_path from its RoomType
PRINT 'Room entity now uses RoomType image_path through relationship mapping.';
GO