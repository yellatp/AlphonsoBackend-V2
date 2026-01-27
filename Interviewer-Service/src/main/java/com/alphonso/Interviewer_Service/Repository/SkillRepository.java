package com.alphonso.Interviewer_Service.Repository;

import java.util.List;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import com.alphonso.Interviewer_Service.Entity.SkillSetDetails;

public interface SkillRepository extends JpaRepository<SkillSetDetails, Long> {
	List<SkillSetDetails> findAllByIdIn(Set<Long> ids);
    List<SkillSetDetails> findAllByNameIn(List<String> names);
}