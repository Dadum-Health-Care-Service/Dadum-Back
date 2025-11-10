package com.project.mog.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.project.mog.repository.role.RoleAssignmentEntity;
import com.project.mog.repository.role.RolesEntity;
import com.project.mog.repository.users.UsersEntity;

public class UsersDetails implements UserDetails {
	private UsersEntity usersEntity;
	private final List<String> roles;

	public UsersDetails(UsersEntity usersEntity) {
		this.usersEntity = usersEntity;
		this.roles = usersEntity.getRoleAssignments().stream()
			.map(RoleAssignmentEntity::getRole)
			.map(RolesEntity::getRoleName)
			.collect(Collectors.toList());
	}
	
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		if (roles.isEmpty()) {
			return List.of(new SimpleGrantedAuthority("ROLE_USER"));
		}
		
		// 역할들의 권한을 적절히 부여 - 상위 역할이 하위 권한도 가지도록
		List<GrantedAuthority> authorities = new ArrayList<>();
		
		for (String role : roles) {
			switch (role.toUpperCase()) {
				case "SUPER_ADMIN":
					authorities.add(new SimpleGrantedAuthority("ROLE_SUPER_ADMIN"));
					authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
					authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
					break;
				case "ADMIN":
					authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
					authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
					break;
				case "USER":
				default:
					authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
					break;
			}
		}
		
		// 중복 제거하여 반환
		return authorities.stream()
				.distinct()
				.collect(Collectors.toList());
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
