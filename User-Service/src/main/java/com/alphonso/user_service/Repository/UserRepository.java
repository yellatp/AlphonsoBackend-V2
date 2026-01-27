package com.alphonso.user_service.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.alphonso.user_service.Model.UsersReg;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UsersReg, Long> {
    Optional<UsersReg> findByEmail(String email);
}
