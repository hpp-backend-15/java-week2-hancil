package io.hhplus.clean.repository;

import io.hhplus.clean.domain.entity.Applicant;
import io.hhplus.clean.domain.entity.Lecture;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ApplicantRepository extends JpaRepository<Applicant, Long> {

    boolean existsByEmailAndLecture(String email, Lecture lecture);

    List<Applicant> findByEmail(String emailId);
}
