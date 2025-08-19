# -----------------------
# Build stage
# -----------------------
FROM maven:3.9.5-eclipse-temurin-17 AS build
WORKDIR /app
# Copy pom.xml và tải dependencies để tận dụng cache
COPY pom.xml .
RUN mvn dependency:go-offline -B
# Copy thêm thư mục config để checkstyle có file config
COPY config ./config
# Copy source code
COPY src ./src
# Build ứng dụng
RUN mvn clean package -DskipTests

# -----------------------
# Runtime stage
# -----------------------
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
# Copy file JAR đã build ở stage trước
COPY --from=build /app/target/*.jar app.jar
# Expose port
EXPOSE 8080
# Environment variables cho H2 (nếu chạy embedded DB)
ENV SPRING_DATASOURCE_URL=jdbc:h2:file:./data/storeapp
ENV SPRING_DATASOURCE_DRIVER_CLASS_NAME=org.h2.Driver
ENV SPRING_DATASOURCE_USERNAME=sa
ENV SPRING_DATASOURCE_PASSWORD=password
ENV SPRING_H2_CONSOLE_ENABLED=true
ENV SPRING_H2_CONSOLE_PATH=/h2-console
# Start app
ENTRYPOINT ["java", "-jar", "app.jar"]