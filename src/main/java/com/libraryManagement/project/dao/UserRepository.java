package com.libraryManagement.project.dao;


import com.libraryManagement.project.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {

	Optional<User> findByEmail(String email);


}
