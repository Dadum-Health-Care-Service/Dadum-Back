package com.project.mog.docs;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import com.project.mog.controller.login.LoginRequest;
import com.project.mog.controller.login.LoginResponse;
import com.project.mog.service.users.UsersDto;
import com.project.mog.service.users.UsersInfoDto;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name="사용자 API", description="사용자 계정 관련 API 입니다")
public interface UsersControllerDocs {

	@Operation(summary="전체 회원 조회",description="전체 회원 정보 조회 API")
	public ResponseEntity<List<UsersInfoDto>> getAllUsers();

	@Operation(summary="단일 회원 조회 ", description="단일 회원 정보 조회 API")
	@Parameter(description="usersId", name="usersId", required=true)
	public ResponseEntity<UsersInfoDto> getUser(@PathVariable Long usersId);

	@Operation(summary="회원 가입", description="회원 가입 API")
	public ResponseEntity<UsersDto> createUser(
			@io.swagger.v3.oas.annotations.parameters.RequestBody(
					content = @Content(
							schema = @Schema(implementation = UsersDto.class),
							examples= @ExampleObject(value="{\r\n"
									+ "    \"usersName\":\"테스트유저\",\r\n"
									+ "    \"email\":\"test@test.com\",\r\n"
									+ "    \"profileImg\":\"profileImg.png\",\r\n"
									+ "    \"biosDto\":{\r\n"
									+ "        \"gender\":0,\r\n"
									+ "        \"age\":40,\r\n"
									+ "        \"height\":180,\r\n"
									+ "        \"weight\":90\r\n"
									+ "    },\r\n"
									+ "    \"authDto\":{\r\n"
									+ "        \"password\":\"testuser\"\r\n"
									+ "    }\r\n"
									+ "\r\n"
									+ "\r\n"
									+ "}")
							)
			)
			@RequestBody UsersDto usersDto);

	@Operation(summary="회원 정보 수정", description="회원 정보 수정 API")
	@Parameter(name="Authorization", hidden=true)
	@Parameter(description="usersId", name="usersId", required=true)
	public ResponseEntity<UsersInfoDto> editUser(@RequestHeader("Authorization") String authHeader, @PathVariable Long usersId, @io.swagger.v3.oas.annotations.parameters.RequestBody(
			content = @Content(
					schema = @Schema(implementation = UsersDto.class),
					examples= @ExampleObject(value="")
					)
	)@RequestBody UsersInfoDto usersInfoDto);

	@Operation(summary="회원 탈퇴", description="회원 탈퇴 API")
	@Parameter(description="usersId", name="usersId", required=true)
	public ResponseEntity<UsersInfoDto> deleteUser(@Parameter(hidden=true) @RequestHeader("Authorization") String authHeader, @PathVariable Long usersId);

	@Operation(summary="로그인", description="로그인 API")
	public ResponseEntity<LoginResponse> login(@io.swagger.v3.oas.annotations.parameters.RequestBody(
			content = @Content(
					schema = @Schema(implementation = LoginRequest.class),
					examples= @ExampleObject(value="{\r\n"
							+ "    \"email\":\"test@test.com\",\r\n"
							+ "    \"password\":\"testuser\"\r\n"
							+ "}")
					)
	)@RequestBody LoginRequest request);


}
