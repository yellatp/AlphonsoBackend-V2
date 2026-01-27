package com.alphonso.moodle_employer_service.Moodle.RequestDTO;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MoodleRequest {
    private String profileId;
    private String firstName;
    private String lastName;
    private String email;
    private List<String> skills;
}
