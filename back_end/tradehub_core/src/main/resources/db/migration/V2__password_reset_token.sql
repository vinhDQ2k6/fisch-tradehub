-- Add transaction_id column to bill table
ALTER TABLE `bill` ADD COLUMN `transaction_id` VARCHAR(255) NULL;

-- Create password reset token table
CREATE TABLE `password_reset_token` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `token` VARCHAR(255) NOT NULL,
    `user_id` BIGINT UNSIGNED NOT NULL,
    `expiry_date` TIMESTAMP NOT NULL,
    `used` BOOLEAN NOT NULL DEFAULT FALSE,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_reset_token_user` FOREIGN KEY (`user_id`) REFERENCES `user`(`id`),
    UNIQUE KEY `password_reset_token_unique` (`token`)
);

CREATE INDEX `idx_reset_token` ON `password_reset_token`(`token`);
CREATE INDEX `idx_reset_user` ON `password_reset_token`(`user_id`);
CREATE INDEX `idx_reset_expiry` ON `password_reset_token`(`expiry_date`);
