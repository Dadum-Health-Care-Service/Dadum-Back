package com.project.mog.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.project.mog.repository.users.UsersEntity;
import com.project.mog.repository.users.UsersRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UsersDetailService implements UserDetailsService{

	private final UsersRepository usersRepository;

	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException{
			// 권한 확인이 필요한 보안 로직이므로 역할 정보까지 포함한 조회 사용
			UsersEntity users = usersRepository.findByEmailWithRole(email).orElseThrow(()->new IllegalArgumentException("유효하지 않은 사용자입니다"));
			if(users==null) throw new UsernameNotFoundException("계정을 찾을 수 없습니다");
			return new UsersDetails(users);
		
	}
	
	
	
}
