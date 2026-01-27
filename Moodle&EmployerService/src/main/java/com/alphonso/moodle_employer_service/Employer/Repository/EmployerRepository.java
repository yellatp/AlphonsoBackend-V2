package com.alphonso.moodle_employer_service.Employer.Repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.alphonso.moodle_employer_service.Employer.Entity.Employer;

public interface EmployerRepository extends JpaRepository<Employer, Long> {

    Optional<Employer> findByEmail(String email);
}