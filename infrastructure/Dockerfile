# Build stage
FROM gradle:8.12-jdk17-corretto AS build
WORKDIR /app

# Create builduser with proper home directory and permissions
RUN groupadd -r builduser && \
    useradd -r -g builduser -m -d /home/builduser builduser && \
    chown -R builduser:builduser /home/builduser && \
    chown -R builduser:builduser /app

# Switch to builduser
USER builduser

# Set GRADLE_USER_HOME to a directory we own
ENV GRADLE_USER_HOME=/home/builduser/.gradle

# Copy only necessary files first to leverage cache
COPY --chown=builduser:builduser backend/build.gradle.kts backend/settings.gradle.kts backend/gradlew backend/.editorconfig ./
COPY --chown=builduser:builduser backend/gradle ./gradle

# Download dependencies
RUN ./gradlew dependencies

# Copy source code
COPY --chown=builduser:builduser backend/src ./src

# Build the application
RUN ./gradlew build

# Copy the entrypoint script and make it executable
COPY --chown=builduser:builduser backend/src/main/docker/entrypoint.sh /app/entrypoint.sh
RUN chmod +x /app/entrypoint.sh

# Run stage
FROM eclipse-temurin:17-jre-ubi9-minimal

# Create appuser with proper home directory
RUN groupadd -r appuser && \
    useradd -r -g appuser -m -d /home/appuser appuser

WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar
COPY --from=build /app/entrypoint.sh /app/entrypoint.sh

# Set proper permissions
RUN chown -R appuser:appuser /app /home/appuser
USER appuser

# Expose the application port
EXPOSE 8080

# Add basic healthcheck
HEALTHCHECK --interval=30s --timeout=3s --start-period=30s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Run the application with basic container support
ENTRYPOINT ["/app/entrypoint.sh"] 