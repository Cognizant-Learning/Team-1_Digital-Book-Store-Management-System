package com.libraryManagement.project.controller;

import com.libraryManagement.project.dto.AuthenticationRequestDTO;
import com.libraryManagement.project.dto.AuthenticationResponseDTO;
import com.libraryManagement.project.model.User;
import com.libraryManagement.project.service.AuthenticationService;
import com.libraryManagement.project.service.MyUserDetailsService;
import com.libraryManagement.project.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {

	@Autowired
	private MyUserDetailsService userDetailsService;

	@Autowired
	private AuthenticationService authService;

	@Autowired
	private AuthenticationManager authenticationManager;

	@Autowired
	private JwtUtil jwtUtil;

	// Endpoint to register a new user
	@PostMapping("/saveUser")
	public User register(@RequestBody User user) {
		return authService.save(user);
	}

	// Endpoint to authenticate and generate JWT token
	@PostMapping("/login")
	public ResponseEntity<?> login(@RequestBody AuthenticationRequestDTO request) {
		try {
			// Authenticate using email and password
			Authentication authentication = authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
			);

			// Get authenticated user details
			UserDetails userDetails = (UserDetails) authentication.getPrincipal();
			String email = userDetails.getUsername(); // getUsername() returns email
			String token = jwtUtil.generateToken(email); // Generate JWT using email

			// Optionally extract role (if needed for response or logging)
			String role = userDetails.getAuthorities().stream()
					.findFirst()
					.map(GrantedAuthority::getAuthority)
					.orElse(null);

			// Return token in response
			return ResponseEntity.ok(new AuthenticationResponseDTO(token));

		} catch (BadCredentialsException ex) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body("Invalid email or password");
		} catch (AuthenticationException ex) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body("Authentication failed: " + ex.getMessage());
		}
	}
}
