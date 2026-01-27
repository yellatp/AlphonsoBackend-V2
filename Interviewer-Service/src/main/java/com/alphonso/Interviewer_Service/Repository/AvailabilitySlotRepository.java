package com.alphonso.Interviewer_Service.Repository;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import com.alphonso.Interviewer_Service.Entity.AvailabilitySlot;
import com.alphonso.Interviewer_Service.Entity.InterviewerDetails;
import com.alphonso.Interviewer_Service.ResponseDTO.AvailabilitySlotResponse;
import feign.Param;

public interface AvailabilitySlotRepository extends JpaRepository<AvailabilitySlot, Long> {

	@Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END FROM AvailabilitySlot a "
			+ "WHERE a.interviewer.id = :interviewerId AND a.status = 'FREE' "
			+ "AND a.start < :newEnd AND a.end > :newStart")
	boolean existsOverlap(@Param("interviewerId") Long interviewerId, @Param("newStart") LocalDateTime newStart,
			@Param("newEnd") LocalDateTime newEnd);
//
//	List<AvailabilitySlot> findByInterviewerAndStartBetween(InterviewerDetails interviewer, LocalDateTime start,
//			LocalDateTime end);
	
	 @Query("""
		        SELECT new com.alphonso.Interviewer_Service.ResponseDTO.AvailabilitySlotResponse(
		            a.id,
		            a.start,
		            a.end,
		            a.status,
		            a.interviewer.id,
		            a.interviewer.email,
		            a.interviewer.name
		        )
		        FROM AvailabilitySlot a
		        WHERE a.interviewer = :interviewer
		          AND a.start >= :start
		          AND a.start < :end
		    """)
		    List<AvailabilitySlotResponse> findAvailabilityResponseForMonth(
		            @Param("interviewer") InterviewerDetails interviewer,
		            @Param("start") LocalDateTime start,
		            @Param("end") LocalDateTime end
		    );

	@Query("""
			    SELECT s FROM AvailabilitySlot s
			    JOIN s.interviewer i
			    JOIN i.skills sk
			    WHERE sk.name = :domain
			    AND s.status = 'FREE'
			    AND s.start >= :now
			    ORDER BY s.start ASC
			""")
	List<AvailabilitySlot> findFreeSlotsByDomainOrdered(@Param("domain") String domain,
			@Param("now") LocalDateTime now);

	@Query("SELECT s FROM AvailabilitySlot s " + "WHERE s.interviewer.id = :interviewerId " + "AND s.status = 'FREE' "
			+ "AND s.start < :end " + "AND s.end > :start")
	List<AvailabilitySlot> findOverlappingFreeSlotsForInterviewer(@Param("interviewerId") Long interviewerId,
			@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

}
