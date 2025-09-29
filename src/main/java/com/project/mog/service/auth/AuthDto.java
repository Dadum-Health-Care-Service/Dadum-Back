package com.project.mog.service.auth;

import java.time.LocalDateTime;

import com.project.mog.repository.auth.AuthEntity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthDto {
	@Schema(hidden = true)
	private long authId;
	@Schema(description = "password",example="testuser1")
	private String password;
	@Schema(hidden = true)
	private LocalDateTime connectTime;
	
	public AuthEntity toEntity() {
			return AuthEntity.builder()
					.authId(authId)
					.password(password)
					.connectTime(connectTime)
					.build();
	}
	
	public static AuthDto toDto(AuthEntity aEntity) {
		if(aEntity==null) return null;	
		return AuthDto.builder()
				.authId(aEntity.getAuthId())
				.password(aEntity.getPassword())
				.connectTime(aEntity.getConnectTime())
				.build();
	}

}
