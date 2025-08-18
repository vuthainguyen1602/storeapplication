# Store Application

A Spring Boot-based store application for managing products, customers, and receipts.

## Features

- Product management
- Customer management
- Receipt calculation and generation
- RESTful API endpoints

## Prerequisites

- Java 17 or higher
- Maven 3.6.3 or higher
- Your favorite IDE (IntelliJ IDEA, Eclipse, etc.)

## Getting Started

1. **Clone the repository**
   ```bash
   git clone [your-repository-url]
   cd storeapplication
   ```

2. **Build the application**
   ```bash
   mvn clean install
   ```

3. **Run the application**
   ```bash
   mvn spring-boot:run
   ```

## Project Structure

```
src/
├── main/
│   └── java/
│       └── com/example/storeapplication/
│           ├── controller/     # REST controllers
│           ├── domain/          # Entity classes
│           ├── repository/     # Data access layer
│           ├── service/        # Business logic
│           │   ├── impl/       # Service implementations
│           └── StoreApplication.java  # Main application class
└── test/                      # Test files
```

## API Documentation

Once the application is running, you can access:
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **H2 Console**: http://localhost:8080/h2-console
  - JDBC URL: jdbc:h2:mem:testdb
  - Username: sa
  - Password: (leave empty)

## Testing

Run the test suite with:
```bash
mvn test
```

## Built With

- [Spring Boot](https://spring.io/projects/spring-boot)
- [Maven](https://maven.apache.org/)
- [H2 Database](https://www.h2database.com/)

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
