# Build stage
FROM maven:3.8-openjdk-8 AS build
WORKDIR /app

# Configure Maven to use multiple mirrors and increase timeouts
COPY settings.xml /root/.m2/settings.xml

# Copy pom.xml first to cache dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build with retry mechanism and force update
RUN mvn clean package -DskipTests -U --batch-mode \
    -Dmaven.wagon.http.pool=false \
    -Dmaven.wagon.http.readTimeout=60000 \
    -Dmaven.wagon.http.connectionTimeout=60000

# Run stage
FROM openjdk:8-jre-slim
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"] 