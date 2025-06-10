# School Management System

A Spring Boot application for managing students and courses in a school system.

## Features

- Student management (CRUD operations)
- Course management
- Student enrollment in courses
- RESTful API endpoints
- PostgreSQL database integration
- Input validation
- Error handling
- Docker support

## Prerequisites

- Java 8 or higher
- Maven
- PostgreSQL
- Docker (optional)

## Database Setup

1. Create a PostgreSQL database named `school_db`
2. Update the database credentials in `src/main/resources/application.properties` if needed

## Running the Application

### Using Maven

1. Clone the repository
2. Navigate to the project directory
3. Run the following command:
   ```bash
   mvn spring-boot:run
   ```

### Using Docker

1. Build the Docker image:
   ```bash
   docker build -t school-management .
   ```

2. Run the container:
   ```bash
   docker run -p 8080:8080 school-management
   ```

## API Endpoints

### Students

- GET `/api/students` - Get all students
- GET `/api/students/{id}` - Get student by ID
- POST `/api/students` - Create a new student
- PUT `/api/students/{id}` - Update a student
- DELETE `/api/students/{id}` - Delete a student
- POST `/api/students/{studentId}/courses/{courseId}` - Enroll a student in a course

## Example API Usage

### Create a Student

```bash
curl -X POST http://localhost:8080/api/students \
-H "Content-Type: application/json" \
-d '{
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@example.com",
    "dateOfBirth": "2000-01-01"
}'
```

### Enroll in a Course

```bash
curl -X POST http://localhost:8080/api/students/1/courses/1
```

## Technologies Used

- Spring Boot
- Spring Data JPA
- PostgreSQL
- Maven
- Docker
- Java 8
- Lombok 