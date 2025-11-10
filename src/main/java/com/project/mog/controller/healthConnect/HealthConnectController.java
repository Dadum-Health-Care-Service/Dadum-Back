package com.project.mog.controller.healthConnect;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.mog.security.jwt.JwtUtil;
import com.project.mog.service.healthConnect.HealthConnectDto;
import com.project.mog.service.healthConnect.HealthConnectResponseDto;
import com.project.mog.service.healthConnect.HealthConnectService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.web.bind.annotation.RequestBody;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/health")
@Tag(name = "헬스커넥트 관리", description = "모바일/워치 헬스커넥트 데이터 관리 API")
public class HealthConnectController {
	
	private final JwtUtil jwtUtil;
	private final HealthConnectService healthConnectService;

	
	@PostMapping("")
	@Operation(summary = "헬스커넥트 데이터 수신", description = "헬스커넥트 데이터를 수신합니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "헬스커넥트 데이터 수신 성공"),
		@ApiResponse(responseCode = "401", description = "인증 실패")
	})
	public ResponseEntity<HealthConnectResponseDto> receiveHealthConnect(
			@RequestHeader("Authorization") String authHeader,
			@RequestBody HealthConnectDto heDto){
		String token = authHeader.replace("Bearer ", "");
		String eamil = jwtUtil.extractUserEmail(token);
		
		HealthConnectResponseDto response = healthConnectService.saveHealthConnect(heDto, eamil);
		return ResponseEntity.ok(response);
	}
	
	@GetMapping("/{usersId}")
	@Operation(summary = "헬스커넥트 데이터 조회", description = "헬스커넥트 데이터를 조회합니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "헬스커넥트 데이터 조회 성공"),
		@ApiResponse(responseCode = "401", description = "인증 실패")
	})
	public ResponseEntity<List<HealthConnectDto>> getHealthConnectDataByUsersId(@PathVariable Long usersId){
		List<HealthConnectDto> healthDataList=healthConnectService.getHealthConnectData(usersId);
		return ResponseEntity.ok(healthDataList);
	}
	
	@DeleteMapping("/{usersId}")
	@Operation(summary = "헬스커넥트 전체 데이터 삭제", description = "헬스커넥트 전체 데이터를 삭제합니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "헬스커넥트 데이터 삭제 성공"),
		@ApiResponse(responseCode = "401", description = "인증 실패")
	})
	public ResponseEntity<HealthConnectResponseDto> deleteHealthConnectData(@PathVariable Long usersId){
		healthConnectService.deleteHealthConnectDataByUsersId(usersId);
		return ResponseEntity.ok(new HealthConnectResponseDto("해당유저의 모든 헬스커넥트 데이터가 성공적으로 삭제되었습니다"));
	}
	
	@DeleteMapping("/{usersId}/health/{healthId}")
	@Operation(summary = "헬스커넥트 단일 데이터 삭제", description = "헬스커넥트 단일 데이터를 삭제합니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "헬스커넥트 데이터 삭제 성공"),
		@ApiResponse(responseCode = "401", description = "인증 실패")
	})
	public ResponseEntity<HealthConnectResponseDto> deleteSingleHealthConnectData(@PathVariable Long usersId, @PathVariable Long healthId){
		healthConnectService.deleteHealthConnectDataByUsersIdAndHealthId(usersId,healthId);
		return ResponseEntity.ok(new HealthConnectResponseDto("해당유저의 헬스커넥트 데이터가 성공적으로 삭제되었습니다"));
	}

}
