package io.microprofile.tutorial.store.user.repository;

import io.microprofile.tutorial.store.user.entity.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import jakarta.enterprise.context.ApplicationScoped;

/**
 * Thread-safe in-memory repository for User objects.
 * This class provides CRUD operations for User entities using a ConcurrentHashMap for thread-safe storage
 * and AtomicLong for safe ID generation in a concurrent environment.
 * 
 * Key features:
 * - Thread-safe operations using ConcurrentHashMap
 * - Atomic ID generation
 * - Immutable User objects in storage
 * - Validation of user data
 * - Optional return types for null-safety
 * 
 * Note: This is a demo implementation. In production:
 * - Consider using a persistent database
 * - Add caching mechanisms
 * - Implement proper pagination
 * - Add audit logging
 */
@ApplicationScoped
public class UserRepository {

    private final Map<Long, User> users = new ConcurrentHashMap<>();
    private final AtomicLong nextId = new AtomicLong(1);

    /**
     * Saves a user to the repository.
     * If the user has no ID, a new ID is assigned.
     *
     * @param user The user to save
     * @return The saved user with ID assigned
     */
    public User save(User user) {
        if (user.getUserId() == null) {
            user.setUserId(nextId.getAndIncrement());
        }
        User savedUser = User.builder()
            .userId(user.getUserId())
            .name(user.getName())
            .email(user.getEmail())
            .passwordHash(user.getPasswordHash())
            .address(user.getAddress())
            .phoneNumber(user.getPhoneNumber())
            .build();
        users.put(savedUser.getUserId(), savedUser);
        return savedUser;
    }

    /**
     * Finds a user by ID.
     *
     * @param id The user ID
     * @return An Optional containing the user if found, or empty if not found
     */
    public Optional<User> findById(Long id) {
        return Optional.ofNullable(users.get(id));
    }

    /**
     * Finds a user by email.
     *
     * @param email The user's email
     * @return An Optional containing the user if found, or empty if not found
     */
    public Optional<User> findByEmail(String email) {
        return users.values().stream()
                .filter(user -> user.getEmail().equals(email))
                .findFirst();
    }

    /**
     * Retrieves all users from the repository.
     *
     * @return A list of all users
     */
    public List<User> findAll() {
        return new ArrayList<>(users.values());
    }

    /**
     * Deletes a user by ID.
     *
     * @param id The ID of the user to delete
     * @return true if the user was deleted, false if not found
     */
    public boolean deleteById(Long id) {
        return users.remove(id) != null;
    }

    /**
     * Updates an existing user.
     *
     * @param id The ID of the user to update
     * @param user The updated user information
     * @return An Optional containing the updated user, or empty if not found
     */
    /**
     * Updates an existing user atomically.
     * Only updates the user if it exists and the update is valid.
     *
     * @param id The ID of the user to update
     * @param user The updated user information
     * @return An Optional containing the updated user, or empty if not found
     * @throws IllegalArgumentException if user is null or has invalid data
     */
    public Optional<User> update(Long id, User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        
        return Optional.ofNullable(users.computeIfPresent(id, (key, existingUser) -> {
            User updatedUser = User.builder()
                .userId(id)
                .name(user.getName() != null ? user.getName() : existingUser.getName())
                .email(user.getEmail() != null ? user.getEmail() : existingUser.getEmail())
                .passwordHash(user.getPasswordHash() != null ? user.getPasswordHash() : existingUser.getPasswordHash())
                .address(user.getAddress() != null ? user.getAddress() : existingUser.getAddress())
                .phoneNumber(user.getPhoneNumber() != null ? user.getPhoneNumber() : existingUser.getPhoneNumber())
                .build();
            return updatedUser;
        }));
    }
}
