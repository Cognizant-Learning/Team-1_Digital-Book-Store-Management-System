package com.libraryManagement.project;


import com.libraryManagement.project.util.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

	@Autowired
	private JwtUtil jwtUtil;

	@Autowired
	private UserDetailsService userDetailsService;

	@Override
	protected void doFilterInternal(HttpServletRequest request,
									HttpServletResponse response,
									FilterChain filterChain)
			throws ServletException, IOException {

		final String authHeader = request.getHeader("Authorization");

		String email = null;
		String token = null;

		if (authHeader != null && authHeader.startsWith("Bearer ")) {
			token = authHeader.substring(7);
			try {
				email = jwtUtil.extractEmail(token);
			} catch (ExpiredJwtException e) {
				System.out.println("Token expired: " + e.getMessage());
			} catch (Exception e) {
				System.out.println("Token invalid: " + e.getMessage());
			}
		}

		if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
			UserDetails userDetails = userDetailsService.loadUserByUsername(email); // still required by Spring

			if (jwtUtil.validateToken(token, userDetails.getUsername())) {
				UsernamePasswordAuthenticationToken authToken =
						new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

				authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
				SecurityContextHolder.getContext().setAuthentication(authToken);
			}
		}

		filterChain.doFilter(request, response);
	}
}

	


	/*@Autowired

	private JwtUtil1 jwtUtil;

	@Autowired

	private UserDetailsService userDetailsService;

	@Override

	protected void doFilterInternal(HttpServletRequest request,

			HttpServletResponse response,

			FilterChain filterChain) throws ServletException, IOException {

		String authHeader = request.getHeader("Authorization");

		if (authHeader != null && authHeader.startsWith("Bearer ")) {

			String token = authHeader.substring(7);//token

			String username = jwtUtil.extractUsername(token);

			if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

				UserDetails userDetails = 
						userDetailsService.loadUserByUsername(username);

				if (jwtUtil.validateToken(token)) {
					String role = jwtUtil.extractRole(token);
					List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(role));

					
					  List<GrantedAuthority> authorities = jwtUtil.extractRoles(token).stream()
					  
					  .map(SimpleGrantedAuthority::new)
					  
					  .collect(Collectors.toList());
					 

					UsernamePasswordAuthenticationToken authToken =

							new UsernamePasswordAuthenticationToken(userDetails, null, authorities);

					SecurityContextHolder.getContext().setAuthentication(authToken);

				}

			}

		}

		filterChain.doFilter(request, response);

	}*/


