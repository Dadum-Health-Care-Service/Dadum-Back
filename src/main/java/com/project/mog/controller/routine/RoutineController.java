package com.project.mog.controller.routine;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.mog.security.jwt.JwtUtil;
import com.project.mog.service.routine.RoutineDto;
import com.project.mog.service.routine.RoutineEndTotalDto;
import com.project.mog.service.routine.RoutineService;
import com.project.mog.service.routine.SaveRoutineDto;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/routine/")
@RequiredArgsConstructor
@Tag(name = "루틴 관리", description = "운동 루틴 관련 API")
public class RoutineController {
	
	private final JwtUtil jwtUtil;
	private final RoutineService routineService;
	
	//루틴 관련 api
	@GetMapping("list")
	@Operation(summary = "루틴 목록 조회", description = "사용자의 모든 루틴 목록을 조회합니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "조회 성공"),
		@ApiResponse(responseCode = "401", description = "인증 실패")
	})
	public ResponseEntity<List<RoutineDto>> getAllRoutines(@Parameter(hidden = true) @RequestHeader("Authorization") String authHeader){
		String token = authHeader.replace("Bearer ", "");
		String authEmail = jwtUtil.extractUserEmail(token);
		List<RoutineDto> routines = routineService.getAllRoutines(authEmail);
		return ResponseEntity.ok(routines);
	}

	// Start routine (DB-backed)
	@PostMapping("{routineId}/start")
	public ResponseEntity<RoutineService.RoutineStartResponse> startRoutine(
			@RequestHeader("Authorization") String authHeader,
			@PathVariable Long routineId) {
		String token = authHeader.replace("Bearer ", "");
		String authEmail = jwtUtil.extractUserEmail(token);
		RoutineService.RoutineStartResponse res = routineService.startRoutine(authEmail, routineId);
		return ResponseEntity.ok(res);
	}
	
	@GetMapping("{setId}")
	@Operation(summary = "루틴 상세 조회", description = "특정 루틴의 상세 정보를 조회합니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "조회 성공"),
		@ApiResponse(responseCode = "401", description = "인증 실패"),
		@ApiResponse(responseCode = "404", description = "루틴을 찾을 수 없음")
	})
	public ResponseEntity<RoutineDto> getRoutineDetail(@Parameter(hidden = true) @RequestHeader("Authorization") String authHeader, @Parameter(description = "세트 ID", example = "1") @PathVariable long setId){
		String token = authHeader.replace("Bearer ", "");
		String authEmail = jwtUtil.extractUserEmail(token);
		RoutineDto routine = routineService.getRoutine(authEmail, setId);
		System.out.println(routine);
		return ResponseEntity.ok(routine);
	}
	
	
	@PostMapping("create")
	@Operation(summary = "루틴 생성", description = "새로운 운동 루틴을 생성합니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "201", description = "루틴 생성 성공"),
		@ApiResponse(responseCode = "401", description = "인증 실패"),
		@ApiResponse(responseCode = "400", description = "잘못된 요청")
	})
	public ResponseEntity<RoutineDto> createRoutine(@Parameter(hidden = true) @RequestHeader("Authorization") String authHeader, @RequestBody RoutineDto routineDto){
		String token = authHeader.replace("Bearer ", "");
		String authEmail = jwtUtil.extractUserEmail(token);
		RoutineDto routine = routineService.createRoutine(authEmail,routineDto);
		return ResponseEntity.status(HttpStatus.CREATED).body(routine);
	}
	@Transactional
	@PutMapping("{setId}/update")
	@Operation(summary = "루틴 수정", description = "기존 루틴을 수정합니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "201", description = "루틴 수정 성공"),
		@ApiResponse(responseCode = "401", description = "인증 실패"),
		@ApiResponse(responseCode = "404", description = "루틴을 찾을 수 없음")
	})
	public ResponseEntity<RoutineDto> updateRoutine(@Parameter(hidden = true) @RequestHeader("Authorization") String authHeader, @Parameter(description = "세트 ID", example = "1") @PathVariable Long setId, @RequestBody RoutineDto routineDto){
		String token = authHeader.replace("Bearer ", "");
		String authEmail = jwtUtil.extractUserEmail(token);
		RoutineDto routine = routineService.updateRoutine(authEmail,setId,routineDto);
		return ResponseEntity.status(HttpStatus.CREATED).body(routine);
	}
	@Transactional
	@DeleteMapping("{setId}/delete") 
	@Operation(summary = "루틴 삭제", description = "루틴을 삭제합니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "루틴 삭제 성공"),
		@ApiResponse(responseCode = "401", description = "인증 실패"),
		@ApiResponse(responseCode = "404", description = "루틴을 찾을 수 없음")
	})
	public ResponseEntity<RoutineDto> updateRoutine(@Parameter(hidden = true) @RequestHeader("Authorization") String authHeader, @Parameter(description = "세트 ID", example = "1") @PathVariable Long setId){
		String token = authHeader.replace("Bearer ", "");
		String authEmail = jwtUtil.extractUserEmail(token);
		RoutineDto routine = routineService.deleteRoutine(authEmail,setId);
		return ResponseEntity.status(HttpStatus.OK).body(routine);
	}
	//루틴 상세 관련
	@GetMapping("{setId}/save/{srId}")
	@Operation(summary = "루틴 상세 정보 조회", description = "특정 루틴의 저장된 상세 정보를 조회합니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "조회 성공"),
		@ApiResponse(responseCode = "404", description = "루틴 상세 정보를 찾을 수 없음")
	})
	public ResponseEntity<SaveRoutineDto> getSaveRoutine(@Parameter(description = "세트 ID", example = "1") @PathVariable Long setId, @Parameter(description = "저장된 루틴 ID", example = "1") @PathVariable Long srId){
		SaveRoutineDto saveRoutine = routineService.getSaveRoutine(setId,srId);
		return ResponseEntity.status(HttpStatus.OK).body(saveRoutine);
	}
	
	@PostMapping("{setId}/save")
	@Operation(summary = "루틴 상세 정보 저장", description = "루틴의 상세 정보를 저장합니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "201", description = "저장 성공"),
		@ApiResponse(responseCode = "400", description = "잘못된 요청")
	})
	public ResponseEntity<SaveRoutineDto> createSaveRoutine(@RequestBody SaveRoutineDto saveRoutineDto, @Parameter(description = "세트 ID", example = "1") @PathVariable Long setId){
		SaveRoutineDto saveRoutine = routineService.createSaveRoutine(saveRoutineDto,setId);
		return ResponseEntity.status(HttpStatus.CREATED).body(saveRoutine);
	}
	
	@DeleteMapping("save/{srId}")
	@Operation(summary = "루틴 상세 정보 삭제", description = "저장된 루틴 상세 정보를 삭제합니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "삭제 성공"),
		@ApiResponse(responseCode = "404", description = "루틴 상세 정보를 찾을 수 없음")
	})
	public ResponseEntity<SaveRoutineDto> deleteSaveRoutine(@Parameter(description = "저장된 루틴 ID", example = "1") @PathVariable Long srId){
	
		SaveRoutineDto saveRoutine = routineService.deleteSaveRoutine(srId);
		return ResponseEntity.status(HttpStatus.OK).body(saveRoutine);
	}
	
	
	//루틴 결과 관련 api
	@PostMapping("{setId}/result")
	@Operation(summary = "루틴 결과 생성", description = "루틴 완료 후 결과를 저장합니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "201", description = "결과 저장 성공"),
		@ApiResponse(responseCode = "400", description = "잘못된 요청")
	})
	public ResponseEntity<RoutineEndTotalDto> createRoutineEndTotal(@RequestBody RoutineEndTotalDto routineEndTotalDto, @Parameter(description = "세트 ID", example = "1") @PathVariable Long setId){
		RoutineEndTotalDto routineEndTotal = routineService.createRoutineEndTotal(routineEndTotalDto,setId);
		return ResponseEntity.status(HttpStatus.CREATED).body(routineEndTotal);
	}
	
	@PostMapping("result") //이후 기간 추가해야함(모든 데이터 반환시 서버에 가해지는 부하 고려)
	@Operation(summary = "루틴 결과 조회", description = "사용자의 루틴 완료 결과를 조회합니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "조회 성공"),
		@ApiResponse(responseCode = "401", description = "인증 실패")
	})
	public ResponseEntity<List<RoutineEndTotalDto>> getRoutineEndTotal(@Parameter(hidden = true) @RequestHeader("Authorization") String authHeader, @RequestBody(required = false) RoutineEndTotalRequest routineEndTotalRequest){
		String token = authHeader.replace("Bearer ", "");
		String authEmail = jwtUtil.extractUserEmail(token);
		List<RoutineEndTotalDto> routineEndTotal = routineService.getRoutineEndTotal(authEmail,routineEndTotalRequest);
		return ResponseEntity.status(HttpStatus.OK).body(routineEndTotal);
	}
	
}
