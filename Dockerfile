FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .
COPY src src

# Ensure gradlew has execution permissions
RUN chmod +x ./gradlew

# Build with verbose output to diagnose issues
RUN ./gradlew bootJar --info

# Verify the JAR was created
RUN ls -la build/libs/

# Copy the generated jar to a fixed filename for easy execution
RUN cp build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]