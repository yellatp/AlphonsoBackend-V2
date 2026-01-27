package com.alphonso.Interviewer_Service.ServiceImp;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JitsiMeetService {

    @Value("${jit.base-url}")
    private String baseUrl;

    public String generateMeetUrl(String interviewCode) {
        return baseUrl + "/" + interviewCode;
    }
}
