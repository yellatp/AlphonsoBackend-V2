package com.alphonso.profile_service.ResponseDTO;

import com.alphonso.profile_service.Entity.VoluntaryDisclosure.Gender;
import com.alphonso.profile_service.Entity.VoluntaryDisclosure.Race;
import lombok.Data;

@Data
public class VoluntaryDisclosureRequest {

    private Gender gender;

    private Race race;
}