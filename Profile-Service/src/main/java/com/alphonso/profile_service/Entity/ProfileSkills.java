package com.alphonso.profile_service.Entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "profile_skills")
public class ProfileSkills {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "group_id")
    private SkillGroup group;

    @ManyToOne
    @JoinColumn(name = "role_id")
    private SkillRole role;

    @ManyToMany
    @JoinTable(
        name = "profile_core_skills",
        joinColumns = @JoinColumn(name = "profile_skill_id"),
        inverseJoinColumns = @JoinColumn(name = "core_skill_id")
    )
    private List<CoreSkills> coreSkills;

    @ManyToMany
    @JoinTable(
        name = "profile_additional_skills",
        joinColumns = @JoinColumn(name = "profile_skill_id"),
        inverseJoinColumns = @JoinColumn(name = "additional_skill_id")
    )
    private List<AdditionalSkill> additionalSkills;
    
    @ManyToOne
    @JoinColumn(name = "programming_skill_id")
    private ProgrammingSkill programmingSkill;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id")
    private ProfileDetails profile;
}
