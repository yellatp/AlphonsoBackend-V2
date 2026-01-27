package com.alphonso.moodle_employer_service.Moodle.Repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import com.alphonso.moodle_employer_service.Moodle.Entity.SkillQbankCategoryMap;

@Repository
public interface SkillQbankCategoryMapRepository extends JpaRepository<SkillQbankCategoryMap, Long> {

	@Query("SELECT s FROM SkillQbankCategoryMap s WHERE UPPER(s.skillName) IN :skillName")
    List<SkillQbankCategoryMap> findBySkillNameIgnoreCase(List<String> skillName);
    
    @Query("select s from SkillQbankCategoryMap s where upper(s.skillName) = upper(?1)")
    Optional<SkillQbankCategoryMap> findBySkillNameIgnoreCase(String skillName);
}