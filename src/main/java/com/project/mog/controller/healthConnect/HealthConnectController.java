package com.project.mog.controller.healthConnect;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.mog.security.jwt.JwtUtil;
import com.project.mog.service.healthConnect.HealthConnectDto;
import com.project.mog.service.healthConnect.HealthConnectResponseDto;
import com.project.mog.service.healthConnect.HealthConnectService;

import io.swagger.v3.oas.annotations.parameters.RequestBody;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/health-connect")
public class HealthConnectController {
	
	private final JwtUtil jwtUtil;
	private final HealthConnectService healthConnectService;
	
	@PostMapping("/")
	public ResponseEntity<HealthConnectResponseDto> receiveHealthConnect(
			@RequestHeader("Authorization") String authHeader,
			@RequestBody HealthConnectDto heDto){
		String token = authHeader.replace("Bearer ", "");
		String eamil = jwtUtil.extractUserEmail(token);
		
		HealthConnectResponseDto response = healthConnectService.saveHealthConnect(heDto, eamil);
		return ResponseEntity.ok(response);
	}

}
