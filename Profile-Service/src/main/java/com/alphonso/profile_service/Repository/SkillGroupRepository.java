package com.alphonso.profile_service.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.alphonso.profile_service.Entity.SkillGroup;

@Repository
public interface SkillGroupRepository extends JpaRepository<SkillGroup, Long> {}
