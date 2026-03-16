package com.example.backend.users.domain.repository;

import com.example.backend.users.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmailOrPesel(String email, String pesel);
    boolean existsByPesel(String pesel);
    boolean existsByEmail(String email);

    @Query("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.roles")
    List<User> findAllWithRoles();

    Optional<User> findByEmail(String email);
    Optional<User> findByPesel(String pesel);
}
