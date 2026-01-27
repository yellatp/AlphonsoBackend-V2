package com.alphonso.profile_service.Entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "Voluntary_Disclosure")
public class VoluntaryDisclosure {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id")
    private ProfileDetails profile;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    private Race race;
    
    public enum Gender {
        MALE,
        FEMALE,
        @JsonProperty("I Choose Not To Self-Identify")
        I_Choose_Not_To_Self_Identify
    }


    public enum Race {
    	@JsonProperty("Hispanic Or Latino")
        HISPANIC_OR_LATINO,
        WHITE,
        @JsonProperty("Black Or African American")
        BLACK_OR_AFRICAN_AMERICAN,
        ASIAN,
        @JsonProperty("Native Hawaiian Or Other Pacific Islander")
        NATIVE_HAWAIIAN_OR_PACIFIC_ISLANDER,
        @JsonProperty("American Indian Or Alaska Native")
        AMERICAN_INDIAN_OR_ALASKA_NATIVE,
        @JsonProperty("Two Or More Races")
        TWO_OR_MORE_RACES,
        @JsonProperty("Prefer Not To Answer")
        Prefer_Not_To_Answer
    }

}
