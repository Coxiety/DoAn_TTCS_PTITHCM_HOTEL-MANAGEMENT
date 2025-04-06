USE [Hotel_Management]
GO

-- Update all user passwords to 'test'
UPDATE [dbo].[users]
SET [password] = 'test'
GO

-- Print confirmation
SELECT 'All passwords have been changed to "test"' AS [Result]
GO 