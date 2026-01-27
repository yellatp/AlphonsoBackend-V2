package com.alphonso.Interviewer_Service.ServiceImp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import com.alphonso.Interviewer_Service.Entity.Interview;
import com.alphonso.Interviewer_Service.Repository.InterviewRepository;

@Service
public class InterviewEventUpdateService {

    @Autowired
    private InterviewRepository interviewRepo;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateEvent(Long id, String meetUrl) {
    	 Interview interview = interviewRepo.findById(id).orElseThrow();
    	    interview.setMeetUrl(meetUrl);
    	    interviewRepo.save(interview);
    }
}
