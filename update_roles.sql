USE [Hotel_Management]
GO

SET QUOTED_IDENTIFIER ON
GO

BEGIN TRANSACTION;

-- First, update to temporary values to avoid conflicts
UPDATE [dbo].[Users]
SET [role_id] = 101
WHERE [role_id] = 1;  -- receptionist -> temp

UPDATE [dbo].[Users]
SET [role_id] = 102
WHERE [role_id] = 2;  -- admin -> temp

UPDATE [dbo].[Users]
SET [role_id] = 100
WHERE [role_id] = 3;  -- customer -> temp

-- Then update to the new values
UPDATE [dbo].[Users]
SET [role_id] = 1
WHERE [role_id] = 102;  -- temp admin -> 1

UPDATE [dbo].[Users]
SET [role_id] = 2
WHERE [role_id] = 101;  -- temp receptionist -> 2

UPDATE [dbo].[Users]
SET [role_id] = 0
WHERE [role_id] = 100;  -- temp customer -> 0

COMMIT;

PRINT 'User roles updated successfully:';
PRINT '- Admin role_id: 1';
PRINT '- Receptionist role_id: 2';
PRINT '- Customer role_id: 0'; 