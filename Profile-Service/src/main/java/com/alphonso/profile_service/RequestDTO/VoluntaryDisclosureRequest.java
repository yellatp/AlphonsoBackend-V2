package com.alphonso.profile_service.RequestDTO;

import com.alphonso.profile_service.Entity.VoluntaryDisclosure.Gender;
import com.alphonso.profile_service.Entity.VoluntaryDisclosure.Race;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class VoluntaryDisclosureRequest {

    @NotNull(message = "Gender selection is required")
    private Gender gender;

    @NotNull(message = "Race selection is required")
    private Race race;
}