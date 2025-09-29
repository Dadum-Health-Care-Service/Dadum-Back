package com.project.mog.controller.login;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {
	@Schema(description = "usersId",example="1")
	private Long usersId;
	@Schema(description = "email",example="test@test.com")
	private String email;
	@Schema(description = "role",example="USER")
	private List<String> roles;
	@Schema(description = "JWT 액세스 토큰")
	private String accessToken;
	@Schema(description = "JWT 리프레시 토큰")
	private String refreshToken;
}
