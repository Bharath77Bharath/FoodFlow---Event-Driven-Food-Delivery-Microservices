package com.foodflow.userservice.Repository;

import com.foodflow.userservice.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepo extends JpaRepository<User, Long> {

    boolean existsByEmail(String email);

}
