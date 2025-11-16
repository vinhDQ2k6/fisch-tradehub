CREATE TABLE
  `user` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `email` VARCHAR(255) NOT NULL,
    `username` VARCHAR(255) NOT NULL,
    `password` VARCHAR(255) NOT NULL,
    `role` VARCHAR(50) NOT NULL,
    `active` BOOLEAN NOT NULL DEFAULT '1',
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY `user_email_unique` (`email`),
    UNIQUE KEY `user_username_unique` (`username`),
    KEY `user_username_index` (`username`)
  );

CREATE TABLE
  `user_info` (
    `user_id` BIGINT UNSIGNED NOT NULL PRIMARY KEY,
    `fullname` VARCHAR(255) NULL,
    `age` TINYINT UNSIGNED NULL,
    `gender` BOOLEAN NULL,
    CONSTRAINT `user_info_user_fk` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
  );

CREATE TABLE
  `fish` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(255) NOT NULL,
    `rarity` VARCHAR(255) NOT NULL,
    `value` DECIMAL(10, 2) NOT NULL,
    `weight` DECIMAL(10, 2) NOT NULL
  );

CREATE TABLE
  `cart` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT UNSIGNED NOT NULL,
    `fish_id` BIGINT UNSIGNED NOT NULL,
    `quantity` INT NOT NULL,
    CONSTRAINT `cart_user_fk` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
    CONSTRAINT `cart_fish_fk` FOREIGN KEY (`fish_id`) REFERENCES `fish` (`id`),
    -- Index cho truy vấn theo user
    KEY `cart_user_idx` (`user_id`),
    -- Unique đảm bảo 1 user không có 2 dòng cho cùng 1 fish
    UNIQUE KEY `cart_user_fish_unique` (`user_id`, `fish_id`)
  );

CREATE TABLE
  `bill` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `buyer_id` BIGINT UNSIGNED NOT NULL,
    `seller_id` BIGINT UNSIGNED NULL,
    `total` DECIMAL(10, 2) NOT NULL,
    `status` TINYINT UNSIGNED NOT NULL DEFAULT 0,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `closed_at` TIMESTAMP NULL,
    `rating` TINYINT NULL,
    CONSTRAINT `bill_buyer_fk` FOREIGN KEY (`buyer_id`) REFERENCES `user` (`id`),
    CONSTRAINT `bill_seller_fk` FOREIGN KEY (`seller_id`) REFERENCES `user` (`id`),
    KEY `bill_buyer_idx` (`buyer_id`),
    KEY `bill_seller_idx` (`seller_id`),
    KEY `bill_status_idx` (`status`)
  );

CREATE TABLE
  `bill_info` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `bill_id` BIGINT UNSIGNED NOT NULL,
    `fish_id` BIGINT UNSIGNED NOT NULL,
    `price` DECIMAL(10, 2) NOT NULL,
    `amount` INT NOT NULL,
    `sum` DECIMAL(10, 2) NOT NULL,
    CONSTRAINT `bill_info_bill_fk` FOREIGN KEY (`bill_id`) REFERENCES `bill` (`id`),
    CONSTRAINT `bill_info_fish_fk` FOREIGN KEY (`fish_id`) REFERENCES `fish` (`id`),
    -- Lấy list items cho 1 bill
    KEY `bill_info_bill_idx` (`bill_id`),
    -- Optional: tránh trùng fish trong cùng bill
    UNIQUE KEY `bill_info_bill_fish_unique` (`bill_id`, `fish_id`)
  );