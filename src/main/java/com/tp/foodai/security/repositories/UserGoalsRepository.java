package com.tp.foodai.security.repositories;

import com.tp.foodai.security.entities.UserGoals;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserGoalsRepository extends JpaRepository<UserGoals, Long> {

    Optional<UserGoals> findByFirebaseUid(String firebaseUid);
}
