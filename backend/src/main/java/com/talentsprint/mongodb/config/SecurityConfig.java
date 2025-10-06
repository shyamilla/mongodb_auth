    package com.talentsprint.mongodb.config;

    import com.talentsprint.mongodb.repository.UserRepository;
    import jakarta.servlet.FilterChain;
    import jakarta.servlet.ServletException;
    import jakarta.servlet.http.HttpServletRequest;
    import jakarta.servlet.http.HttpServletResponse;
    import org.springframework.context.annotation.Bean;
    import org.springframework.context.annotation.Configuration;
    import org.springframework.security.authentication.AuthenticationManager;
    import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
    import org.springframework.security.config.annotation.web.builders.HttpSecurity;
    import org.springframework.security.config.http.SessionCreationPolicy;
    import org.springframework.security.core.userdetails.User;
    import org.springframework.security.core.userdetails.UserDetails;
    import org.springframework.security.core.userdetails.UserDetailsService;
    import org.springframework.security.core.userdetails.UsernameNotFoundException;
    import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
    import org.springframework.security.crypto.password.PasswordEncoder;
    import org.springframework.security.web.SecurityFilterChain;
    import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
    import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
    import org.springframework.web.cors.CorsConfiguration;
    import org.springframework.web.cors.CorsConfigurationSource;
    import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
    import org.springframework.web.filter.OncePerRequestFilter;

    import java.io.IOException;
    import java.util.List;

    @Configuration
    public class SecurityConfig {

        private final UserRepository userRepository;
        private final JwtUtil jwtUtil;

        public SecurityConfig(UserRepository userRepository, JwtUtil jwtUtil) {
            this.userRepository = userRepository;
            this.jwtUtil = jwtUtil;
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
            return new BCryptPasswordEncoder();
        }

        @Bean
        public UserDetailsService userDetailsService() {
            return username -> userRepository.findByUsername(username)
                    .map(user -> User.withUsername(user.getUsername())
                            .password(user.getPassword())
                            .roles("USER")
                            .build())
                    .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        }

        @Bean
        public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
            return config.getAuthenticationManager();
        }

        @Bean
        public OncePerRequestFilter jwtAuthFilter(UserDetailsService userDetailsService) {
            return new OncePerRequestFilter() {
                @Override
                protected void doFilterInternal(HttpServletRequest request,
                                                HttpServletResponse response,
                                                FilterChain filterChain) throws ServletException, IOException {
                    final String authHeader = request.getHeader("Authorization");
                    String username = null;
                    String token = null;

                    if (authHeader != null && authHeader.startsWith("Bearer ")) {
                        token = authHeader.substring(7);
                        System.out.println("Raw JWT: " + token);

                        try {
                            username = jwtUtil.extractEmail(token);
                            System.out.println("JWT received for user: " + username);
                            // the above line is working fine, can see the username
                            // but this is not moving to or givin permissino to get to welcome endpoint
                        } catch (Exception e) {
                            System.out.println("Invalid JWT: " + e.getMessage());
                        }
                    }

                    if (username != null) {
                        try {
                            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                            if (jwtUtil.validateToken(token)) {
                                var authToken = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                                        userDetails, null, userDetails.getAuthorities());
                                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                                org.springframework.security.core.context.SecurityContextHolder.getContext()
                                        .setAuthentication(authToken);
                            }
                        } catch (UsernameNotFoundException e) {
                            System.out.println("User not found during JWT validation: " + username);
                        }
                    }

                    // what does this do?

                    filterChain.doFilter(request, response);

                    // do souts to see the flow of request and filter invocations
                    // for now also allow request to pass through /welcome endpoint regardless of jwt validation
                    
                }
            };
        }

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http, OncePerRequestFilter jwtAuthFilter)
                throws Exception {
            http
                    .csrf(csrf -> csrf.disable())
                    .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                    .authorizeHttpRequests(auth -> auth
                            .requestMatchers("/auth/register", "/auth/login", "/auth/forgot", "/auth/reset")
                            .permitAll()
                            .anyRequest().authenticated())
                    .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                    .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

            return http.build();
        }

        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
            CorsConfiguration config = new CorsConfiguration();
            config.setAllowedOrigins(List.of("http://localhost:4200", "http://127.0.0.1:4200"));
            config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
            config.setAllowedHeaders(List.of("*"));
            config.setAllowCredentials(true);

            UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
            source.registerCorsConfiguration("/**", config);
            return source;
        }
    }
