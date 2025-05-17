# F1 Champions Assessment

A Spring Boot application that provides information about Formula 1 World Champions and race winners from 2005 to the present.

## Technical Stack

- Spring Boot 3.2.3
- Kotlin 1.9.22
- PostgreSQL
- Spring Data JPA
- Spring Web
- Springdoc OpenAPI

## Prerequisites

- JDK 17 or higher
- PostgreSQL 12 or higher
- Gradle 7.6 or higher

## Environment Variables

The following environment variables can be configured:

| Variable | Description | Default |
|----------|-------------|---------|
| DB_USERNAME | PostgreSQL username | postgres |
| DB_PASSWORD | PostgreSQL password | postgres |
| SERVER_PORT | Application port | 8080 |

## Setup Instructions

1. Clone the repository
2. Create a PostgreSQL database named `f1_champions`
3. Configure environment variables if needed
4. Run the application:
   ```bash
   ./gradlew bootRun
   ```

## API Documentation

Once the application is running, you can access:
- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI Documentation: http://localhost:8080/api-docs

## Project Structure

```
src/main/kotlin/com/f1champions/
├── F1ChampionsApplication.kt
├── controller/
├── service/
├── repository/
├── entity/
└── dto/
```

## Development

To build the project:
```bash
./gradlew build
```

To run tests:
```bash
./gradlew test
```

## Monitoring

The application exposes the following actuator endpoints:
- Health check: http://localhost:8080/actuator/health
- Metrics: http://localhost:8080/actuator/metrics
- Application info: http://localhost:8080/actuator/info

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details. 