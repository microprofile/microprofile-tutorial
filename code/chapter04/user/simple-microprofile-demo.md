# Building a Simple MicroProfile Demo Application

## Introduction

When demonstrating MicroProfile and Jakarta EE concepts, keeping things simple is crucial. Too often, we get caught up in advanced patterns and considerations that can obscure the actual standards and APIs we're trying to teach. In this article, I'll outline how we built a straightforward user management API to showcase MicroProfile features without unnecessary complexity.

## The Goal: Focus on Standards, Not Implementation Details

Our primary objective was to create a demo application that clearly illustrates:

- Jakarta Restful Web Services
- CDI for dependency injection
- JSON-B for object serialization
- Bean Validation for input validation
- MicroProfile OpenAPI for API documentation

To achieve this, we deliberately kept our implementation as simple as possible, avoiding distractions like concurrency handling, performance optimizations, or scalability considerations.

## The Simple Approach

### Basic Entity Class

Our User entity is a straightforward POJO with validation annotations:

```java
public class User {
    private Long userId;

    @NotEmpty(message = "Name cannot be empty")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @NotEmpty(message = "Email cannot be empty")
    @Email(message = "Email should be valid")
    private String email;

    private String passwordHash;
    private String address;
    private String phoneNumber;
    
    // Getters and setters
}
```

### Simple Repository

For data access, we used a basic in-memory HashMap:

```java
@ApplicationScoped
public class UserRepository {
    private final Map<Long, User> users = new HashMap<>();
    private long nextId = 1;
    
    public User save(User user) {
        if (user.getUserId() == null) {
            user.setUserId(nextId++);
        }
        users.put(user.getUserId(), user);
        return user;
    }
    
    // Other basic CRUD methods
}
```

### Straightforward Service Layer

The service layer focuses on business logic without unnecessary complexity:

```java
@ApplicationScoped
public class UserService {
    @Inject
    private UserRepository userRepository;
    
    public User createUser(User user) {
        // Basic validation logic
        Optional<User> existingUser = userRepository.findByEmail(user.getEmail());
        if (existingUser.isPresent()) {
            throw new WebApplicationException("Email already in use", Response.Status.CONFLICT);
        }
        
        // Simple password hashing
        if (user.getPasswordHash() != null) {
            user.setPasswordHash(hashPassword(user.getPasswordHash()));
        }
        
        return userRepository.save(user);
    }
    
    // Other business methods
}
```

### Clear REST Resources

Our REST endpoints are annotated with OpenAPI documentation:

```java
@Path("/users")
@Tag(name = "User Management", description = "Operations for managing users")
public class UserResource {
    @Inject
    private UserService userService;
    
    @GET
    @Operation(summary = "Get all users")
    @APIResponse(responseCode = "200", description = "List of users")
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }
    
    // Other endpoints
}
```

## When You Should Consider More Advanced Approaches

While our simple approach works well for demonstration purposes, production applications would benefit from additional considerations:

- Thread safety for shared state (using ConcurrentHashMap, AtomicLong, etc.)
- Security hardening beyond basic password hashing
- Proper error handling and logging
- Connection pooling for database access
- Transaction management

