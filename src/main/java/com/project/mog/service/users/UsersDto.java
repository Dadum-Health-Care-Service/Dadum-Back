package com.project.mog.service.users;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.project.mog.repository.auth.AuthEntity;
import com.project.mog.repository.bios.BiosEntity;
import com.project.mog.repository.role.RoleAssignmentEntity;
import com.project.mog.repository.users.UsersEntity;
import com.project.mog.service.auth.AuthDto;
import com.project.mog.service.bios.BiosDto;
import com.project.mog.service.role.RoleAssignmentDto;

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
	@Schema(hidden = true)
	private long usersId;
	@Nullable
	private BiosDto biosDto;
	private AuthDto authDto;
	private List<RoleAssignmentDto> roleAssignments;
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
	@Schema(hidden = true)
	private LocalDateTime regDate;
	@Schema(hidden = true)
	private LocalDateTime updateDate;
	
	public UsersEntity toEntity() {
		UsersEntity uEntity = UsersEntity.builder()
					.usersId(usersId)
					.usersName(usersName)
					.nickName(nickName)
					.email(email)
					.profileImg(profileImg)
					.phoneNum(phoneNum)
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
		if(roleAssignments!=null) {
			List<RoleAssignmentEntity> rEntity = roleAssignments.stream().map(RoleAssignmentDto::toEntity).collect(Collectors.toList());
			rEntity.forEach(r->r.setUser(uEntity));
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
				.roleAssignments(RoleAssignmentDto.toDto(uEntity.getRoleAssignments()))
				.regDate(uEntity.getRegDate())
				.updateDate(uEntity.getUpdateDate())
				.biosDto(BiosDto.toDto(uEntity.getBios()))
				.authDto(AuthDto.toDto(uEntity.getAuth()))
				.build();
	}

}
