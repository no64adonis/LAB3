
UPDATE Users SET 
    PasswordHash = '$2a$12$4xZo.kcH.Zbar8PvVZKxfOws3VOEa5xpf153kJ1uAta2fFcIbUpUq',
    IsActive = 1,
    password_set = 1,
    Balance = 500000.00,
    FirstName = 'Test',
    LastName = 'User'
WHERE Email = 'user@gmail.com';

UPDATE Users SET 
    PasswordHash = '$2a$12$4xZo.kcH.Zbar8PvVZKxfOws3VOEa5xpf153kJ1uAta2fFcIbUpUq',
    IsActive = 1,
    password_set = 1,
    Balance = 1000000.00,
    FirstName = 'Admin',
    LastName = 'Fortuna'
WHERE Email = 'admin@gmail.com';

PRINT 'User passwords updated successfully.';

DECLARE @userId INT;
DECLARE @adminId INT;
SELECT @userId = UserID FROM Users WHERE Email = 'user@gmail.com';
SELECT @adminId = UserID FROM Users WHERE Email = 'admin@gmail.com';

PRINT 'User ID: ' + CAST(@userId AS VARCHAR);
PRINT 'Admin ID: ' + CAST(@adminId AS VARCHAR);

IF NOT EXISTS (SELECT 1 FROM Users WHERE Email = 'alice@example.com')
    INSERT INTO Users (PasswordHash, Email, FirstName, LastName, Phone, Role, CreationDate, LastLoginDate, Balance, IsActive, password_set) VALUES
    ('$2a$12$4xZo.kcH.Zbar8PvVZKxfOws3VOEa5xpf153kJ1uAta2fFcIbUpUq', 'alice@example.com', 'Alice', 'Nguyen', '0911111111', 'user', DATEADD(day, -90, GETDATE()), DATEADD(day, -1, GETDATE()), 150000.00, 1, 1);
IF NOT EXISTS (SELECT 1 FROM Users WHERE Email = 'bob@example.com')
    INSERT INTO Users (PasswordHash, Email, FirstName, LastName, Phone, Role, CreationDate, LastLoginDate, Balance, IsActive, password_set) VALUES
    ('$2a$12$4xZo.kcH.Zbar8PvVZKxfOws3VOEa5xpf153kJ1uAta2fFcIbUpUq', 'bob@example.com', 'Bob', 'Tran', '0922222222', 'user', DATEADD(day, -80, GETDATE()), DATEADD(day, -3, GETDATE()), 250000.00, 1, 1);
IF NOT EXISTS (SELECT 1 FROM Users WHERE Email = 'charlie@example.com')
    INSERT INTO Users (PasswordHash, Email, FirstName, LastName, Phone, Role, CreationDate, LastLoginDate, Balance, IsActive, password_set) VALUES
    ('$2a$12$4xZo.kcH.Zbar8PvVZKxfOws3VOEa5xpf153kJ1uAta2fFcIbUpUq', 'charlie@example.com', 'Charlie', 'Le', '0933333333', 'user', DATEADD(day, -70, GETDATE()), DATEADD(day, -7, GETDATE()), 50000.00, 1, 1);
IF NOT EXISTS (SELECT 1 FROM Users WHERE Email = 'diana@example.com')
    INSERT INTO Users (PasswordHash, Email, FirstName, LastName, Phone, Role, CreationDate, LastLoginDate, Balance, IsActive, password_set) VALUES
    ('$2a$12$4xZo.kcH.Zbar8PvVZKxfOws3VOEa5xpf153kJ1uAta2fFcIbUpUq', 'diana@example.com', 'Diana', 'Pham', '0944444444', 'user', DATEADD(day, -60, GETDATE()), DATEADD(day, -14, GETDATE()), 300000.00, 1, 1);
IF NOT EXISTS (SELECT 1 FROM Users WHERE Email = 'eve@example.com')
    INSERT INTO Users (PasswordHash, Email, FirstName, LastName, Phone, Role, CreationDate, LastLoginDate, Balance, IsActive, password_set) VALUES
    ('$2a$12$4xZo.kcH.Zbar8PvVZKxfOws3VOEa5xpf153kJ1uAta2fFcIbUpUq', 'eve@example.com', 'Eve', 'Vo', '0955555555', 'user', DATEADD(day, -50, GETDATE()), DATEADD(day, -30, GETDATE()), 0.00, 1, 1);
IF NOT EXISTS (SELECT 1 FROM Users WHERE Email = 'frank@example.com')
    INSERT INTO Users (PasswordHash, Email, FirstName, LastName, Phone, Role, CreationDate, LastLoginDate, Balance, IsActive, password_set) VALUES
    ('$2a$12$4xZo.kcH.Zbar8PvVZKxfOws3VOEa5xpf153kJ1uAta2fFcIbUpUq', 'frank@example.com', 'Frank', 'Hoang', '0966666666', 'admin', DATEADD(day, -40, GETDATE()), DATEADD(day, -2, GETDATE()), 750000.00, 1, 1);
IF NOT EXISTS (SELECT 1 FROM Users WHERE Email = 'grace@example.com')
    INSERT INTO Users (PasswordHash, Email, FirstName, LastName, Phone, Role, CreationDate, LastLoginDate, Balance, IsActive, password_set) VALUES
    ('$2a$12$4xZo.kcH.Zbar8PvVZKxfOws3VOEa5xpf153kJ1uAta2fFcIbUpUq', 'grace@example.com', 'Grace', 'Do', '0977777777', 'user', DATEADD(day, -35, GETDATE()), NULL, 100000.00, 0, 1);
IF NOT EXISTS (SELECT 1 FROM Users WHERE Email = 'henry@example.com')
    INSERT INTO Users (PasswordHash, Email, FirstName, LastName, Phone, Role, CreationDate, LastLoginDate, Balance, IsActive, password_set) VALUES
    ('$2a$12$4xZo.kcH.Zbar8PvVZKxfOws3VOEa5xpf153kJ1uAta2fFcIbUpUq', 'henry@example.com', 'Henry', 'Bui', '0988888888', 'user', DATEADD(day, -25, GETDATE()), DATEADD(day, -5, GETDATE()), 200000.00, 1, 1);
IF NOT EXISTS (SELECT 1 FROM Users WHERE Email = 'iris@example.com')
    INSERT INTO Users (PasswordHash, Email, FirstName, LastName, Phone, Role, CreationDate, LastLoginDate, Balance, IsActive, password_set) VALUES
    ('$2a$12$4xZo.kcH.Zbar8PvVZKxfOws3VOEa5xpf153kJ1uAta2fFcIbUpUq', 'iris@example.com', 'Iris', 'Dang', '0999999999', 'user', DATEADD(day, -15, GETDATE()), DATEADD(day, -10, GETDATE()), 400000.00, 1, 1);
IF NOT EXISTS (SELECT 1 FROM Users WHERE Email = 'jack@example.com')
    INSERT INTO Users (PasswordHash, Email, FirstName, LastName, Phone, Role, CreationDate, LastLoginDate, Balance, IsActive, password_set) VALUES
    ('$2a$12$4xZo.kcH.Zbar8PvVZKxfOws3VOEa5xpf153kJ1uAta2fFcIbUpUq', 'jack@example.com', 'Jack', 'Ngo', '0901010101', 'user', DATEADD(day, -10, GETDATE()), DATEADD(day, -1, GETDATE()), 600000.00, 1, 1);

PRINT 'Dummy users seeded successfully.';

DELETE FROM LotteryTickets WHERE TicketID LIKE 'E2E-%';

INSERT INTO LotteryTickets (TicketID, Company, [Number 1], [Number 2], [Number 3], [Number 4], [Number 5], [Number 6], CreationDate, Published, ViewCount, Price, OwnerID) VALUES
('E2E-001', 'Vietlott', 5, 12, 23, 34, 41, 45, CAST(GETDATE() AS DATE), 1, 10, 10000.00, NULL),
('E2E-002', 'Vietlott', 3, 8, 17, 28, 35, 42, CAST(GETDATE() AS DATE), 1, 5, 10000.00, NULL),
('E2E-003', 'Vietlott', 1, 11, 22, 33, 44, 55, CAST(GETDATE() AS DATE), 1, 3, 10000.00, NULL),
('E2E-004', 'XSMB', 7, 14, 21, 28, 35, 42, CAST(GETDATE() AS DATE), 1, 8, 15000.00, NULL),
('E2E-005', 'XSMB', 2, 9, 16, 25, 36, 49, CAST(GETDATE() AS DATE), 1, 2, 15000.00, NULL),
('E2E-006', 'XSMN', 4, 13, 19, 27, 33, 48, CAST(GETDATE() AS DATE), 1, 6, 12000.00, NULL),
('E2E-007', 'XSMN', 6, 15, 24, 31, 39, 47, CAST(GETDATE() AS DATE), 1, 1, 12000.00, NULL),
('E2E-008', 'XSMT', 10, 20, 30, 40, 50, 60, CAST(GETDATE() AS DATE), 1, 4, 8000.00, NULL),
('E2E-009', 'XSMT', 8, 18, 26, 38, 46, 52, CAST(GETDATE() AS DATE), 1, 7, 8000.00, NULL),
('E2E-010', 'Vietlott', 11, 22, 33, 44, 55, 66, CAST(GETDATE() AS DATE), 1, 0, 10000.00, NULL);

INSERT INTO LotteryTickets (TicketID, Company, [Number 1], [Number 2], [Number 3], [Number 4], [Number 5], [Number 6], CreationDate, Published, ViewCount, Price, OwnerID) VALUES
('E2E-011', 'Vietlott', 1, 2, 3, 4, 5, 6, DATEADD(day, -5, CAST(GETDATE() AS DATE)), 1, 15, 10000.00, @userId),
('E2E-012', 'XSMB', 7, 8, 9, 10, 11, 12, DATEADD(day, -4, CAST(GETDATE() AS DATE)), 1, 12, 15000.00, @userId),
('E2E-013', 'XSMN', 13, 14, 15, 16, 17, 18, DATEADD(day, -3, CAST(GETDATE() AS DATE)), 1, 20, 12000.00, @userId),
('E2E-014', 'Vietlott', 19, 20, 21, 22, 23, 24, DATEADD(day, -2, CAST(GETDATE() AS DATE)), 1, 9, 10000.00, @userId),
('E2E-015', 'XSMT', 25, 26, 27, 28, 29, 30, DATEADD(day, -1, CAST(GETDATE() AS DATE)), 1, 11, 8000.00, @userId);

INSERT INTO LotteryTickets (TicketID, Company, [Number 1], [Number 2], [Number 3], [Number 4], [Number 5], [Number 6], CreationDate, Published, ViewCount, Price, OwnerID) VALUES
('E2E-016', 'Vietlott', 31, 32, 33, 34, 35, 36, CAST(GETDATE() AS DATE), 0, 0, 10000.00, NULL),
('E2E-017', 'XSMB', 37, 38, 39, 40, 41, 42, CAST(GETDATE() AS DATE), 0, 0, 15000.00, NULL),
('E2E-018', 'XSMN', 43, 44, 45, 46, 47, 48, CAST(GETDATE() AS DATE), 0, 0, 12000.00, NULL);

INSERT INTO LotteryTickets (TicketID, Company, [Number 1], [Number 2], [Number 3], [Number 4], [Number 5], [Number 6], CreationDate, Published, ViewCount, Price, OwnerID) VALUES
('E2E-019', 'Vietlott', 49, 50, 51, 52, 53, 54, DATEADD(day, -30, CAST(GETDATE() AS DATE)), 1, 25, 10000.00, NULL),
('E2E-020', 'XSMB', 55, 56, 57, 58, 59, 60, DATEADD(day, -60, CAST(GETDATE() AS DATE)), 1, 30, 15000.00, NULL),
('E2E-021', 'XSMN', 61, 62, 63, 64, 65, 66, DATEADD(day, -90, CAST(GETDATE() AS DATE)), 1, 40, 12000.00, NULL);

INSERT INTO LotteryTickets (TicketID, Company, [Number 1], [Number 2], [Number 3], [Number 4], [Number 5], [Number 6], CreationDate, Published, ViewCount, Price, OwnerID) VALUES
('E2E-P01', 'Vietlott', 1, 3, 5, 7, 9, 11, DATEADD(day, -1, CAST(GETDATE() AS DATE)), 1, 2, 10000.00, NULL),
('E2E-P02', 'Vietlott', 2, 4, 6, 8, 10, 12, DATEADD(day, -1, CAST(GETDATE() AS DATE)), 1, 3, 10000.00, NULL),
('E2E-P03', 'XSMB', 11, 13, 15, 17, 19, 21, DATEADD(day, -1, CAST(GETDATE() AS DATE)), 1, 1, 15000.00, NULL),
('E2E-P04', 'XSMB', 12, 14, 16, 18, 20, 22, DATEADD(day, -1, CAST(GETDATE() AS DATE)), 1, 4, 15000.00, NULL),
('E2E-P05', 'XSMN', 21, 23, 25, 27, 29, 31, DATEADD(day, -1, CAST(GETDATE() AS DATE)), 1, 5, 12000.00, NULL),
('E2E-P06', 'XSMN', 22, 24, 26, 28, 30, 32, DATEADD(day, -2, CAST(GETDATE() AS DATE)), 1, 6, 12000.00, NULL),
('E2E-P07', 'XSMT', 31, 33, 35, 37, 39, 41, DATEADD(day, -2, CAST(GETDATE() AS DATE)), 1, 7, 8000.00, NULL),
('E2E-P08', 'XSMT', 32, 34, 36, 38, 40, 42, DATEADD(day, -2, CAST(GETDATE() AS DATE)), 1, 8, 8000.00, NULL),
('E2E-P09', 'Vietlott', 41, 43, 45, 47, 49, 51, DATEADD(day, -3, CAST(GETDATE() AS DATE)), 1, 9, 10000.00, NULL),
('E2E-P10', 'Vietlott', 42, 44, 46, 48, 50, 52, DATEADD(day, -3, CAST(GETDATE() AS DATE)), 1, 10, 10000.00, NULL);

PRINT 'Lottery tickets seeded successfully.';

DELETE FROM Transactions WHERE UserID = @userId;
DELETE FROM PaymentMethods WHERE UserID = @userId;
DELETE FROM PaymentMethods WHERE UserID = @adminId;

INSERT INTO PaymentMethods (UserID, LastFourDigits, CardHolder, ExpiryDate) VALUES
(@userId, '4242', 'Test User', '12/2028'),
(@userId, '1234', 'Test User', '06/2027');

INSERT INTO PaymentMethods (UserID, LastFourDigits, CardHolder, ExpiryDate) VALUES
(@adminId, '5678', 'Admin Fortuna', '03/2029');

PRINT 'Payment methods seeded successfully.';

DECLARE @pmId1 INT;
SELECT TOP 1 @pmId1 = PaymentMethodID FROM PaymentMethods WHERE UserID = @userId ORDER BY PaymentMethodID ASC;

IF @pmId1 IS NOT NULL
BEGIN
    INSERT INTO Transactions (UserID, Amount, PaymentMethodID) VALUES (@userId, 100000.00, @pmId1);
    INSERT INTO Transactions (UserID, Amount, PaymentMethodID) VALUES (@userId, 200000.00, @pmId1);
    INSERT INTO Transactions (UserID, Amount, PaymentMethodID) VALUES (@userId, 50000.00, @pmId1);
    INSERT INTO Transactions (UserID, Amount, PaymentMethodID) VALUES (@userId, 150000.00, @pmId1);
    PRINT 'Transactions seeded: 4 records.';
END
ELSE
    PRINT 'WARNING: No payment method found for user, skipping transactions.';

DELETE FROM UserSearchHistory WHERE UserID = @userId;

INSERT INTO UserSearchHistory (UserID, SearchPrompt, SearchDate) VALUES
(@userId, 'Vietlott', DATEADD(hour, -1, GETDATE())),
(@userId, 'XSMB', DATEADD(hour, -2, GETDATE())),
(@userId, 'XSMN', DATEADD(day, -1, GETDATE()));

PRINT 'Search history seeded successfully.';

PRINT '';
PRINT '=== SEED DATA VERIFICATION ===';

SELECT 'Users (total)' AS [Table], COUNT(*) AS [Count] FROM Users
UNION ALL
SELECT 'LotteryTickets (E2E)', COUNT(*) FROM LotteryTickets WHERE TicketID LIKE 'E2E-%'
UNION ALL
SELECT 'PaymentMethods (user)', COUNT(*) FROM PaymentMethods WHERE UserID = @userId
UNION ALL
SELECT 'PaymentMethods (admin)', COUNT(*) FROM PaymentMethods WHERE UserID = @adminId
UNION ALL
SELECT 'Transactions (user)', COUNT(*) FROM Transactions WHERE UserID = @userId
UNION ALL
SELECT 'SearchHistory (user)', COUNT(*) FROM UserSearchHistory WHERE UserID = @userId;

PRINT '';
PRINT 'E2E test data seeding complete!';
GO
