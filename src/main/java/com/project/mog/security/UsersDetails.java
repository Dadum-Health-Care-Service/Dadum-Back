package com.project.mog.security;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.project.mog.repository.users.UsersEntity;

public class UsersDetails implements UserDetails {
	private UsersEntity usersEntity;
	
	public UsersDetails(UsersEntity usersEntity) {
		this.usersEntity = usersEntity;
	}
	
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		String role = usersEntity.getRoleAssignment().getRole().getRoleName();
		
		// 역할에 따른 권한 부여
		switch (role) {
			case "SUPER_ADMIN":
				return List.of(
					new SimpleGrantedAuthority("ROLE_SUPER_ADMIN"),
					new SimpleGrantedAuthority("ROLE_ADMIN"),
					new SimpleGrantedAuthority("ROLE_USER")
				);
			case "ADMIN":
				return List.of(
					new SimpleGrantedAuthority("ROLE_ADMIN"),
					new SimpleGrantedAuthority("ROLE_USER")
				);
			case "USER":
			default:
				return List.of(new SimpleGrantedAuthority("ROLE_USER"));
		}
	}

	@Override
	public String getPassword() {
		// 비밀번호 정보 제공
		return usersEntity.getAuth().getPassword();
	}

	@Override
	public String getUsername() {
		// ID 정보 제공
		return usersEntity.getEmail();
	}

	@Override
	public boolean isAccountNonExpired() {
		//계정 만료 여부
		//사용 안할시 항상 true 반환 처리
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		// 계정 비활성화 여부
		// 사용 안할시 항상 true 반환 처리
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		// 계정 인증 정보 항상 저장 여부
		// true 처리시 모든 인증정보를 만료시키지 않으므로 false처리
		return false;
	}

	@Override
	public boolean isEnabled() {
		// 계정 활성화 여부
		// 사용 안할시 항상 true 처리
		return true;
	}
	
}
