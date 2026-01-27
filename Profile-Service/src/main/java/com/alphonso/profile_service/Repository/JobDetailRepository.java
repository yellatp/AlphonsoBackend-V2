package com.alphonso.profile_service.Repository;

import com.alphonso.profile_service.Entity.JobDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JobDetailRepository extends JpaRepository<JobDetail, Long> {}
