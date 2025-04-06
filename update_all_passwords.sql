USE [Hotel_Management]
GO

-- Update all user passwords to the SHA-256 hash of "test"
-- SHA-256 of "test" Base64-encoded is "n4bQgYhMfWWaL+qgxVrQFaO/TxsrC4Is0V1sFbDwCgg="
UPDATE [dbo].[Users]
SET [password] = 'n4bQgYhMfWWaL+qgxVrQFaO/TxsrC4Is0V1sFbDwCgg='

-- Print confirmation
PRINT 'All passwords have been updated to "test" using SHA-256 hashing.'
GO 