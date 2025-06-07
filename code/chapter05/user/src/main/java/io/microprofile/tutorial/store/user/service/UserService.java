package io.microprofile.tutorial.store.user.service;

import io.microprofile.tutorial.store.user.entity.User;
import io.microprofile.tutorial.store.user.repository.UserRepository;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

/**
 * Service class for User management operations.
 */
@ApplicationScoped
public class UserService {

    @Inject
    private UserRepository userRepository;

    /**
     * Creates a new user.
     *
     * @param user The user to create
     * @return The created user
     * @throws WebApplicationException if a user with the email already exists
     */
    public User createUser(User user) {
        // Check if email already exists
        Optional<User> existingUser = userRepository.findByEmail(user.getEmail());
        if (existingUser.isPresent()) {
            throw new WebApplicationException("Email already in use", Response.Status.CONFLICT);
        }

        // Hash the password
        if (user.getPasswordHash() != null) {
            user.setPasswordHash(hashPassword(user.getPasswordHash()));
        }
        
        return userRepository.save(user);
    }

    /**
     * Gets a user by ID.
     *
     * @param id The user ID
     * @return The user
     * @throws WebApplicationException if the user is not found
     */
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new WebApplicationException("User not found", Response.Status.NOT_FOUND));
    }

    /**
     * Gets all users.
     *
     * @return A list of all users
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Updates a user.
     *
     * @param id The user ID
     * @param user The updated user information
     * @return The updated user
     * @throws WebApplicationException if the user is not found or if updating to an email that's already in use
     */
    public User updateUser(Long id, User user) {
        // Check if email already exists and belongs to another user
        Optional<User> existingUserWithEmail = userRepository.findByEmail(user.getEmail());
        if (existingUserWithEmail.isPresent() && !existingUserWithEmail.get().getUserId().equals(id)) {
            throw new WebApplicationException("Email already in use", Response.Status.CONFLICT);
        }

        // Hash the password if it has changed
        if (user.getPasswordHash() != null && 
            !user.getPasswordHash().matches("^[a-fA-F0-9]{64}$")) { // Simple check if it's already a SHA-256 hash
            user.setPasswordHash(hashPassword(user.getPasswordHash()));
        }
        
        return userRepository.update(id, user)
                .orElseThrow(() -> new WebApplicationException("User not found", Response.Status.NOT_FOUND));
    }

    /**
     * Deletes a user.
     *
     * @param id The user ID
     * @throws WebApplicationException if the user is not found
     */
    public void deleteUser(Long id) {
        boolean deleted = userRepository.deleteById(id);
        if (!deleted) {
            throw new WebApplicationException("User not found", Response.Status.NOT_FOUND);
        }
    }

    /**
     * Simple password hashing using SHA-256.
     * Note: In a production environment, use a more secure hashing algorithm with salt
     *
     * @param password The password to hash
     * @return The hashed password
     */
    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to hash password", e);
        }
    }
}
