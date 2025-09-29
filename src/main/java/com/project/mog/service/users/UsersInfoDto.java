package com.project.mog.service.users;

import java.time.LocalDateTime;

import com.project.mog.repository.auth.AuthEntity;
import com.project.mog.repository.bios.BiosEntity;
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
public class UsersInfoDto {
	@Schema(hidden = true)
	private long usersId;
	@Nullable
	private BiosDto biosDto;
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
	private RoleAssignmentDto roleAssignmentDto;
	@Schema(hidden = true)
	private LocalDateTime regDate;
	@Schema(hidden = true)
	private LocalDateTime updateDate;
	
	
	public UsersInfoDto applyTo(UsersEntity user, BiosEntity bios) {
		user.setUsersName(usersName);
		user.setNickName(nickName);
		user.setProfileImg(profileImg);
		user.setPhoneNum(phoneNum);
		user.setRoleAssignment(roleAssignmentDto.toEntity());
		user.setUpdateDate(LocalDateTime.now());
		
		if(biosDto!=null) {
			bios.setAge(biosDto.toEntity().getAge());
			bios.setGender(biosDto.toEntity().isGender());
			bios.setHeight(biosDto.toEntity().getHeight());
			bios.setWeight(biosDto.toEntity().getWeight());
			user.setBios(bios);	
		}
		else {
			user.setBios(null);
		}
		
		return UsersInfoDto.builder()
				.usersId(user.getUsersId())
				.email(user.getEmail())
				.usersName(user.getUsersName())
				.profileImg(user.getProfileImg())
				.nickName(user.getNickName())
				.phoneNum(user.getPhoneNum())
				.roleAssignmentDto(RoleAssignmentDto.toDto(user.getRoleAssignment()))
				.regDate(user.getRegDate())
				.updateDate(user.getUpdateDate())
				.biosDto(BiosDto.toDto(user.getBios()))
				.build();
	}
	
	public static UsersInfoDto toDto(UsersEntity user) {
		return UsersInfoDto.builder().usersId(user.getUsersId())
				.email(user.getEmail())
				.usersName(user.getUsersName())
				.profileImg(user.getProfileImg())
				.nickName(user.getNickName())
				.phoneNum(user.getPhoneNum())
				.roleAssignmentDto(RoleAssignmentDto.toDto(user.getRoleAssignment()))
				.regDate(user.getRegDate())
				.updateDate(user.getUpdateDate())
				.biosDto(BiosDto.toDto(user.getBios()))
				.build();
	}
}
