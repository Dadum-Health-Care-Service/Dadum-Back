package com.project.mog.controller.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "비밀번호 확인 요청 DTO")
public class PasswordCheckRequest {
	@Schema(description = "현재 비밀번호", example = "currentPassword123")
	private String password;
}
