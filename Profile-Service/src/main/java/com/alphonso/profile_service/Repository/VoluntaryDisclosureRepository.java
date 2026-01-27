package com.alphonso.profile_service.Repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.alphonso.profile_service.Entity.VoluntaryDisclosure;

@Repository
public interface VoluntaryDisclosureRepository extends JpaRepository<VoluntaryDisclosure, Long> {
    Optional<VoluntaryDisclosure> findById(Long profile);
    Optional<VoluntaryDisclosure> findByProfile_Id(Long profileId);
}