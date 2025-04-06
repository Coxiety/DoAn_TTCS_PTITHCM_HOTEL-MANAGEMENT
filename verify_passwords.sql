USE [Hotel_Management]
GO

-- Verify that all passwords have been changed to the SHA-256 hash of "test"
SELECT 
    [id],
    [username],
    [password],
    CASE 
        WHEN [password] = 'n4bQgYhMfWWaL+qgxVrQFaO/TxsrC4Is0V1sFbDwCgg=' THEN 'YES'
        ELSE 'NO'
    END AS [password_reset_to_test]
FROM [dbo].[Users]
ORDER BY [id]

-- Print the count of passwords set to "test"
SELECT 
    COUNT(*) AS [total_users],
    SUM(CASE WHEN [password] = 'n4bQgYhMfWWaL+qgxVrQFaO/TxsrC4Is0V1sFbDwCgg=' THEN 1 ELSE 0 END) AS [users_with_test_password]
FROM [dbo].[Users]
GO 