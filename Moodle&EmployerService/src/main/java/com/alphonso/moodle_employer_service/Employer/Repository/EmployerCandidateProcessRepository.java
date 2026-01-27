package com.alphonso.moodle_employer_service.Employer.Repository;

import com.alphonso.moodle_employer_service.Employer.Entity.EmployerCandidateProcess;
import com.alphonso.moodle_employer_service.Employer.Entity.EmployerCandidateProcess.Stage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface EmployerCandidateProcessRepository extends JpaRepository<EmployerCandidateProcess, Long> {

    Optional<EmployerCandidateProcess> findByEmployer_IdAndRequisition_IdAndProfileId(
            Long employerId, Long requisitionId, String profileId
    );

    List<EmployerCandidateProcess> findByEmployer_IdAndRequisition_Id(Long employerId, Long requisitionId);

    List<EmployerCandidateProcess> findByProfileIdAndStage(String profileId, Stage stage);

    List<EmployerCandidateProcess> findByProfileId(String profileId);

    List<EmployerCandidateProcess> findByEmployer_IdAndRequisition_IdAndStage(
            Long employerId, Long requisitionId, Stage stage
    );
}
