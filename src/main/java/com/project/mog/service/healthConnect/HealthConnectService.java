package com.project.mog.service.healthConnect;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.mog.repository.healthConnect.HealthConnectEntity;
import com.project.mog.repository.healthConnect.HealthConnectRepository;
import com.project.mog.repository.healthConnect.HeartRateDataEntity;
import com.project.mog.repository.users.UsersEntity;
import com.project.mog.repository.users.UsersRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class HealthConnectService {
	
	private final HealthConnectRepository healthConnectRepository;
	private final UsersRepository usersRepository;
	
	@Transactional
	public HealthConnectResponseDto saveHealthConnect(HealthConnectDto heDto, String email) {
		UsersEntity user = usersRepository.findByEmail(email)
				.orElseThrow(()-> new EntityNotFoundException("사용자를 찾을 수 없습니다. ID: "+email));
		
		HealthConnectEntity healthConnectEntity = new HealthConnectEntity(
				user,
				heDto.getStepData(),
				heDto.getCaloriesBurnedData(),
				heDto.getDistanceWalked(),
				heDto.getActiveCaloriesBurned(),
				heDto.getTotalSleepMinutes(),
				heDto.getDeepSleepMinutes(),
				heDto.getRemSleepMinutes(),
				heDto.getLightSleepMinutes()
				);
		
		List<HeartRateDataEntity> heartRates = heDto.getHeartRateData().stream()
				.map(hrDto -> new HeartRateDataEntity(hrDto.getBpm(), hrDto.getTime()))
				.collect(Collectors.toList());
		
		healthConnectEntity.addHeartRateData(heartRates);
		healthConnectRepository.save(healthConnectEntity);
		
		return new HealthConnectResponseDto("헬스커넥트 데이터를 성공적으로 수신하였습니다.");
	}

}
