package com.project.mog.controller.users;

import java.security.NoSuchAlgorithmException;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.project.mog.controller.auth.EmailFindRequest;
import com.project.mog.controller.auth.PasswordCheckRequest;
import com.project.mog.controller.auth.PasswordUpdateRequest;
import com.project.mog.controller.auth.PasswordlessRegisterRequest;
import com.project.mog.controller.login.LoginRequest;
import com.project.mog.controller.login.LoginResponse;
import com.project.mog.controller.login.SocialLoginRequest;
import com.project.mog.controller.mail.SendPasswordRequest;
import com.project.mog.docs.UsersControllerDocs;
import com.project.mog.repository.users.UsersEntity;
import com.project.mog.security.jwt.JwtUtil;
import com.project.mog.service.mail.MailDto;
import com.project.mog.service.mail.MailService;
import com.project.mog.service.users.UsersDto;
import com.project.mog.service.users.UsersInfoDto;
import com.project.mog.service.users.UsersService;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;



@RestController
@RequestMapping("/api/v1/users/")
@RequiredArgsConstructor
public class UsersController implements UsersControllerDocs{
	private final JwtUtil jwtUtil;
	private final UsersService usersService;
	private final MailService mailService;
	
	
	@GetMapping("list")
	public ResponseEntity<List<UsersInfoDto>> getAllUsers(){
		List<UsersInfoDto> users = usersService.getAllUsers();
		return ResponseEntity.ok(users);
	}
	
	@PostMapping("signup")
	public ResponseEntity<UsersDto> createUser(@RequestBody UsersDto usersDto){
		UsersDto createUsers = usersService.createUser(usersDto);
		return ResponseEntity.status(HttpStatus.CREATED).body(createUsers);
	}
	@GetMapping("/{usersId:\\d+}")
	public ResponseEntity<UsersInfoDto> getUser(@PathVariable Long usersId){
		UsersInfoDto findUsers = usersService.getUser(usersId);
		return ResponseEntity.status(HttpStatus.OK).body(findUsers);
	}
	@GetMapping("/email/{email}")
	public ResponseEntity<UsersInfoDto> getUserByEmail(@PathVariable String email){
		UsersInfoDto findUsers = usersService.getUserByEmail(email);
		return ResponseEntity.status(HttpStatus.OK).body(findUsers);
	}
	@Transactional
	@PutMapping("/update/{usersId}")
	public ResponseEntity<UsersInfoDto> editUser(@RequestHeader("Authorization") String authHeader, @PathVariable Long usersId,@RequestBody UsersInfoDto usersInfoDto){
		String token = authHeader.replace("Bearer ", "");
		String authEmail = jwtUtil.extractUserEmail(token);
		UsersInfoDto editUsers = usersService.editUser(usersInfoDto,usersId,authEmail);
		return ResponseEntity.status(HttpStatus.OK).body(editUsers);
	}
	@Transactional
	@DeleteMapping("/delete/{usersId}")
	public ResponseEntity<UsersInfoDto> deleteUser(@RequestHeader("Authorization") String authHeader, @PathVariable Long usersId){
		String token = authHeader.replace("Bearer ", "");
		String authEmail = jwtUtil.extractUserEmail(token);
		UsersInfoDto deleteUsers = usersService.deleteUser(usersId,authEmail);
		return ResponseEntity.status(HttpStatus.OK).body(deleteUsers);
	}
	
	
	@PostMapping("login")
	public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request){
		UsersDto usersDto = usersService.login(request);
		
		long usersId = usersDto.getUsersId();
		String email = usersDto.getEmail();
		String role = usersDto.getRole();
		String accessToken = jwtUtil.generateAccessToken(email);
		String refreshToken = jwtUtil.generateRefreshToken(email);
		
		
		LoginResponse loginResponse = LoginResponse.builder()
											.usersId(usersId)
											.email(email)
											.role(role)
											.accessToken(accessToken)
											.refreshToken(refreshToken)
											.build();
		
		return ResponseEntity.status(HttpStatus.OK).body(loginResponse);
	}
	
	@PostMapping("login/kakao")
	public ResponseEntity<LoginResponse> socialLogin(@RequestBody SocialLoginRequest request){
		UsersDto usersDto = usersService.socialLogin(request);
		long usersId = usersDto.getUsersId();
		String email = usersDto.getEmail();
		String role = usersDto.getRole();
		String accessToken = jwtUtil.generateAccessToken(email);
		String refreshToken = jwtUtil.generateRefreshToken(email);
		
		LoginResponse loginResponse = LoginResponse.builder()
										.usersId(usersId)
										.email(email)
										.role(role)
										.accessToken(accessToken)
										.refreshToken(refreshToken)
										.build();
		return ResponseEntity.status(HttpStatus.OK).body(loginResponse);
	}
	
	@PostMapping("auth/email/find")
	public ResponseEntity<UsersInfoDto> findEmail(@RequestBody EmailFindRequest emailFindRequest){
		UsersInfoDto usersInfoDto = usersService.getUserByRequest(emailFindRequest);
		
		return ResponseEntity.status(HttpStatus.OK).body(usersInfoDto);
		
	}
	
	@PostMapping("auth/password/check")
	public ResponseEntity<UsersDto> checkPassword(@RequestHeader("Authorization") String authHeader, @RequestBody PasswordCheckRequest passwordCheckRequest){
		String token = authHeader.replace("Bearer ", "");
		String authEmail = jwtUtil.extractUserEmail(token);
		String password = passwordCheckRequest.getPassword();
		UsersDto usersDto = usersService.checkPassword(authEmail,password);
		return ResponseEntity.status(HttpStatus.OK).body(usersDto);
	}
	
	@Transactional
	@PutMapping("auth/password/update")
	public ResponseEntity<UsersDto> editPassword(@RequestHeader("Authorization") String authHeader, @RequestBody PasswordUpdateRequest passwordUpdateRequest){
		String token = authHeader.replace("Bearer ", "");
		String authEmail = jwtUtil.extractUserEmail(token);
		String originPassword = passwordUpdateRequest.getOriginPassword();
		String newPassword = passwordUpdateRequest.getNewPassword();
		
		UsersDto usersDto = usersService.editPassword(authEmail,originPassword,newPassword);
		
		return ResponseEntity.status(HttpStatus.OK).body(usersDto);
	}
	
	@PostMapping("send/password")
	public ResponseEntity<String> sendPassword(@RequestBody SendPasswordRequest sendPasswordRequest) {
		String email = sendPasswordRequest.getEmail();
		String usersName = sendPasswordRequest.getUsersName();
		UsersDto usersDto = usersService.getPassword(email);
		String password = usersDto.getAuthDto().getPassword();
		MailDto mail = mailService.createMail(password, usersName, email);
		mailService.sendMail(mail);
		
		return ResponseEntity.status(HttpStatus.OK).body("비밀번호 찾기 이메일 전송 완료");
	}
	
	@Transactional
	@PostMapping("auth/passwordless/register")
	public ResponseEntity<UsersDto> registerPasswordless(@RequestHeader("Authorization") String authHeader, @RequestBody PasswordlessRegisterRequest passwordlessRegisterRequest) throws JsonProcessingException, NoSuchAlgorithmException{
		String token = authHeader.replace("Bearer ", "");
		String authEmail = jwtUtil.extractUserEmail(token);
		String passwordlessToken = passwordlessRegisterRequest.getPasswordlessToken();
		
		UsersDto usersDto = usersService.registerPasswordless(authEmail,passwordlessToken);
		
		return ResponseEntity.status(HttpStatus.OK).body(usersDto);
		
	}
}
