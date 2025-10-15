package com.project.mog.service.users;

import java.time.LocalDateTime;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
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
import com.project.mog.repository.like.LikeRepository;
import com.project.mog.repository.payment.OrderRepository;
import com.project.mog.repository.payment.PaymentRepository;
import com.project.mog.repository.users.UsersEntity;
import com.project.mog.repository.users.UsersRepository;
import com.project.mog.service.bios.BiosDto;
import com.project.mog.service.comment.CommentService;
import com.project.mog.service.healthConnect.HealthConnectService;
import com.project.mog.service.mail.SendPasswordRequest;
import com.project.mog.service.post.PostService;
import com.project.mog.service.routine.RoutineService;
import com.project.mog.repository.routine.RoutineRepository;
import com.project.mog.repository.routine.RoutineEndTotalRepository;
import com.project.mog.repository.routine.RoutineEndTotalEntity;
import com.project.mog.repository.routine.RoutineEntity;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import com.project.mog.repository.role.RoleAssignmentEntity;
import com.project.mog.repository.role.RoleAssignmentRepository;
import com.project.mog.repository.role.RolesEntity;
import com.project.mog.repository.role.RolesRepository;
import com.project.mog.service.role.RoleAssignmentDto;
import com.project.mog.service.role.RolesDto;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
public class UsersService {

		private UsersRepository usersRepository;
		private BiosRepository biosRepository;
		private AuthRepository authRepository;
		private KakaoApiClient kakaoApiClient;
		private RolesRepository rolesRepository;
		private HealthConnectService healthConnectService;
		private PostService postService;
		private RoutineRepository routineRepository;
		private RoutineEndTotalRepository routineEndTotalRepository;
		private RoleAssignmentRepository roleAssignmentRepository;
		
		

		public UsersService(UsersRepository usersRepository, 
							BiosRepository biosRepository,
							AuthRepository authRepository, 
							KakaoApiClient kakaoApiClient, 
							PasswordEncoder passwordEncoder, 
							HealthConnectService healthConnectService,
						PostService postService,
							PaymentRepository paymentRepository,
							OrderRepository orderRepository,
						RolesRepository rolesRepository,
						RoutineRepository routineRepository,
						RoutineEndTotalRepository routineEndTotalRepository,
						RoleAssignmentRepository roleAssignmentRepository) {
			this.usersRepository=usersRepository;
			this.biosRepository=biosRepository;
			this.authRepository=authRepository;
			this.kakaoApiClient=kakaoApiClient;
			this.rolesRepository=rolesRepository;
			this.healthConnectService=healthConnectService;
			this.postService=postService;
			this.routineRepository=routineRepository;
			this.routineEndTotalRepository=routineEndTotalRepository;
			this.roleAssignmentRepository=roleAssignmentRepository;
		}


		public List<UsersInfoDto> getAllUsers() {
			
			return usersRepository.findAll().stream().map(UsersInfoDto::toDto).collect(Collectors.toList());
		}


		public UsersDto createUser(UsersDto usersDto) {
			UsersEntity isDuplicated = usersRepository.findByEmail(usersDto.getEmail()).orElse(null);
			RolesEntity role = rolesRepository.findByRoleName("USER").orElse(null);
			
			if(isDuplicated!=null) throw new IllegalArgumentException("중복된 아이디입니다");

			UsersEntity uEntity = usersDto.toEntity();

			RoleAssignmentEntity roleAssignment = RoleAssignmentEntity.builder()
					.isActive(1L)
					.assignedAt(LocalDateTime.now())
					.expiredAt(LocalDateTime.now().plusDays(30))
					.role(role)
					.user(uEntity)
					.build();

			uEntity.getRoleAssignments().add(roleAssignment);
			usersRepository.save(uEntity); // cascade로 RoleAssignment도 함께 저장

			return UsersDto.toDto(uEntity);

		}


		public UsersInfoDto getUser(Long usersId) {
			return usersRepository.findById(usersId).map(uEntity->UsersInfoDto.toDto(uEntity)).orElseThrow(()->new IllegalArgumentException("사용자를 찾을 수 없습니다"));
		}
		
		public UsersInfoDto getUserByEmail(String email) {
			return usersRepository.findByEmail(email).map(uEntity->UsersInfoDto.toDto(uEntity)).orElseThrow(()->new IllegalArgumentException("사용자를 찾을 수 없습니다"));
		}

	public UsersInfoDto deleteUser(Long usersId, String authEmail) {
		// 권한 확인이 필요한 부분이므로 역할 정보까지 포함한 조회 사용
		UsersEntity currentUser = usersRepository.findByEmailWithRole(authEmail)
			.orElseThrow(() -> new IllegalArgumentException("유효하지 않은 사용자입니다"));
			
			// 삭제할 사용자 정보 조회
			UsersEntity targetUser = usersRepository.findById(usersId)
				.orElseThrow(() -> new RuntimeException("삭제할 사용자를 찾을 수 없습니다"));
			
			// 권한 검증: SUPER_ADMIN이거나 자기 자신인 경우만 삭제 가능
			if (!currentUser.getRoleAssignments().stream().map(RoleAssignmentEntity::getRole).map(RolesEntity::getRoleName).collect(Collectors.toList()).contains("SUPER_ADMIN") && currentUser.getUsersId() != usersId) {
				throw new AccessDeniedException("자기 자신만 삭제 가능합니다");
			}
			
			// SUPER_ADMIN은 자기 자신을 삭제할 수 없음
			if (currentUser.getRoleAssignments().stream().map(RoleAssignmentEntity::getRole).map(RolesEntity::getRoleName).collect(Collectors.toList()).contains("SUPER_ADMIN") && currentUser.getUsersId() == usersId) {
				throw new AccessDeniedException("최고 관리자는 자기 자신을 삭제할 수 없습니다");
			}

			//user삭제 전 연결되어있는 데이터 먼저 삭제
			healthConnectService.deleteHealthConnectDataByUsersId(usersId); //healthConnect삭제로 연결되어있는 heartRateData,StepData 함께 삭제
			postService.deleteByUsersId(usersId); //post삭제로 연결되어있는 comment,like 함께 삭제
			
			usersRepository.deleteById(usersId);
			return UsersInfoDto.toDto(targetUser);
		}

		public UsersInfoDto editUser(UsersInfoDto usersInfoDto, Long usersId, String authEmail) {		
			// 수정할 사용자 정보 조회
			UsersEntity usersEntity = usersRepository.findById(usersId)
				.orElseThrow(() -> new IllegalArgumentException(usersId + "가 존재하지 않습니다"));
			if (!authEmail.equals(usersEntity.getEmail())) {
				throw new IllegalArgumentException("인가되지 않은 사용자입니다");
			}
			BiosEntity biosEntity = biosRepository.findByUser(usersEntity);
			if(biosEntity==null&usersInfoDto.getBiosDto()!=null) {
				BiosEntity newBiosEntity = BiosEntity.builder()
						.age(usersInfoDto.getBiosDto().getAge())
						.gender(usersInfoDto.getBiosDto().isGender())
						.height(usersInfoDto.getBiosDto().getHeight())
						.weight(usersInfoDto.getBiosDto().getWeight())
						.build();
				return usersInfoDto.applyTo(usersEntity, newBiosEntity);
			}
			return usersInfoDto.applyTo(usersEntity, biosEntity);
		}

		public UsersDto login(LoginRequest request) {
		// 1. 이메일로 사용자 찾기
		UsersEntity usersEntity = usersRepository.findByEmail(request.getEmail())
			.orElseThrow(() -> new IllegalArgumentException("올바르지 않은 아이디/비밀번호입니다"));
		
		System.out.println(usersEntity.getAuth().isPasswordless());
		
		// 2. 패스워드리스 등록됐을 경우 반환
		if(usersEntity.getAuth().isPasswordless()==true) {
			throw new AccessDeniedException("패스워드리스로 등록된 계정입니다");
		}
		
		// 3. 비밀번호 검증 (평문 비밀번호와 비교)
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

		private String generateTempPassword() {
			return UUID.randomUUID().toString().substring(0,8);
		}
		
		public String updatePasswordToTemp(SendPasswordRequest request) {
			UsersEntity user = usersRepository.findByEmail(request.getEmail())
					.orElseThrow(()-> new IllegalArgumentException("사용자를 찾을 수 없습니다"));
			
			String tempPassword = generateTempPassword();
			AuthEntity authEntity = user.getAuth();
			authEntity.setPassword(tempPassword);
			return tempPassword;
		}

		public UsersInfoDto getUserByRequest(EmailFindRequest emailFindRequest) {
			UsersEntity usersEntity = usersRepository.findByUsersNameAndPhoneNum(emailFindRequest.getUsersName(),emailFindRequest.getPhoneNum()).orElseThrow(()->new IllegalArgumentException("사용자를 찾을 수 없습니다"));
			return UsersInfoDto.toDto(usersEntity);
		}

		public UsersDto registerPasswordless(String authEmail, String passwordlessToken) throws JsonProcessingException, NoSuchAlgorithmException {
			UsersEntity usersEntity = usersRepository.findByEmail(authEmail).orElseThrow(()->new IllegalArgumentException("사용자를 찾을 수 없습니다"));
			AuthEntity authEntity = authRepository.findByUser(usersEntity);
			
			// 패스워드리스 등록 후 로그인 불가능하도록 해쉬화한 패스워드리스토큰을 비밀번호로 저장
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hash = digest.digest(passwordlessToken.getBytes(StandardCharsets.UTF_8));
			StringBuilder sb = new StringBuilder();
			for (byte b : hash) {
			    sb.append(String.format("%02x", b));
			}
			String hexHash = sb.toString();
			BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
			String bcryptHash = encoder.encode(hexHash);
			authEntity.setPassword(bcryptHash);
			authEntity.setPasswordless(true);
			return UsersDto.toDto(usersEntity);
			
			
		}


		public UsersDto loginPasswordless(String email, String passwordlessToken) throws NoSuchAlgorithmException {
			UsersEntity usersEntity = usersRepository.findByEmail(email).orElseThrow(()->new IllegalArgumentException("사용자를 찾을 수 없습니다"));
			AuthEntity authEntity = authRepository.findByUser(usersEntity);
			
			//패스워드리스 로그인 후 해쉬화한 패스워드리스토큰으로 재설정
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hash = digest.digest(passwordlessToken.getBytes(StandardCharsets.UTF_8));
			StringBuilder sb = new StringBuilder();
			for (byte b : hash) {
			    sb.append(String.format("%02x", b));
			}
			String hexHash = sb.toString();
			BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
			String bcryptHash = encoder.encode(hexHash);
			authEntity.setPassword(bcryptHash);
			authEntity.setPasswordless(true);
			
			return UsersDto.toDto(usersEntity);
		}

		// ===== Home APIs =====
		public HomeStatsDto getHomeStats(String authEmail) {
			UsersEntity user = usersRepository.findByEmail(authEmail).orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));
			List<RoutineEntity> routines = routineRepository.findByUsersId(user.getUsersId());
			int totalRoutines = routines.size();
			long totalMinutes = routines.stream()
					.flatMap(r -> routineEndTotalRepository.findAllBySetId(r.getSetId()).stream())
					.mapToLong(ret -> java.time.Duration.between(ret.getTStart(), ret.getTEnd()).toMinutes())
					.sum();
			// 간단한 연속일수 계산: 오늘로부터 역순으로 한 건이라도 기록이 있으면 +1 (최대 30)
			java.time.LocalDate today = java.time.LocalDate.now();
			int consecutive = 0;
			for (int i = 0; i < 30; i++) {
				java.time.LocalDate d = today.minusDays(i);
				boolean has = routines.stream().anyMatch(r -> routineEndTotalRepository.findAllBySetId(r.getSetId()).stream()
						.anyMatch(ret -> ret.getTEnd() != null && ret.getTEnd().toLocalDate().equals(d)));
				if (has) consecutive++; else break;
			}
			String totalTime = (totalMinutes / 60) > 0 ? (totalMinutes / 60) + "시간 " + (totalMinutes % 60) + "분" : totalMinutes + "분";
			String consecutiveMessage = consecutive == 0 ? "지금 시작해보세요" : "연속 달성";
			String routinesMessage = totalRoutines == 0 ? "루틴을 만들어 보세요" : "총 루틴";
			String timeMessage = totalMinutes == 0 ? "지금 시작해보세요" : "총 시간";
			return HomeStatsDto.builder()
					.consecutiveDays(consecutive)
					.totalRoutines(totalRoutines)
					.totalTime(totalTime)
					.consecutiveMessage(consecutiveMessage)
					.routinesMessage(routinesMessage)
					.timeMessage(timeMessage)
					.build();
		}

		public java.util.List<HomeRoutineItemDto> getHomeRoutines(String authEmail) {
			UsersEntity user = usersRepository.findByEmail(authEmail).orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));
			List<RoutineEntity> routines = routineRepository.findByUsersId(user.getUsersId());
			// 사용자 전체 루틴 로그가 하나도 없으면 빈 배열 반환 (프런트에 카드 숨김 신호)
			boolean hasAnyLog = routines.stream()
					.flatMap(r -> routineEndTotalRepository.findAllBySetId(r.getSetId()).stream())
					.findAny()
					.isPresent();
			if (!hasAnyLog) {
				return java.util.List.of();
			}
			return routines.stream().map(r -> {
				List<RoutineEndTotalEntity> logs = routineEndTotalRepository.findAllBySetId(r.getSetId());
				boolean completedToday = logs.stream().anyMatch(ret -> ret.getTEnd() != null && ret.getTEnd().toLocalDate().equals(java.time.LocalDate.now()));
				return HomeRoutineItemDto.builder()
					.id(r.getSetId())
					.title(r.getRoutineName())
					.time("15분")
					.difficulty("보통")
					.icon("💪")
					.completed(completedToday)
					.build();
			}).collect(java.util.stream.Collectors.toList());
		}


        public void saveWebPushToken(String authEmail, String webPushToken) {
            UsersEntity usersEntity = usersRepository.findByEmail(authEmail).orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));
            AuthEntity authEntity = usersEntity.getAuth();
			System.out.println(webPushToken);
			authEntity.setWebPushToken(webPushToken);
        }


		public List<UsersEntity> getUsersByRole(String string) {
			return usersRepository.findByRoleAssignmentsRoleRoleName(string);
		}
		
}
