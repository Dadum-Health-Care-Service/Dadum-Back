package com.project.mog.controller.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "비밀번호 변경 요청 DTO")
public class PasswordUpdateRequest {
	@Schema(description = "현재 비밀번호", example = "currentPassword123")
	private String originPassword;
	@Schema(description = "새 비밀번호", example = "newPassword123")
	private String newPassword;
}
