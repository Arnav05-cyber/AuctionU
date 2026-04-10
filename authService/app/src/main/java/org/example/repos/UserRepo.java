package org.example.repos;

import org.example.entities.UserInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepo extends JpaRepository<UserInfo, String> {
    Optional<UserInfo> findByUserName(String userName);
    Boolean existsByUserName(String userName);
    Boolean existsByEmail(String email);
}
