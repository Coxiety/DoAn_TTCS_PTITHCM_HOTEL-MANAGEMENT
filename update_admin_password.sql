USE [Hotel_Management]
GO

SET QUOTED_IDENTIFIER ON
GO

-- Update admin password to use the SHA-256 hash of "test"
-- SHA-256 of "test" Base64-encoded is "n4bQgYhMfWWaL+qgxVrQFaO/TxsrC4Is0V1sFbDwCgg="
UPDATE [dbo].[Users]
SET [password] = 'n4bQgYhMfWWaL+qgxVrQFaO/TxsrC4Is0V1sFbDwCgg='
WHERE [username] = 'admin';

PRINT 'Admin password updated successfully to "test".' 