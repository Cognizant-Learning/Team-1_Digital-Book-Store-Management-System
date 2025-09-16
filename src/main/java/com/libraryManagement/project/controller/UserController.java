package com.libraryManagement.project.controller;


import com.libraryManagement.project.model.User;
import com.libraryManagement.project.service.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private AuthenticationService authService;

    //View profile (accessible by USER and ADMIN)
    @GetMapping("/profile")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public String userProfile() {
        return "User Profile";
    }

    //Create a new user (Admin only)
    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public User createUser(@RequestBody User user) {
        return authService.save(user);
    }

    //Get all users (Admin only)
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public List<User> getAllUsers() {
        return authService.getAllUsers();
    }

    //Update user (Admin or User)
    @PutMapping("/update/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public User updateUser(@PathVariable Long id, @RequestBody User updatedUser) {
        return authService.updateUser(id, updatedUser);
    }

    //Delete user (Admin only)
    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteUser(@PathVariable Long id) {
        authService.deleteUser(id);
        return "User deleted successfully";
    }
}
