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

@RestController
@RequestMapping("/api/v1/routine/")
@RequiredArgsConstructor
public class RoutineController {
	
	private final JwtUtil jwtUtil;
	private final RoutineService routineService;
	
	//루틴 관련 api
	@GetMapping("list")
	public ResponseEntity<List<RoutineDto>> getAllRoutines(@RequestHeader("Authorization") String authHeader){
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
	public ResponseEntity<RoutineDto> getRoutineDetail(@RequestHeader("Authorization") String authHeader, @PathVariable long setId){
		String token = authHeader.replace("Bearer ", "");
		String authEmail = jwtUtil.extractUserEmail(token);
		RoutineDto routine = routineService.getRoutine(authEmail, setId);
		System.out.println(routine);
		return ResponseEntity.ok(routine);
	}
	
	
	@PostMapping("create")
	public ResponseEntity<RoutineDto> createRoutine(@RequestHeader("Authorization") String authHeader, @RequestBody RoutineDto routineDto){
		String token = authHeader.replace("Bearer ", "");
		String authEmail = jwtUtil.extractUserEmail(token);
		RoutineDto routine = routineService.createRoutine(authEmail,routineDto);
		return ResponseEntity.status(HttpStatus.CREATED).body(routine);
	}
	@Transactional
	@PutMapping("{setId}/update")
	public ResponseEntity<RoutineDto> updateRoutine(@RequestHeader("Authorization") String authHeader,@PathVariable Long setId, @RequestBody RoutineDto routineDto){
		String token = authHeader.replace("Bearer ", "");
		String authEmail = jwtUtil.extractUserEmail(token);
		RoutineDto routine = routineService.updateRoutine(authEmail,setId,routineDto);
		return ResponseEntity.status(HttpStatus.CREATED).body(routine);
	}
	@Transactional
	@DeleteMapping("{setId}/delete") 
	public ResponseEntity<RoutineDto> updateRoutine(@RequestHeader("Authorization") String authHeader,@PathVariable Long setId){
		String token = authHeader.replace("Bearer ", "");
		String authEmail = jwtUtil.extractUserEmail(token);
		RoutineDto routine = routineService.deleteRoutine(authEmail,setId);
		return ResponseEntity.status(HttpStatus.OK).body(routine);
	}
	//루틴 상세 관련
	@GetMapping("{setId}/save/{srId}")
	public ResponseEntity<SaveRoutineDto> getSaveRoutine( @PathVariable Long setId, @PathVariable Long srId){
		SaveRoutineDto saveRoutine = routineService.getSaveRoutine(setId,srId);
		return ResponseEntity.status(HttpStatus.OK).body(saveRoutine);
	}
	
	@PostMapping("{setId}/save")
	public ResponseEntity<SaveRoutineDto> createSaveRoutine(@RequestBody SaveRoutineDto saveRoutineDto, @PathVariable Long setId){
		SaveRoutineDto saveRoutine = routineService.createSaveRoutine(saveRoutineDto,setId);
		return ResponseEntity.status(HttpStatus.CREATED).body(saveRoutine);
	}
	
	@DeleteMapping("save/{srId}")
	public ResponseEntity<SaveRoutineDto> deleteSaveRoutine(@PathVariable Long srId){
	
		SaveRoutineDto saveRoutine = routineService.deleteSaveRoutine(srId);
		return ResponseEntity.status(HttpStatus.OK).body(saveRoutine);
	}
	
	
	//루틴 결과 관련 api
	@PostMapping("{setId}/result")
	public ResponseEntity<RoutineEndTotalDto> createRoutineEndTotal(@RequestBody RoutineEndTotalDto routineEndTotalDto,@PathVariable Long setId){
		RoutineEndTotalDto routineEndTotal = routineService.createRoutineEndTotal(routineEndTotalDto,setId);
		return ResponseEntity.status(HttpStatus.CREATED).body(routineEndTotal);
	}
	
	@PostMapping("result") //이후 기간 추가해야함(모든 데이터 반환시 서버에 가해지는 부하 고려)
	public ResponseEntity<List<RoutineEndTotalDto>> getRoutineEndTotal(@RequestHeader("Authorization") String authHeader, @RequestBody(required = false) RoutineEndTotalRequest routineEndTotalRequest){
		String token = authHeader.replace("Bearer ", "");
		String authEmail = jwtUtil.extractUserEmail(token);
		List<RoutineEndTotalDto> routineEndTotal = routineService.getRoutineEndTotal(authEmail,routineEndTotalRequest);
		return ResponseEntity.status(HttpStatus.OK).body(routineEndTotal);
	}
	
}
