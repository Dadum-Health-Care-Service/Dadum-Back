package com.project.mog.service.users;

import java.time.LocalDateTime;
import java.util.Optional;

import com.project.mog.repository.auth.AuthEntity;
import com.project.mog.repository.bios.BiosEntity;
import com.project.mog.repository.users.UsersEntity;
import com.project.mog.service.auth.AuthDto;
import com.project.mog.service.bios.BiosDto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
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
public class UsersDto {
	@Schema(description = "usersId",example="1")
	private long usersId;
	@Nullable
	private BiosDto biosDto;
	private AuthDto authDto;
	@Schema(description = "usersName",example="테스트유저")
	private String usersName;
	@Schema(description = "nickName",example="테스트닉네임")
	private String nickName;
	@Schema(description = "email",example="test@test.com")
	private String email;
	@Schema(description = "profileImg",example="profileImg.png")
	private String profileImg;
	@Schema(description = "phoneNum", example="01012345678")
	private String phoneNum;
	@Schema(description = "role", example="USER")
	private String role;
	private LocalDateTime regDate;
	private LocalDateTime updateDate;
	
	public UsersEntity toEntity() {
		UsersEntity uEntity = UsersEntity.builder()
					.usersId(usersId)
					.usersName(usersName)
					.nickName(nickName)
					.email(email)
					.profileImg(profileImg)
					.phoneNum(phoneNum)
					.role(role) 
					.regDate(regDate) 
					.updateDate(updateDate) 
					.bios(Optional.ofNullable(biosDto).map(BiosDto::toEntity).orElse(null))
					.auth(Optional.ofNullable(authDto).map(AuthDto::toEntity).orElse(null))
					.build();
		if(biosDto!=null) {
			BiosEntity bEntity = biosDto.toEntity();
			bEntity.setUser(uEntity);
			uEntity.setBios(bEntity);
		}
		if(authDto!=null) {
			AuthEntity aEntity = authDto.toEntity();
			aEntity.setUser(uEntity);
			uEntity.setAuth(aEntity);
		}
		return uEntity;
	}
	
	public static UsersDto toDto(UsersEntity uEntity) {
		if (uEntity==null) return null;
		return UsersDto.builder()
				.usersId(uEntity.getUsersId())
				.usersName(uEntity.getUsersName())
				.nickName(uEntity.getNickName())
				.email(uEntity.getEmail())
				.profileImg(uEntity.getProfileImg())
				.phoneNum(uEntity.getPhoneNum())
				.role(uEntity.getRole())
				.regDate(uEntity.getRegDate())
				.updateDate(uEntity.getUpdateDate())
				.biosDto(BiosDto.toDto(uEntity.getBios()))
				.authDto(AuthDto.toDto(uEntity.getAuth()))
				.build();
	}

}
