package com.libraryManagement.project.service;

import com.libraryManagement.project.entity.User;
import com.libraryManagement.project.enums.Role;
import com.libraryManagement.project.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    private void validateAdmin(Long adminId) {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin not found"));
        if (admin.getRole() != Role.ADMIN) {
            throw new SecurityException("Access denied: Admin privileges required.");
        }
    }

    public User createUserAsAdmin(User user, Long adminId) {
        validateAdmin(adminId);
        return userRepository.save(user);
    }

    public List<User> getAllUsersAsAdmin(Long adminId) {
        validateAdmin(adminId);
        return userRepository.findAll();
    }

    public User getUserByIdAsAdmin(Long id, Long adminId) {
        validateAdmin(adminId);
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public User updateUserAsAdmin(Long id, User updatedUser, Long adminId) {
        validateAdmin(adminId);
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        existingUser.setEmail(updatedUser.getEmail());
        existingUser.setFullName(updatedUser.getFullName());
        existingUser.setPassword(updatedUser.getPassword());
        existingUser.setRole(updatedUser.getRole());

        return userRepository.save(existingUser);
    }

    public void deleteUserAsAdmin(Long id, Long adminId) {
        validateAdmin(adminId);
        userRepository.deleteById(id);
    }
}
