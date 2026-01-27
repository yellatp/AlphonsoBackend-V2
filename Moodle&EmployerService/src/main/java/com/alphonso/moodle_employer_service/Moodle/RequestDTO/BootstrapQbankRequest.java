package com.alphonso.moodle_employer_service.Moodle.RequestDTO;

import java.util.List;
import lombok.Data;

@Data
public class BootstrapQbankRequest {
	
   private String hostCourseShortname;
   private List<String> skills;
}