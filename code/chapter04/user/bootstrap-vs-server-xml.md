# Understanding Configuration in Open Liberty: bootstrap.properties vs. server.xml

*May 15, 2025*

![Open Liberty Logo](https://openliberty.io/img/blog/logo.png)

When configuring an Open Liberty server for your Jakarta EE or MicroProfile applications, you'll encounter two primary configuration files: `bootstrap.properties` and `server.xml`. Understanding the difference between these files and when to use each one is crucial for effectively managing your server environment.

## The Fundamentals

Before diving into the differences, let's establish what each file does:

- **server.xml**: The main configuration file for defining features, endpoints, applications, and other server settings
- **bootstrap.properties**: Properties loaded early in the server startup process, before server.xml is processed

## bootstrap.properties: The Early Bird

The `bootstrap.properties` file, as its name suggests, is loaded during the bootstrapping phase of the server's lifecycle—before most of the server infrastructure is initialized. This gives it some unique characteristics:

### When to use bootstrap.properties:

1. **Very Early Configuration**: When you need settings available at the earliest stages of server startup
2. **Variable Definition**: Define variables that will be used within your server.xml file
3. **Port Configuration**: Setting default HTTP/HTTPS ports before the server starts
4. **JVM Options Control**: Configure settings that affect how the JVM runs
5. **Logging Configuration**: Set up initial logging parameters before the full logging system initializes

Here's a basic example of a bootstrap.properties file:

```properties
# Application context root
app.context.root=/user

# Default HTTP port
default.http.port=9080

# Default HTTPS port
default.https.port=9443

# Application name
app.name=user
```

### Key Advantage:

Variables defined in bootstrap.properties can be referenced in server.xml using the `${variableName}` syntax, allowing for dynamic configuration. This makes bootstrap.properties an excellent place for environment-specific settings that might change between development, testing, and production environments.

## server.xml: The Main Configuration Hub

The `server.xml` file is where most of your server configuration happens. It's an XML-based file that provides a structured way to define various aspects of your server:

### When to use server.xml:

1. **Feature Configuration**: Enable Jakarta EE and MicroProfile features
2. **Application Deployment**: Define applications and their context roots
3. **Resource Configuration**: Set up data sources, connection factories, etc.
4. **Security Settings**: Configure authentication and authorization
5. **HTTP Endpoints**: Define HTTP/HTTPS endpoints and their properties
6. **Logging Policy**: Set up detailed logging configuration

Here's a simplified example of a server.xml file:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<server description="User Management Server">

    <!-- Enable features -->
    <featureManager>
        <feature>jakartaee-10.0</feature>
        <feature>microProfile-6.1</feature>
    </featureManager>

    <httpEndpoint id="defaultHttpEndpoint"
                  host="*"
                  httpPort="${default.http.port}"
                  httpsPort="${default.https.port}" />
                  
    <webApplication id="user"
                    name="user"
                    location="user.war"
                    contextRoot="${app.context.root}" />

    <logging traceSpecification="*=info" />
</server>
```

Notice how this server.xml references variables from bootstrap.properties using `${variable}` notation.

## Key Differences: A Direct Comparison

| Aspect | bootstrap.properties | server.xml |
|--------|----------------------|------------|
| **Format** | Simple key-value pairs | Structured XML |
| **Loading Time** | Very early in server startup | After bootstrap properties |
| **Typical Use** | Environment variables, ports, paths | Features, applications, resources |
| **Flexibility** | Limited to property values | Full configuration capabilities |
| **Readability** | Simple but limited | More verbose but comprehensive |
| **Hot Deploy** | Changes require server restart | Some changes can be dynamically applied |
| **Variable Use** | Defines variables | Uses variables (from itself or bootstrap) |

## Best Practices: When to Use Each

### Use bootstrap.properties for:

1. **Environment-specific configuration** that might change between development, testing, and production
2. **Port definitions** to ensure consistency across environments
3. **Critical paths** needed early in the startup process
4. **Variables** that will be used throughout server.xml

### Use server.xml for:

1. **Feature enablement** for Jakarta EE and MicroProfile capabilities
2. **Application definition** including location and context root
3. **Resource configuration** like datasources and JMS queues
4. **Detailed security settings** for authentication and authorization
5. **Comprehensive logging configuration**

## Real-world Integration: Making Them Work Together

The real power comes from using these files together. For instance, you might define environment-specific properties in bootstrap.properties:

```properties
# Environment: DEVELOPMENT
env.name=development
db.host=localhost
db.port=5432
db.name=userdb
```

Then reference these in your server.xml:

```xml
<dataSource id="DefaultDataSource" jndiName="jdbc/userDB">
    <properties serverName="${db.host}" 
                portNumber="${db.port}" 
                databaseName="${db.name}" />
</dataSource>

<logging traceFileName="trace-${env.name}.log" />
```

This approach gives you the flexibility to keep environment-specific configuration separate from your main server configuration, making it easier to deploy the same application across different environments.

## Containerization Considerations

In containerized environments, especially with Docker and Kubernetes, bootstrap.properties becomes even more valuable. You can use environment variables to override bootstrap properties, providing a clean integration with container orchestration platforms:

```bash
docker run -e default.http.port=9081 -e app.context.root=/api my-liberty-app
```

## Conclusion

Understanding the distinction between bootstrap.properties and server.xml is essential for effectively managing Open Liberty servers:

- **bootstrap.properties** provides early configuration and variables for use throughout the server
- **server.xml** offers comprehensive configuration for all aspects of the Liberty server

By leveraging both files appropriately, you can create flexible, maintainable server configurations that work consistently across different environments—from local development to production deployment.

The next time you're setting up an Open Liberty server, take a moment to consider which configuration belongs where. Your future self (and your team) will thank you for the clear organization and increased flexibility.

---

*About the Author: A Jakarta EE and MicroProfile enthusiast with extensive experience deploying enterprise Java applications in containerized environments.*
