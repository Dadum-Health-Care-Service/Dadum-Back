package com.project.mog.controller.users;

import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.stream.Collectors;

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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.project.mog.controller.auth.EmailFindRequest;
import com.project.mog.controller.auth.PasswordCheckRequest;
import com.project.mog.controller.auth.PasswordUpdateRequest;
import com.project.mog.controller.auth.PasswordlessLoginRequest;
import com.project.mog.controller.auth.PasswordlessRegisterRequest;
import com.project.mog.controller.login.LoginRequest;
import com.project.mog.controller.login.LoginResponse;
import com.project.mog.controller.login.SocialLoginRequest;
import com.project.mog.repository.users.UsersEntity;
import com.project.mog.security.jwt.JwtUtil;
import com.project.mog.service.mail.MailDto;
import com.project.mog.service.mail.MailService;
import com.project.mog.service.mail.SendPasswordRequest;
import com.project.mog.service.role.RoleAssignmentDto;
import com.project.mog.service.role.RoleDeleteDto;
import com.project.mog.service.role.RolePermitDto;
import com.project.mog.service.role.RoleRequestDto;
import com.project.mog.service.role.RoleResponseDto;
import com.project.mog.service.role.RolesDto;
import com.project.mog.service.role.RolesService;
import com.project.mog.service.users.HomeStatsDto;
import com.project.mog.service.users.HomeRoutineItemDto;
import com.project.mog.service.users.UsersDto;
import com.project.mog.service.users.UsersInfoDto;
import com.project.mog.service.users.UsersService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;



@RestController
@RequestMapping("/api/v1/users/")
@RequiredArgsConstructor
@Tag(name = "사용자 관리", description = "사용자 관련 API")
public class UsersController {
	private final JwtUtil jwtUtil;
	private final UsersService usersService;
	private final MailService mailService;
	private final RolesService rolesService;
	
	
	@GetMapping("list")
	@Operation(summary = "전체 사용자 목록 조회", description = "모든 사용자의 정보를 조회합니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "조회 성공"),
		@ApiResponse(responseCode = "500", description = "서버 오류")
	})
	public ResponseEntity<List<UsersInfoDto>> getAllUsers(){
		List<UsersInfoDto> users = usersService.getAllUsers();
		return ResponseEntity.ok(users);
	}
	
	@PostMapping("signup")
	@Operation(summary = "회원가입", description = "새로운 사용자를 등록합니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "201", description = "회원가입 성공"),
		@ApiResponse(responseCode = "400", description = "잘못된 요청"),
		@ApiResponse(responseCode = "409", description = "이미 존재하는 이메일")
	})
	public ResponseEntity<UsersDto> createUser(@RequestBody UsersDto usersDto){
		UsersDto createUsers = usersService.createUser(usersDto);
		return ResponseEntity.status(HttpStatus.CREATED).body(createUsers);
	}
	@GetMapping("/{usersId:\\d+}")
	@Operation(summary = "사용자 정보 조회", description = "사용자 ID로 특정 사용자 정보를 조회합니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "조회 성공"),
		@ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
	})
	public ResponseEntity<UsersInfoDto> getUser(@Parameter(description = "사용자 ID", example = "1") @PathVariable Long usersId){
		UsersInfoDto findUsers = usersService.getUser(usersId);
		return ResponseEntity.status(HttpStatus.OK).body(findUsers);
	}
	@GetMapping("/email/{email}")
	@Operation(summary = "이메일로 사용자 조회", description = "이메일 주소로 사용자 정보를 조회합니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "조회 성공"),
		@ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
	})
	public ResponseEntity<UsersInfoDto> getUserByEmail(@Parameter(description = "이메일 주소", example = "test@test.com") @PathVariable String email){
		UsersInfoDto findUsers = usersService.getUserByEmail(email);
		return ResponseEntity.status(HttpStatus.OK).body(findUsers);
	}
	@Transactional
	@PutMapping("/update/{usersId}")
	@Operation(summary = "사용자 정보 수정", description = "사용자 정보를 수정합니다. 본인 정보만 수정 가능합니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "수정 성공"),
		@ApiResponse(responseCode = "401", description = "인증 실패"),
		@ApiResponse(responseCode = "403", description = "권한 없음"),
		@ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
	})
	public ResponseEntity<UsersInfoDto> editUser(@Parameter(hidden = true) @RequestHeader("Authorization") String authHeader, @Parameter(description = "사용자 ID", example = "1") @PathVariable Long usersId,@RequestBody UsersInfoDto usersInfoDto){
		String token = authHeader.replace("Bearer ", "");
		String authEmail = jwtUtil.extractUserEmail(token);
		UsersInfoDto editUsers = usersService.editUser(usersInfoDto,usersId,authEmail);
		return ResponseEntity.status(HttpStatus.OK).body(editUsers);
	}
	@Transactional
	@DeleteMapping("/delete/{usersId}")
	@Operation(summary = "사용자 삭제", description = "사용자 계정을 삭제합니다. 본인 계정만 삭제 가능합니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "삭제 성공"),
		@ApiResponse(responseCode = "401", description = "인증 실패"),
		@ApiResponse(responseCode = "403", description = "권한 없음"),
		@ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
	})
	public ResponseEntity<UsersInfoDto> deleteUser(@Parameter(hidden = true) @RequestHeader("Authorization") String authHeader, @Parameter(description = "사용자 ID", example = "1") @PathVariable Long usersId){
		String token = authHeader.replace("Bearer ", "");
		String authEmail = jwtUtil.extractUserEmail(token);
		UsersInfoDto deleteUsers = usersService.deleteUser(usersId,authEmail);
		return ResponseEntity.status(HttpStatus.OK).body(deleteUsers);
	}
	
	
	@PostMapping("login")
	@Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인합니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "로그인 성공"),
		@ApiResponse(responseCode = "401", description = "인증 실패"),
		@ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
	})
	public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request){
		UsersDto usersDto = usersService.login(request);
		
		long usersId = usersDto.getUsersId();
		String email = usersDto.getEmail();
		List<String> roles = usersDto.getRoleAssignments().stream().map(RoleAssignmentDto::getRolesDto).map(RolesDto::getRoleName).collect(Collectors.toList());
		String accessToken = jwtUtil.generateAccessToken(email);
		String refreshToken = jwtUtil.generateRefreshToken(email);
		
		
		LoginResponse loginResponse = LoginResponse.builder()
											.usersId(usersId)
											.email(email)
											.roles(roles)
											.accessToken(accessToken)
											.refreshToken(refreshToken)
											.build();
		
		return ResponseEntity.status(HttpStatus.OK).body(loginResponse);
	}
	
	@PostMapping("login/kakao")
	@Operation(summary = "소셜 로그인", description = "카카오 소셜 로그인을 처리합니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "로그인 성공"),
		@ApiResponse(responseCode = "401", description = "인증 실패"),
		@ApiResponse(responseCode = "400", description = "잘못된 요청")
	})
	public ResponseEntity<LoginResponse> socialLogin(@RequestBody SocialLoginRequest request){
		UsersDto usersDto = usersService.socialLogin(request);
		long usersId = usersDto.getUsersId();
		String email = usersDto.getEmail();
		List<String> roles = usersDto.getRoleAssignments().stream().map(RoleAssignmentDto::getRolesDto).map(RolesDto::getRoleName).collect(Collectors.toList());
		String accessToken = jwtUtil.generateAccessToken(email);
		String refreshToken = jwtUtil.generateRefreshToken(email);
		
		LoginResponse loginResponse = LoginResponse.builder()
										.usersId(usersId)
										.email(email)
										.roles(roles)
										.accessToken(accessToken)
										.refreshToken(refreshToken)
										.build();
		return ResponseEntity.status(HttpStatus.OK).body(loginResponse);
	}
	
	@PostMapping("auth/email/find")
	@Operation(summary = "이메일 찾기", description = "이름과 전화번호로 이메일을 찾습니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "조회 성공"),
		@ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
	})
	public ResponseEntity<UsersInfoDto> findEmail(@RequestBody EmailFindRequest emailFindRequest){
		UsersInfoDto usersInfoDto = usersService.getUserByRequest(emailFindRequest);
		
		return ResponseEntity.status(HttpStatus.OK).body(usersInfoDto);
		
	}
	
	@PostMapping("auth/password/check")
	@Operation(summary = "비밀번호 확인", description = "현재 비밀번호를 확인합니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "비밀번호 확인 성공"),
		@ApiResponse(responseCode = "401", description = "인증 실패"),
		@ApiResponse(responseCode = "400", description = "비밀번호 불일치")
	})
	public ResponseEntity<UsersDto> checkPassword(@Parameter(hidden = true) @RequestHeader("Authorization") String authHeader, @RequestBody PasswordCheckRequest passwordCheckRequest){
		String token = authHeader.replace("Bearer ", "");
		String authEmail = jwtUtil.extractUserEmail(token);
		String password = passwordCheckRequest.getPassword();
		UsersDto usersDto = usersService.checkPassword(authEmail,password);
		return ResponseEntity.status(HttpStatus.OK).body(usersDto);
	}

    // ===== Home endpoints (DB-backed) =====
    @GetMapping("stats")
    public ResponseEntity<HomeStatsDto> getUserStats(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            if (authHeader == null || authHeader.isBlank()) {
                HomeStatsDto defaults = HomeStatsDto.builder()
                        .consecutiveDays(0)
                        .totalRoutines(0)
                        .totalTime("0분")
                        .consecutiveMessage("지금 시작해보세요")
                        .routinesMessage("루틴을 만들어 보세요")
                        .timeMessage("지금 시작해보세요")
                        .build();
                return ResponseEntity.ok(defaults);
            }
            String token = authHeader.replace("Bearer ", "");
            String authEmail = jwtUtil.extractUserEmail(token);
            HomeStatsDto stats = usersService.getHomeStats(authEmail);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            HomeStatsDto defaults = HomeStatsDto.builder()
                    .consecutiveDays(0)
                    .totalRoutines(0)
                    .totalTime("0분")
                    .consecutiveMessage("지금 시작해보세요")
                    .routinesMessage("루틴을 만들어 보세요")
                    .timeMessage("지금 시작해보세요")
                    .build();
            return ResponseEntity.ok(defaults);
        }
    }

    @GetMapping("routines")
    public ResponseEntity<java.util.List<HomeRoutineItemDto>> getUserRoutines(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            if (authHeader == null || authHeader.isBlank()) {
                return ResponseEntity.ok(java.util.List.of());
            }
            String token = authHeader.replace("Bearer ", "");
            String authEmail = jwtUtil.extractUserEmail(token);
            java.util.List<HomeRoutineItemDto> routines = usersService.getHomeRoutines(authEmail);
            return ResponseEntity.ok(routines);
        } catch (Exception e) {
            return ResponseEntity.ok(java.util.List.of());
        }
    }
	
	@Transactional
	@PutMapping("auth/password/update")
	@Operation(summary = "비밀번호 변경", description = "사용자의 비밀번호를 변경합니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "비밀번호 변경 성공"),
		@ApiResponse(responseCode = "401", description = "인증 실패"),
		@ApiResponse(responseCode = "400", description = "현재 비밀번호 불일치")
	})
	public ResponseEntity<UsersDto> editPassword(@Parameter(hidden = true) @RequestHeader("Authorization") String authHeader, @RequestBody PasswordUpdateRequest passwordUpdateRequest){
		String token = authHeader.replace("Bearer ", "");
		String authEmail = jwtUtil.extractUserEmail(token);
		String originPassword = passwordUpdateRequest.getOriginPassword();
		String newPassword = passwordUpdateRequest.getNewPassword();
		
		UsersDto usersDto = usersService.editPassword(authEmail,originPassword,newPassword);
		
		return ResponseEntity.status(HttpStatus.OK).body(usersDto);
	}
	
	@Transactional
	@PostMapping("send/password")
	@Operation(summary = "비밀번호 찾기", description = "이메일로 비밀번호를 전송합니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "이메일 전송 성공"),
		@ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음"),
		@ApiResponse(responseCode = "500", description = "이메일 전송 실패")
	})
	public ResponseEntity<String> sendPassword(@RequestBody SendPasswordRequest sendPasswordRequest) {
		String email = sendPasswordRequest.getEmail();
		String usersName = sendPasswordRequest.getUsersName();
		String tempPassword = usersService.updatePasswordToTemp(sendPasswordRequest);
		MailDto mail = mailService.createMail(tempPassword, usersName, email);
		mailService.sendMail(mail);
		
		return ResponseEntity.status(HttpStatus.OK).body("비밀번호 찾기 이메일 전송 완료 : "+tempPassword);
	}
	
	@Transactional
	@Operation(summary = "패스워드리스 로그인", description = "패스워드리스 로그인을 처리합니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "로그인 성공"),
		@ApiResponse(responseCode = "401", description = "인증 실패"),
		@ApiResponse(responseCode = "400", description = "잘못된 요청")
	})
	@PostMapping("auth/passwordless/login")
	public ResponseEntity<LoginResponse> loginPasswordless(@RequestBody PasswordlessLoginRequest passwordlessLoginRequest) throws NoSuchAlgorithmException{
		String email = passwordlessLoginRequest.getEmail();
		String passwordlessToken = passwordlessLoginRequest.getPasswordlessToken();
		
		UsersDto usersDto = usersService.loginPasswordless(email,passwordlessToken);
		Long usersId = usersDto.getUsersId();
		List<String> roles = usersDto.getRoleAssignments().stream().map((assign)->assign.getRolesDto().getRoleName()).toList();

		String accessToken = jwtUtil.generateAccessToken(email);
		String refreshToken = jwtUtil.generateRefreshToken(email);
		LoginResponse loginResponse = LoginResponse.builder()
				.usersId(usersId)
				.email(email)
				.roles(roles)
				.accessToken(accessToken)
				.refreshToken(refreshToken)
				.build();
		return ResponseEntity.status(HttpStatus.OK).body(loginResponse);
	}
	
	@Transactional
	@Operation(summary = "패스워드리스 등록", description = "패스워드리스 등록을 처리합니다. 등록 후에는 기존 비밀번호로 로그인이 불가능합니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "회원가입 성공"),
		@ApiResponse(responseCode = "401", description = "인증 실패"),
		@ApiResponse(responseCode = "400", description = "잘못된 요청")
	})
	@PostMapping("auth/passwordless/register")
	public ResponseEntity<UsersDto> registerPasswordless(@Parameter(hidden = true) @RequestHeader("Authorization") String authHeader, @RequestBody PasswordlessRegisterRequest passwordlessRegisterRequest) throws JsonProcessingException, NoSuchAlgorithmException{
		String token = authHeader.replace("Bearer ", "");
		String authEmail = jwtUtil.extractUserEmail(token);
		String passwordlessToken = passwordlessRegisterRequest.getPasswordlessToken();
		
		UsersDto usersDto = usersService.registerPasswordless(authEmail,passwordlessToken);
		
		return ResponseEntity.status(HttpStatus.OK).body(usersDto);
	}


	@Transactional
	@Operation(summary = "권한 요청", description = "사용자 권한 변경을 요청합니다.")
	@PostMapping("role/request")
	public ResponseEntity<List<RoleAssignmentDto>> requestRoleAssignment(@Parameter(hidden = true) @RequestHeader("Authorization") String authHeader, @RequestBody RoleRequestDto roleRequestDto) {
		String token = authHeader.replace("Bearer ", "");
		String authEmail = jwtUtil.extractUserEmail(token);
		List<RoleAssignmentDto> roleAssignmentDto = rolesService.requestRoleAssignment(roleRequestDto,authEmail);
		return ResponseEntity.status(HttpStatus.OK).body(roleAssignmentDto);
	}

	@Transactional
	@Operation(summary = "권한 삭제", description = "사용자 권한을 삭제합니다.")
	@DeleteMapping("role/delete/{usersId}")
	public ResponseEntity<List<RoleAssignmentDto>> deleteRoleAssignment(@Parameter(hidden = true) @RequestHeader("Authorization") String authHeader, @PathVariable Long usersId, @RequestBody RoleDeleteDto roleDeleteDto) {
		String token = authHeader.replace("Bearer ", "");
		String authEmail = jwtUtil.extractUserEmail(token);
		List<RoleAssignmentDto> roleAssignmentDto = rolesService.deleteRoleAssignment(roleDeleteDto,usersId,authEmail);
		return ResponseEntity.status(HttpStatus.OK).body(roleAssignmentDto);
	}

	@Transactional
	@Operation(summary = "권한 허가", description = "사용자 권한을 허가합니다.")
	@PutMapping("role/update/{usersId}")
	public ResponseEntity<RoleAssignmentDto> permitRoleAssignment(@Parameter(hidden = true) @RequestHeader("Authorization") String authHeader, @PathVariable Long usersId, @RequestBody RolePermitDto rolePermitDto) {
		String token = authHeader.replace("Bearer ", "");
		String authEmail = jwtUtil.extractUserEmail(token);
		RoleAssignmentDto roleAssignmentDto = rolesService.permitRoleAssignment(rolePermitDto,usersId,authEmail);
		return ResponseEntity.status(HttpStatus.OK).body(roleAssignmentDto);
	}

	@Transactional
	@Operation(summary = "전체 권한 조회", description = "전체 사용자의 권한을 조회합니다.")
	@GetMapping("role/list")
	public ResponseEntity<List<UsersDto>> getRoleAssignment(@Parameter(hidden = true) @RequestHeader("Authorization") String authHeader) {
		String token = authHeader.replace("Bearer ", "");
		String authEmail = jwtUtil.extractUserEmail(token);
		List<UsersDto> usersDto = rolesService.getRoleAssignment(authEmail);
		return ResponseEntity.status(HttpStatus.OK).body(usersDto);
	}

	@Transactional
	@Operation(summary = "전체 권한 요청 조회", description = "전체 사용자의 권한 요청을 조회합니다.")
	@GetMapping("role/request/list")
	public ResponseEntity<List<RoleResponseDto>> getRoleRequest(@Parameter(hidden = true) @RequestHeader("Authorization") String authHeader) {
		String token = authHeader.replace("Bearer ", "");
		String authEmail = jwtUtil.extractUserEmail(token);
		List<RoleResponseDto> roleAssignmentDto = rolesService.getRoleRequests(authEmail);
		return ResponseEntity.status(HttpStatus.OK).body(roleAssignmentDto);
	}

}
