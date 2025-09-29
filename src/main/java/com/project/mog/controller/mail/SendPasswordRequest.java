package com.project.mog.controller.mail;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@Schema(description = "비밀번호 찾기 이메일 전송 요청 DTO")
public class SendPasswordRequest {
	@Schema(description = "이메일 주소", example = "test@test.com")
	private String email;
	@Schema(description = "사용자 이름", example = "홍길동")
	private String usersName;
}
