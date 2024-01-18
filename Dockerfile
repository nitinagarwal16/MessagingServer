# Base image with Java 21
FROM maven:3.8.7

# Set working directory
WORKDIR /app

# Copy project files
COPY . .

# Build the application
RUN mvn clean package

# Expose application port
EXPOSE 8080

# Run the application
CMD ["java", "-jar", "target/MessagingServer-1.0.0.jar"]