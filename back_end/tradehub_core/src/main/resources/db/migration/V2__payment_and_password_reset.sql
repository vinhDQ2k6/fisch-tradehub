-- Add transaction_id column to bill table for payment tracking
ALTER TABLE `bill` ADD COLUMN `transaction_id` VARCHAR(100) NULL;

-- Create password_reset_token table
CREATE TABLE
  `password_reset_token` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `token` VARCHAR(255) NOT NULL,
    `user_id` BIGINT UNSIGNED NOT NULL,
    `expiry_date` TIMESTAMP NOT NULL,
    `used` BOOLEAN NOT NULL DEFAULT FALSE,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_reset_token_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
    UNIQUE KEY `idx_reset_token` (`token`),
    KEY `idx_reset_user` (`user_id`)
  );
