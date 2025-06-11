# Build stage
FROM maven:3.8-openjdk-8 AS build
WORKDIR /app

# Configure Maven to use multiple mirrors and increase timeouts
COPY settings.xml /root/.m2/settings.xml

# Copy pom.xml first to cache dependencies
COPY pom.xml .

# Enable Maven debug logging and show dependency tree
RUN mvn dependency:tree -Dverbose && \
    mvn dependency:go-offline -B -X

# Copy source code
COPY src ./src

# Build with detailed error reporting
RUN mvn clean package -DskipTests -U --batch-mode \
    -Dmaven.wagon.http.pool=false \
    -Dmaven.wagon.http.readTimeout=60000 \
    -Dmaven.wagon.http.connectionTimeout=60000 \
    -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=WARN \
    -Dorg.slf4j.simpleLogger.showDateTime=true \
    -Dorg.slf4j.simpleLogger.dateTimeFormat=HH:mm:ss.SSS \
    -Dorg.slf4j.simpleLogger.showThreadName=true \
    -Dorg.slf4j.simpleLogger.showLogName=true \
    -Dorg.slf4j.simpleLogger.defaultLogLevel=DEBUG \
    -Dmaven.test.failure.ignore=true \
    -Dmaven.compiler.failOnError=true \
    -Dmaven.compiler.showWarnings=true \
    -Dmaven.compiler.showDeprecation=true \
    -Dmaven.compiler.verbose=true

# Run stage
FROM openjdk:8-jre-slim
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"] 