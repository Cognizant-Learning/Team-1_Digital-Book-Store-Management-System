package com.libraryManagement.project.repository;
import com.libraryManagement.project.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;


public interface UserRepository extends JpaRepository<User, Long>
{

    User findByEmail(String email);
}