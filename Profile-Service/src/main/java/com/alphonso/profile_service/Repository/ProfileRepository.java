package com.alphonso.profile_service.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.alphonso.profile_service.Entity.ProfileDetails;
import com.alphonso.profile_service.Entity.ProfileDetails.AssessmentStatus;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProfileRepository extends JpaRepository<ProfileDetails, Long> {
    Optional<ProfileDetails> findByEmail(String email);
    Optional<ProfileDetails> findByUserId(Long userId);
    Optional<ProfileDetails> findByProfileId(String profileId);
    List<ProfileDetails> findByAssessmentStatus(AssessmentStatus status);
    
    @Query("""
            SELECT 
                p.profileId      AS profileId,
                p.email          AS email,
                p.firstName      AS firstName,
                p.lastName       AS lastName,
                r.roleName       AS domainName
            FROM ProfileDetails p
            JOIN p.profileSkills ps
            JOIN ps.role r
            WHERE p.assessmentStatus = :status
        """)
        List<CandidateDetailsForInterview> findByAssessmentStatusForInterview(
                @Param("status") ProfileDetails.AssessmentStatus status
        );

        interface CandidateDetailsForInterview {
            String getProfileId();
            String getEmail();
            String getFirstName();
            String getLastName();
            String getDomainName();
        }
    
    Optional<ProfileProjection> findProjectedByEmail(String email);
    interface ProfileProjection {
        String getProfileId();
        String getEmail();
        String getFirstName();
        String getLastName();
    }
    
    @Query("""
            SELECT p FROM ProfileDetails p
            LEFT JOIN FETCH p.profileSkills ps
            LEFT JOIN FETCH ps.role
            LEFT JOIN FETCH ps.coreSkills
            LEFT JOIN FETCH p.applicationQuestion aq
            LEFT JOIN FETCH p.experiences exp
            LEFT JOIN FETCH exp.jobDetails jd
            WHERE p.profileId = :profileId
        """)
        Optional<ProfileDetails> fetchLeftPanelData(@Param("profileId") String profileId);
    
//    @Query("""
//            SELECT DISTINCT p
//            FROM ProfileDetails p
//            JOIN p.profileSkills ps
//            JOIN ps.role r
//            LEFT JOIN FETCH ps.coreSkills cs
//            WHERE r.roleName = :jobRole
//              AND p.assessmentStatus = :status
//              AND p.status = com.alphonso.profile_service.Entity.ProfileDetails.Status.SUBMITTED
//        """)
//        List<ProfileDetails> findCandidatesByJobRoleAndAssessmentStatus(
//                @Param("jobRole") String jobRole,
//                @Param("status") ProfileDetails.AssessmentStatus status
//        );
    
    @Query("""
    	    SELECT DISTINCT p
    	    FROM ProfileDetails p
    	    JOIN FETCH p.profileSkills ps
    	    JOIN ps.role r
    	    LEFT JOIN FETCH ps.coreSkills cs
    	    WHERE r.roleName = :jobRole
    	      AND p.assessmentStatus = :status
    	      AND p.status = com.alphonso.profile_service.Entity.ProfileDetails.Status.SUBMITTED
    	""")
    	List<ProfileDetails> findCandidatesByJobRoleAndAssessmentStatus(
    	        @Param("jobRole") String jobRole,
    	        @Param("status") ProfileDetails.AssessmentStatus status
    	);

}

