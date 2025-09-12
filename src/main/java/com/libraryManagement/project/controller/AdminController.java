package com.libraryManagement.project.controller;

import com.libraryManagement.project.entity.User;
import com.libraryManagement.project.enums.Role;
import com.libraryManagement.project.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/users")
public class AdminController {

    @Autowired
    private UserService userService;

    // Create a new user (admin only)
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user, @RequestHeader("adminId") Long adminId) {
        user.setRole(Role.CUSTOMER); // Default role
        return ResponseEntity.ok(userService.createUserAsAdmin(user, adminId));
    }

    // Get all users (admin only)
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers(@RequestHeader("adminId") Long adminId) {
        return ResponseEntity.ok(userService.getAllUsersAsAdmin(adminId));
    }

    // Get user by ID (admin only)
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id, @RequestHeader("adminId") Long adminId) {
        return ResponseEntity.ok(userService.getUserByIdAsAdmin(id, adminId));
    }

    // Update user (admin only)
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User updatedUser, @RequestHeader("adminId") Long adminId) {
        return ResponseEntity.ok(userService.updateUserAsAdmin(id, updatedUser, adminId));
    }

    // Delete user (admin only)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id, @RequestHeader("adminId") Long adminId) {
        userService.deleteUserAsAdmin(id, adminId);
        return ResponseEntity.noContent().build();
    }
}
