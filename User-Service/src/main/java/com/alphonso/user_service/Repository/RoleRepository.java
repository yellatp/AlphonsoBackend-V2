package com.alphonso.user_service.Repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.alphonso.user_service.Model.RoleCategory;

@Repository
public interface RoleRepository extends JpaRepository<RoleCategory, Long> {
    Optional<RoleCategory> findByCategoryName(String name);
}
