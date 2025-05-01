USE [Hotel_Management]
GO

-- Step 1: Update customer fullnames to match user fullnames where user_id is not null
UPDATE c
SET c.full_name = u.full_name,
    c.email = u.email,
    c.phone = u.phone,
    c.updated_at = GETDATE()
FROM [dbo].[customer] c
INNER JOIN [dbo].[users] u ON c.user_id = u.id
WHERE c.user_id IS NOT NULL
  AND (c.full_name != u.full_name OR c.email != u.email OR c.phone != u.phone);

-- Step 2: Update booking records to set user_id from customer's user_id where missing
UPDATE b
SET b.user_id = c.user_id,
    b.updated_at = GETDATE()
FROM [dbo].[booking] b
INNER JOIN [dbo].[customer] c ON b.customer_id = c.id
WHERE b.user_id IS NULL AND c.user_id IS NOT NULL;

-- Print summary of updates
PRINT 'Data synchronization completed.'
GO 