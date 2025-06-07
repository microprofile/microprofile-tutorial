package io.microprofile.tutorial.store.user.entity;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * User entity for the microprofile tutorial store application.
 * Represents a user in the system with their profile information.
 * Uses in-memory storage with thread-safe operations.
 * 
 * Key features:
 * - Validated user information
 * - Secure password storage (hashed)
 * - Contact information validation
 * 
 * Potential improvements:
 * 1. Auditing fields:
 *    - createdAt: Timestamp for account creation
 *    - modifiedAt: Last modification timestamp
 *    - version: For optimistic locking in concurrent updates
 *    
 * 2. Security enhancements:
 *    - passwordSalt: For more secure password hashing
 *    - lastPasswordChange: Track password updates
 *    - failedLoginAttempts: For account security
 *    - accountLocked: Boolean for account status
 *    - lockTimeout: Timestamp for temporary locks
 *    
 * 3. Additional features:
 *    - userRole: ENUM for role-based access (USER, ADMIN, etc.)
 *    - status: ENUM for account state (ACTIVE, INACTIVE, SUSPENDED)
 *    - emailVerified: Boolean for email verification
 *    - timeZone: User's preferred timezone
 *    - locale: User's preferred language/region
 *    - lastLoginAt: Track user activity
 *    
 * 4. Compliance:
 *    - privacyPolicyAccepted: Track user consent
 *    - marketingPreferences: User communication preferences
 *    - dataRetentionPolicy: For GDPR compliance
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    private Long userId;

    @NotEmpty(message = "Name cannot be empty")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @NotEmpty(message = "Email cannot be empty")
    @Email(message = "Email should be valid")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    private String email;

    @NotEmpty(message = "Password hash cannot be empty")
    private String passwordHash;
    
    @Size(max = 200, message = "Address must not exceed 200 characters")
    private String address;
    
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Phone number must be in E.164 format")
    @Size(max = 15, message = "Phone number must not exceed 15 characters")
    private String phoneNumber;
}
