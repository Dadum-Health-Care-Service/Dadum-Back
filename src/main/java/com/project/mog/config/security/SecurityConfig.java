package com.project.mog.config.security;

import com.project.mog.filter.JwtAuthenticationFilter;
import com.project.mog.security.UsersDetailService;
import com.project.mog.security.jwt.JwtUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	private final JwtUtil jwtUtil;
	private final UsersDetailService usersDetailService;
	
	public SecurityConfig(JwtUtil jwtUtil, UsersDetailService usersDetailsService) {
		this.jwtUtil = jwtUtil;
		this.usersDetailService= usersDetailsService;
	}
    
	
	@Bean
	protected SecurityFilterChain filterChain(HttpSecurity http) throws Exception{
		System.out.println("called?");
		SecurityFilterChain result = http
				.cors(Customizer.withDefaults())
				.csrf(csrf -> csrf.disable())
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(auth -> auth.requestMatchers(
						"/api/v1/users/list",
						"/api/v1/users/login",
						"/api/v1/users/auth/passwordless/login",
						"/api/v1/users/login/*",
						"/api/v1/users/signup",
						"/api/v1/routine/**",
						"/api/v1/users/**",
						"/api/v1/users/email/**",
						"/api/v1/users/send/*",
						"/api/v1/posts/list",
						"/api/v1/payments/**",
						"/api/v1/health/**",
						"/api/v1/shop/**",
						"/api/chat/**",  // 챗봇 API 허용
						"/api/v1/seller/products/public/**",  // Shop용 public 상품 API 허용
						"/api/v1/ai/**",  // AI API 허용
						"/ws/**",  // WebSocket 허용
						"/swagger-ui/*",
						"/swagger-resources/**",
						"/v3/api-docs/**",
						"/error"
						
						).permitAll()
				.anyRequest().authenticated())
				.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
				.exceptionHandling(	
							ex-> ex.authenticationEntryPoint((req,res,ex2)->{
								res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
								res.setContentType("text/plain;charset=UTF-8");
								if(ex2 instanceof UsernameNotFoundException) {
									res.getWriter().write("[401 Unauthorized] 인증되지 않음 : 테스트테스트");
								}
								else {
									res.getWriter().write("[401 Unauthorized] 인증되지 않음 : 인증이 필요합니다");
									
								}
								
								
							})
							.accessDeniedHandler((req,res,ex2)->{
								res.setStatus(HttpServletResponse.SC_FORBIDDEN);
								res.setContentType("text/plain;charset=UTF-8");
								res.getWriter().write("[403 Forbidden] 접근이 거부되었습니다 : 권한이 없습니다");
							})
							
			        )
				.build();
		
		System.out.println("RESULT:"+result);
		return result;
	}
	
	@Bean
	public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
		AuthenticationManagerBuilder authBuilder =http.getSharedObject(AuthenticationManagerBuilder.class);
		
		authBuilder.userDetailsService(usersDetailService)
					.passwordEncoder(getPasswordEncoder());
		return authBuilder.build();
	}
	
	@Bean
    protected JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtUtil, usersDetailService);
    }
	

    @Bean
    protected BCryptPasswordEncoder getPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        
        // 허용할 Origin 설정
        config.setAllowedOrigins(List.of(
            "http://localhost:3000",  // 기존 React 개발 서버
            "http://localhost:5173",  // Vite 개발 서버 (챗봇용)
            "http://localhost:5174"   // Vite 대체 포트 (챗봇용)
        ));
        
        // 허용할 HTTP 메서드 설정
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        
        // 허용할 헤더 설정
        config.setAllowedHeaders(List.of("*"));
        
        // 쿠키 및 인증 정보 허용
        config.setAllowCredentials(true);
        
        // Preflight 요청 캐시 시간 (초)
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

}
