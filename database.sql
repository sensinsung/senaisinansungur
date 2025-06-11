CREATE DATABASE senaisinansungur;
GO

USE senaisinansungur;
GO

CREATE TABLE Roles (
    role_id INT IDENTITY(1,1) PRIMARY KEY,
    role_name NVARCHAR(50) NOT NULL UNIQUE,
    created_at DATETIME DEFAULT GETDATE()
);
GO

INSERT INTO Roles (role_name) VALUES ('admin');
INSERT INTO Roles (role_name) VALUES ('kullanıcı');
GO

CREATE TABLE Users (
    userid INT IDENTITY(1,1) PRIMARY KEY,
    first_name NVARCHAR(50) NOT NULL,
    last_name NVARCHAR(50) NOT NULL,
    username NVARCHAR(50) NOT NULL UNIQUE,
    email NVARCHAR(100) NOT NULL UNIQUE,
    password_hash NVARCHAR(256) NOT NULL,
    profile_picture VARBINARY(MAX),
    role_id INT NOT NULL DEFAULT 2,
    is_private BIT NOT NULL DEFAULT 0,
    created_at DATETIME DEFAULT GETDATE(),
    CONSTRAINT FK_Users_Roles FOREIGN KEY (role_id) REFERENCES Roles(role_id)
);
GO

CREATE TABLE Follows (
    follow_id INT IDENTITY(1,1) PRIMARY KEY,
    follower_id INT NOT NULL,
    following_id INT NOT NULL,
    created_at DATETIME DEFAULT GETDATE(),
    CONSTRAINT FK_Follows_Follower FOREIGN KEY (follower_id) REFERENCES Users(userid),
    CONSTRAINT FK_Follows_Following FOREIGN KEY (following_id) REFERENCES Users(userid),
    CONSTRAINT UQ_Follows_Unique UNIQUE (follower_id, following_id)
);
GO

CREATE TABLE Notification (
    request_id INT IDENTITY(1,1) PRIMARY KEY,
    sender_id INT NOT NULL,
    receiver_id INT NOT NULL,
    created_at DATETIME DEFAULT GETDATE(),
    CONSTRAINT FK_Notification_Sender FOREIGN KEY (sender_id) REFERENCES Users(userid),
    CONSTRAINT FK_Notification_Receiver FOREIGN KEY (receiver_id) REFERENCES Users(userid),
    CONSTRAINT UQ_Notification_Unique UNIQUE (sender_id, receiver_id)
);
GO

CREATE VIEW UserFollowersCount AS
SELECT 
    u.userid,
    u.username,
    COUNT(f.follower_id) as follower_count
FROM Users u
LEFT JOIN Follows f ON u.userid = f.following_id
GROUP BY u.userid, u.username;
GO

CREATE VIEW UserFollowingCount AS
SELECT 
    u.userid,
    u.username,
    COUNT(f.following_id) as following_count
FROM Users u
LEFT JOIN Follows f ON u.userid = f.follower_id
GROUP BY u.userid, u.username;
GO

CREATE VIEW UserFollowers AS
SELECT 
    u.userid,
    u.username,
    u.first_name,
    u.last_name,
    f.created_at as followed_at
FROM Users u
INNER JOIN Follows f ON u.userid = f.follower_id
WHERE f.following_id = u.userid;
GO

CREATE VIEW UserFollowing AS
SELECT 
    u.userid,
    u.username,
    u.first_name,
    u.last_name,
    f.created_at as followed_at
FROM Users u
INNER JOIN Follows f ON u.userid = f.following_id
WHERE f.follower_id = u.userid;
GO

CREATE VIEW UserProfile AS
SELECT 
    u.userid,
    u.username,
    u.first_name,
    u.last_name,
    u.profile_picture,
    u.is_private,
    u.created_at,
    (SELECT COUNT(*) FROM Follows WHERE following_id = u.userid) as follower_count,
    (SELECT COUNT(*) FROM Follows WHERE follower_id = u.userid) as following_count
FROM Users u;
GO

CREATE TABLE notification_types (
    type_id INT IDENTITY(1,1) PRIMARY KEY,
    type_name NVARCHAR(50) NOT NULL UNIQUE,
    created_at DATETIME DEFAULT GETDATE()
);
GO

INSERT INTO notification_types (type_name) VALUES ('takip_onay');
GO

CREATE TABLE Notifications (
    notification_id INT IDENTITY(1,1) PRIMARY KEY,
    type_id INT NOT NULL,
    sender_id INT,
    receiver_id INT NOT NULL,
    content NVARCHAR(500) NOT NULL,
    is_read BIT NOT NULL DEFAULT 0,
    created_at DATETIME DEFAULT GETDATE(),
    request_id INT,
    CONSTRAINT FK_Notifications_Type FOREIGN KEY (type_id) REFERENCES notification_types(type_id),
    CONSTRAINT FK_Notifications_Sender FOREIGN KEY (sender_id) REFERENCES Users(userid),
    CONSTRAINT FK_Notifications_Receiver FOREIGN KEY (receiver_id) REFERENCES Users(userid)
);
GO

CREATE VIEW UnreadNotificationsCount AS
SELECT 
    u.userid,
    COUNT(n.notification_id) as unread_count
FROM Users u
LEFT JOIN Notifications n ON u.userid = n.receiver_id AND n.is_read = 0
GROUP BY u.userid;
GO

CREATE TABLE post_privacy_types (
    privacy_id INT IDENTITY(1,1) PRIMARY KEY,
    privacy_name NVARCHAR(50) NOT NULL UNIQUE,
    created_at DATETIME DEFAULT GETDATE()
);
GO

INSERT INTO post_privacy_types (privacy_name) VALUES ('herkese_açık');
INSERT INTO post_privacy_types (privacy_name) VALUES ('arkadaşlar');
INSERT INTO post_privacy_types (privacy_name) VALUES ('özel');
GO

CREATE TABLE Posts (
    post_id INT IDENTITY(1,1) PRIMARY KEY,
    user_id INT NOT NULL,
    content NVARCHAR(1000),
    privacy_id INT NOT NULL DEFAULT 1,
    created_at DATETIME DEFAULT GETDATE(),
    updated_at DATETIME,
    CONSTRAINT FK_Posts_Users FOREIGN KEY (user_id) REFERENCES Users(userid),
    CONSTRAINT FK_Posts_Privacy FOREIGN KEY (privacy_id) REFERENCES post_privacy_types(privacy_id)
);
GO

CREATE TABLE post_photos (
    photo_id INT IDENTITY(1,1) PRIMARY KEY,
    post_id INT NOT NULL,
    photo_data VARBINARY(MAX) NOT NULL,
    photo_order INT NOT NULL,
    created_at DATETIME DEFAULT GETDATE(),
    CONSTRAINT FK_post_photos_Posts FOREIGN KEY (post_id) REFERENCES Posts(post_id) ON DELETE CASCADE
);
GO

CREATE TABLE post_likes (
    like_id INT IDENTITY(1,1) PRIMARY KEY,
    post_id INT NOT NULL,
    user_id INT NOT NULL,
    created_at DATETIME DEFAULT GETDATE(),
    CONSTRAINT FK_post_likes_Posts FOREIGN KEY (post_id) REFERENCES Posts(post_id) ON DELETE CASCADE,
    CONSTRAINT FK_post_likes_Users FOREIGN KEY (user_id) REFERENCES Users(userid),
    CONSTRAINT UQ_post_likes_Unique UNIQUE (post_id, user_id)
);
GO

CREATE TABLE Comments (
    comment_id INT IDENTITY(1,1) PRIMARY KEY,
    post_id INT NOT NULL,
    user_id INT NOT NULL,
    content NVARCHAR(1000) NOT NULL,
    created_at DATETIME DEFAULT GETDATE(),
    updated_at DATETIME,
    CONSTRAINT FK_Comments_Posts FOREIGN KEY (post_id) REFERENCES Posts(post_id) ON DELETE CASCADE,
    CONSTRAINT FK_Comments_Users FOREIGN KEY (user_id) REFERENCES Users(userid)
);
GO

CREATE TABLE comment_likes (
    like_id INT IDENTITY(1,1) PRIMARY KEY,
    comment_id INT NOT NULL,
    user_id INT NOT NULL,
    created_at DATETIME DEFAULT GETDATE(),
    CONSTRAINT FK_comment_likes_Comments FOREIGN KEY (comment_id) REFERENCES Comments(comment_id) ON DELETE CASCADE,
    CONSTRAINT FK_comment_likes_Users FOREIGN KEY (user_id) REFERENCES Users(userid),
    CONSTRAINT UQ_comment_likes_Unique UNIQUE (comment_id, user_id)
);
GO

CREATE TABLE comment_replies (
    reply_id INT IDENTITY(1,1) PRIMARY KEY,
    comment_id INT NOT NULL,
    user_id INT NOT NULL,
    content NVARCHAR(1000) NOT NULL,
    created_at DATETIME DEFAULT GETDATE(),
    updated_at DATETIME,
    CONSTRAINT FK_comment_replies_Comments FOREIGN KEY (comment_id) REFERENCES Comments(comment_id) ON DELETE CASCADE,
    CONSTRAINT FK_comment_replies_Users FOREIGN KEY (user_id) REFERENCES Users(userid)
);
GO

CREATE TABLE reply_likes (
    like_id INT IDENTITY(1,1) PRIMARY KEY,
    reply_id INT NOT NULL,
    user_id INT NOT NULL,
    created_at DATETIME DEFAULT GETDATE(),
    CONSTRAINT FK_reply_likes_Replies FOREIGN KEY (reply_id) REFERENCES comment_replies(reply_id) ON DELETE CASCADE,
    CONSTRAINT FK_reply_likes_Users FOREIGN KEY (user_id) REFERENCES Users(userid),
    CONSTRAINT UQ_reply_likes_Unique UNIQUE (reply_id, user_id)
);
GO

CREATE VIEW PostPhotoCount AS
SELECT 
    p.post_id,
    COUNT(pp.photo_id) as photo_count
FROM Posts p
LEFT JOIN post_photos pp ON p.post_id = pp.post_id
GROUP BY p.post_id;
GO

CREATE VIEW PostLikeCount AS
SELECT 
    p.post_id,
    COUNT(pl.like_id) as like_count
FROM Posts p
LEFT JOIN post_likes pl ON p.post_id = pl.post_id
GROUP BY p.post_id;
GO

CREATE VIEW CommentLikeCount AS
SELECT 
    c.comment_id,
    COUNT(cl.like_id) as like_count
FROM Comments c
LEFT JOIN comment_likes cl ON c.comment_id = cl.comment_id
GROUP BY c.comment_id;
GO

CREATE VIEW ReplyLikeCount AS
SELECT 
    r.reply_id,
    COUNT(rl.like_id) as like_count
FROM comment_replies r
LEFT JOIN reply_likes rl ON r.reply_id = rl.reply_id
GROUP BY r.reply_id;
GO

CREATE VIEW PostVisibility AS
SELECT 
    p.post_id,
    p.user_id,
    p.content,
    p.privacy_id,
    pt.privacy_name,
    u.is_private as user_is_private,
    CASE 
        WHEN p.privacy_id = 3 THEN 0
        WHEN p.privacy_id = 1 THEN 1
        WHEN p.privacy_id = 2 AND u.is_private = 0 THEN 1
        WHEN p.privacy_id = 2 AND u.is_private = 1 THEN 0
        ELSE 0
    END as is_visible
FROM Posts p
INNER JOIN post_privacy_types pt ON p.privacy_id = pt.privacy_id
INNER JOIN Users u ON p.user_id = u.userid;
GO