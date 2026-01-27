package com.alphonso.profile_service.Service;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.alphonso.profile_service.Entity.Education;
import com.alphonso.profile_service.Entity.Experience;
import com.alphonso.profile_service.Entity.ProfileDetails;
import com.alphonso.profile_service.Entity.ProfileSkills;
import com.alphonso.profile_service.RequestDTO.EducationRequest;
import com.alphonso.profile_service.RequestDTO.ExperienceRequest;
import com.alphonso.profile_service.RequestDTO.SkillSelectionRequest;
import com.alphonso.profile_service.ResponseDTO.ProfileDTO;

@Mapper(componentModel = "spring")
public interface IUserMapper {

    @Mapping(target = "education", source = "education")
    @Mapping(target = "experiences", source = "experiences")
    @Mapping(target = "skills", source = "skills")
    
    ProfileDetails toEntity(ProfileDTO dto);
    Education toEntity(EducationRequest dto);
    Experience toEntity(ExperienceRequest dto);
    ProfileSkills toEntity(SkillSelectionRequest dto);

}
