package edu.espe.springlab.service;

import edu.espe.springlab.domain.Student;
import edu.espe.springlab.dto.StudentRequestData;
import edu.espe.springlab.repository.StudentRepository;
import edu.espe.springlab.service.impl.StudentServiceImpl;
import edu.espe.springlab.web.advice.ConflictException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import({StudentServiceImpl.class})
public class StudentServiceTest {

    @Autowired
    private StudentServiceImpl service;

    @Autowired
    private StudentRepository repository;

    @Test
    void shouldNotAllowDuplicateEmail() {
        Student existing = new Student();
        existing.setFullName("Existing");
        existing.setEmail("duplicate@example.com");
        existing.setBirthDate(LocalDate.of(2000, 10, 10));
        existing.setActive(true);

        repository.save(existing);

        StudentRequestData req = new StudentRequestData();
        req.setFullName("New User Dup");
        req.setEmail("duplicate@example.com");
        req.setBirthDate(LocalDate.of(2000, 10, 10));

        assertThatThrownBy(() -> service.create(req))
                .isInstanceOf(ConflictException.class);
    }
}
