package com.school.service;
import com.school.dto.CourseDTO;
import com.school.dto.StudentDTO;
import com.school.entity.Student;
import com.school.entity.Course;
import com.school.repository.StudentRepository;
import com.school.repository.CourseRepository;
import com.school.exception.ResourceNotFoundException;
import com.school.exception.DuplicateResourceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Service
@Transactional
public class StudentService {

    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final ExecutorService executorService;

    @Autowired
    public StudentService(StudentRepository studentRepository, CourseRepository courseRepository) {
        this.studentRepository = studentRepository;
        this.courseRepository = courseRepository;
        this.executorService = Executors.newFixedThreadPool(5);
    }

    public List<StudentDTO> getAllStudents() {
        return studentRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public StudentDTO getStudentById(Long id) {
        return studentRepository.findById(id)
                .map(this::convertToDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + id));
    }

    public StudentDTO createStudent(StudentDTO studentDTO) {
        if (studentRepository.existsByEmail(studentDTO.getEmail())) {
            throw new DuplicateResourceException("Email already exists: " + studentDTO.getEmail());
        }

        Student student = convertToEntity(studentDTO);
        Student savedStudent = studentRepository.save(student);
        return convertToDTO(savedStudent);
    }

    public StudentDTO updateStudent(Long id, StudentDTO studentDTO) {
        Student existingStudent = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + id));

        if (!existingStudent.getEmail().equals(studentDTO.getEmail()) &&
                studentRepository.existsByEmail(studentDTO.getEmail())) {
            throw new DuplicateResourceException("Email already exists: " + studentDTO.getEmail());
        }

        existingStudent.setFirstName(studentDTO.getFirstName());
        existingStudent.setLastName(studentDTO.getLastName());
        existingStudent.setEmail(studentDTO.getEmail());
        existingStudent.setDateOfBirth(studentDTO.getDateOfBirth());

        Student updatedStudent = studentRepository.save(existingStudent);
        return convertToDTO(updatedStudent);
    }

    public void deleteStudent(Long id) {
        if (!studentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Student not found with id: " + id);
        }
        studentRepository.deleteById(id);
    }

    public StudentDTO enrollInCourse(Long studentId, Long courseId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));
        
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseId));

        student.getCourses().add(course);
        Student updatedStudent = studentRepository.save(student);
        return convertToDTO(updatedStudent);
    }

    public Page<StudentDTO> searchAndSortStudents(String searchTerm, String sortBy, String sortDirection, int page, int size) {
        Sort.Direction direction = Sort.Direction.fromString(sortDirection.toUpperCase());
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<Student> students;
        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            students = studentRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
                searchTerm, searchTerm, pageable);
        } else {
            students = studentRepository.findAll(pageable);
        }
        
        return students.map(this::convertToDTO);
    }

    public CompletableFuture<StudentDTO> createStudentAsync(StudentDTO studentDTO) {
        return CompletableFuture.supplyAsync(() -> {
            if (studentRepository.existsByEmail(studentDTO.getEmail())) {
                throw new DuplicateResourceException("Email already exists: " + studentDTO.getEmail());
            }

            Student student = convertToEntity(studentDTO);
            Student savedStudent = studentRepository.save(student);
            return convertToDTO(savedStudent);
        }, executorService);
    }

    public List<StudentDTO> createStudentsBatch(List<StudentDTO> studentDTOs) {
        List<CompletableFuture<StudentDTO>> futures = studentDTOs.stream()
            .map(this::createStudentAsync)
            .collect(Collectors.toList());

        return futures.stream()
            .map(CompletableFuture::join)
            .collect(Collectors.toList());
    }

    private StudentDTO convertToDTO(Student student) {
        StudentDTO dto = new StudentDTO();
        dto.setId(student.getId());
        dto.setFirstName(student.getFirstName());
        dto.setLastName(student.getLastName());
        dto.setEmail(student.getEmail());
        dto.setDateOfBirth(student.getDateOfBirth());
        
        if (student.getCourses() != null) {
            dto.setCourses(student.getCourses().stream()
                    .map(course -> {
                        CourseDTO courseDTO = new CourseDTO();
                        courseDTO.setId(course.getId());
                        courseDTO.setCourseName(course.getCourseName());
                        courseDTO.setCourseCode(course.getCourseCode());
                        courseDTO.setCredits(course.getCredits());
                        return courseDTO;
                    })
                    .collect(Collectors.toSet()));
        }
        
        return dto;
    }

    private Student convertToEntity(StudentDTO dto) {
        Student student = new Student();
        student.setId(dto.getId());
        student.setFirstName(dto.getFirstName());
        student.setLastName(dto.getLastName());
        student.setEmail(dto.getEmail());
        student.setDateOfBirth(dto.getDateOfBirth());
        return student;
    }
} 