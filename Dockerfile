# === Build stage ===
FROM gradle:8.5-jdk21 AS build
WORKDIR /app

# Copy only files needed to download dependencies first (for caching)
COPY gradlew .
COPY gradle ./gradle
COPY build.gradle.kts settings.gradle.kts gradle.properties ./

# Nadaj uprawnienia
RUN chmod +x gradlew

# Download dependencies
RUN #gradle build --no-daemon --stacktrace || true
RUN ./gradlew dependencies --no-daemon

# Copy rest of the project and build it
COPY . .

# Build the project
RUN gradle clean shadowJar --no-daemon

# === Runtime stage ===
FROM eclipse-temurin:21-jre-jammy
WORKDIR /kontenerki

# Copy the built JAR from the build stage
COPY --from=build /app/build/libs/*.jar ./email.jar

# Expose the port the Ktor app runs on
EXPOSE 8200

# Environment variables
ENV API_NAME=api
ENV API_PORT=100
ENV EMAIL_PASSWORD=ehpejfervmuwjwrg
ENV EMAIL_USER=parkingostrowskiego@gmail.com

# Run the app
CMD ["java", "-jar", "email.jar"]
