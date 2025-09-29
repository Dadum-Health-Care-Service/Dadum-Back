package com.project.mog.controller.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "이메일 찾기 요청 DTO")
public class EmailFindRequest {
	@Schema(description = "사용자 이름", example = "홍길동")
	private String usersName;
	@Schema(description = "전화번호", example = "010-1234-5678")
	private String phoneNum;
	
}
