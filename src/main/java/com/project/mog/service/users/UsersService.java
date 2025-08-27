package com.project.mog.service.users;


import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.project.mog.annotation.UserAuthorizationCheck;
import com.project.mog.api.KakaoApiClient;
import com.project.mog.controller.auth.EmailFindRequest;
import com.project.mog.controller.login.LoginRequest;
import com.project.mog.controller.login.LoginResponse;
import com.project.mog.controller.login.SocialLoginRequest;
import com.project.mog.repository.auth.AuthEntity;
import com.project.mog.repository.auth.AuthRepository;
import com.project.mog.repository.bios.BiosEntity;
import com.project.mog.repository.bios.BiosRepository;
import com.project.mog.repository.users.UsersEntity;
import com.project.mog.repository.users.UsersRepository;
import com.project.mog.service.bios.BiosDto;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
public class UsersService {

		private UsersRepository usersRepository;
		private BiosRepository biosRepository;
		private AuthRepository authRepository;
		private KakaoApiClient kakaoApiClient;
		private PasswordEncoder passwordEncoder;
		
		
		
		public UsersService(UsersRepository usersRepository, BiosRepository biosRepository,AuthRepository authRepository, KakaoApiClient kakaoApiClient, PasswordEncoder passwordEncoder ) {
			this.usersRepository=usersRepository;
			this.biosRepository=biosRepository;
			this.authRepository=authRepository;
			this.kakaoApiClient=kakaoApiClient;
			this.passwordEncoder=passwordEncoder;
		}


		public List<UsersInfoDto> getAllUsers() {
			
			return usersRepository.findAll().stream().map(UsersInfoDto::toDto).collect(Collectors.toList());
		}


				public UsersDto createUser(UsersDto usersDto) {
		UsersEntity isDuplicated = usersRepository.findByEmail(usersDto.getEmail()).orElse(null);
		if(isDuplicated!=null) throw new IllegalArgumentException("중복된 아이디입니다");
		
		// 기본 역할을 USER로 설정
		if (usersDto.getRole() == null || usersDto.getRole().trim().isEmpty()) {
			usersDto.setRole("USER");
		}
		
		// UsersEntity로 변환 (비밀번호 암호화 없이)
		UsersEntity uEntity = usersDto.toEntity();
		
		UsersEntity savedEntity = usersRepository.save(uEntity);
			return UsersDto.toDto(uEntity);
		}


		public UsersInfoDto getUser(Long usersId) {
			return usersRepository.findById(usersId).map(uEntity->UsersInfoDto.toDto(uEntity)).orElseThrow(()->new IllegalArgumentException("사용자를 찾을 수 없습니다"));
		}
		
		public UsersInfoDto getUserByEmail(String email) {
			return usersRepository.findByEmail(email).map(uEntity->UsersInfoDto.toDto(uEntity)).orElseThrow(()->new IllegalArgumentException("사용자를 찾을 수 없습니다"));
		}

		public UsersInfoDto deleteUser(Long usersId, String authEmail) {
			// 현재 로그인한 사용자 정보 조회
			UsersEntity currentUser = usersRepository.findByEmail(authEmail)
				.orElseThrow(() -> new IllegalArgumentException("유효하지 않은 사용자입니다"));
			
			// 삭제할 사용자 정보 조회
			UsersEntity targetUser = usersRepository.findById(usersId)
				.orElseThrow(() -> new RuntimeException("삭제할 사용자를 찾을 수 없습니다"));
			
			// 권한 검증: SUPER_ADMIN이거나 자기 자신인 경우만 삭제 가능
			if (!currentUser.getRole().equals("SUPER_ADMIN") && currentUser.getUsersId() != usersId) {
				throw new AccessDeniedException("자기 자신만 삭제 가능합니다");
			}
			
			// SUPER_ADMIN은 자기 자신을 삭제할 수 없음
			if (currentUser.getRole().equals("SUPER_ADMIN") && currentUser.getUsersId() == usersId) {
				throw new AccessDeniedException("최고 관리자는 자기 자신을 삭제할 수 없습니다");
			}
			
			// ADMIN은 다른 ADMIN을 삭제할 수 없음
			if (currentUser.getRole().equals("ADMIN") && targetUser.getRole().equals("ADMIN")) {
				throw new AccessDeniedException("일반 관리자는 다른 관리자를 삭제할 수 없습니다");
			}
			
			usersRepository.deleteById(usersId);
			return UsersInfoDto.toDto(targetUser);
		}

		public UsersInfoDto editUser(UsersInfoDto usersInfoDto, Long usersId, String authEmail) {		
			// 현재 로그인한 사용자 정보 조회
			UsersEntity currentUser = usersRepository.findByEmail(authEmail)
				.orElseThrow(() -> new IllegalArgumentException("유효하지 않은 사용자입니다"));
			
			// 수정할 사용자 정보 조회
			UsersEntity usersEntity = usersRepository.findById(usersId)
				.orElseThrow(() -> new IllegalArgumentException(usersId + "가 존재하지 않습니다"));
			
			// 권한 검증: SUPER_ADMIN이거나 자기 자신인 경우만 수정 가능
			if (!currentUser.getRole().equals("SUPER_ADMIN") && currentUser.getUsersId() != usersId) {
				throw new AccessDeniedException("자기 자신만 수정 가능합니다");
			}
			
			// 역할 변경 권한 검증
			if (usersInfoDto.getRole() != null && !usersInfoDto.getRole().equals(usersEntity.getRole())) {
				// SUPER_ADMIN만 역할을 변경할 수 있음
				if (!currentUser.getRole().equals("SUPER_ADMIN")) {
					throw new AccessDeniedException("역할 변경은 최고 관리자만 가능합니다");
				}
				
				// SUPER_ADMIN은 다른 SUPER_ADMIN을 만들 수 없음
				if (usersInfoDto.getRole().equals("SUPER_ADMIN") && currentUser.getUsersId() != usersId) {
					throw new AccessDeniedException("다른 사용자를 최고 관리자로 만들 수 없습니다");
				}
			}
			
			BiosEntity biosEntity = biosRepository.findByUser(usersEntity);
			
			return usersInfoDto.applyTo(usersEntity, biosEntity);
		}

		public UsersDto login(LoginRequest request) {
		// 1. 이메일로 사용자 찾기
		UsersEntity usersEntity = usersRepository.findByEmail(request.getEmail())
			.orElseThrow(() -> new IllegalArgumentException("올바르지 않은 아이디/비밀번호입니다"));
		
		// 2. 비밀번호 검증 (평문 비밀번호와 비교)
		if (!request.getPassword().equals(usersEntity.getAuth().getPassword())) {
			throw new IllegalArgumentException("올바르지 않은 아이디/비밀번호입니다");
		}
		
		return UsersDto.toDto(usersEntity);
	}


		public UsersDto socialLogin(SocialLoginRequest request) {
			System.out.println("social?");
			System.out.println(request.getSocialType().equalsIgnoreCase("kakao"));
			if(request.getSocialType().equalsIgnoreCase("kakao")) {
				System.out.println("on kakao?");
				KakaoUser kakaoUser = kakaoApiClient.getUserInfo(request.getAccessToken());
				UsersEntity usersEntity = usersRepository.findByEmail(String.format("user%s@kakao.com", kakaoUser.getId())).orElse(null);
				if(usersEntity==null) {
					AuthEntity newKakaoAuth = AuthEntity.builder().password(request.getAccessToken()).build();
					UsersEntity newKakaoUser = UsersEntity.builder()
													.usersName(kakaoUser.getProperties().getNickname())
													.email("user"+kakaoUser.getId()+"@kakao.com")
													.profileImg(kakaoUser.getProperties().getProfile_image())
													.nickName(kakaoUser.getProperties().getNickname())
													.bios(null)
													.auth(newKakaoAuth)
													.phoneNum(kakaoUser.getId().toString())
													.build();
					return createUser(UsersDto.toDto(newKakaoUser));
				}
				return UsersDto.toDto(usersEntity);
			}
			
			return null;
		}


		public UsersDto checkPassword(String authEmail, String password) {
			UsersEntity usersEntity = usersRepository.findByEmailAndPassword(authEmail, password).orElseThrow(()->new IllegalArgumentException("사용자를 찾을 수 없습니다"));
			return UsersDto.toDto(usersEntity);
			
		}

		public UsersDto getPassword(String authEmail) {
			UsersEntity usersEntity = usersRepository.findByEmail(authEmail).orElseThrow(()->new IllegalArgumentException("사용자를 찾을 수 없습니다"));
			return UsersDto.toDto(usersEntity);
		}

		
		public UsersDto editPassword(String authEmail, String originPassword, String newPassword) {
			UsersEntity usersEntity = usersRepository.findByEmailAndPassword(authEmail, originPassword).orElseThrow(()->new IllegalArgumentException("사용자를 찾을 수 없습니다"));
			AuthEntity authEntity = usersEntity.getAuth();
			authEntity.setPassword(newPassword);
			return UsersDto.toDto(usersEntity);
		}


		public UsersInfoDto getUserByRequest(EmailFindRequest emailFindRequest) {
			UsersEntity usersEntity = usersRepository.findByUsersNameAndPhoneNum(emailFindRequest.getUsersName(),emailFindRequest.getPhoneNum()).orElseThrow(()->new IllegalArgumentException("사용자를 찾을 수 없습니다"));
			return UsersInfoDto.toDto(usersEntity);
		}


		
		
		
		
}
