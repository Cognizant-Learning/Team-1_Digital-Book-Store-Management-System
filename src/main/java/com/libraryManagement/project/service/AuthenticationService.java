package com.libraryManagement.project.service;


import com.libraryManagement.project.dao.UserRepository;
import com.libraryManagement.project.exception.ResourceNotFoundException;
import com.libraryManagement.project.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuthenticationService {

	@Autowired
	private UserRepository userRepo;

	@Autowired
	private PasswordEncoder passwordEncoder;

	// Save a new user with encoded password
	public User save(User user) {
		user.setPassword(passwordEncoder.encode(user.getPassword()));
		return userRepo.save(user);
	}

	// Retrieve all users
	public List<User> getAllUsers() {
		return userRepo.findAll();
	}

	// Update user details
	public User updateUser(Long id, User updatedUser) {
		User existingUser = userRepo.findById(String.valueOf(id))
				.orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));

		existingUser.setUsername(updatedUser.getUsername());
		existingUser.setEmail(updatedUser.getEmail());
		existingUser.setRole(updatedUser.getRole());

		if (updatedUser.getPassword() != null && !updatedUser.getPassword().isEmpty()) {
			existingUser.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
		}

		return userRepo.save(existingUser);
	}

	// Delete user by ID
	public void deleteUser(Long id) {
		userRepo.deleteById(String.valueOf(id));
	}
}
