package com.project.mog.service.healthConnect;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.mog.repository.healthConnect.HealthConnectEntity;
import com.project.mog.repository.healthConnect.HealthConnectRepository;
import com.project.mog.repository.healthConnect.HeartRateDataEntity;
import com.project.mog.repository.healthConnect.StepDataEntity;
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
		HealthConnectEntity healthConnectEntity = new HealthConnectEntity();
		healthConnectEntity.setUser(user);
		healthConnectEntity.setCaloriesBurnedData(heDto.getCaloriesBurnedData());
		healthConnectEntity.setDistanceWalked(heDto.getDistanceWalked());
		healthConnectEntity.setActiveCaloriesBurned(heDto.getActiveCaloriesBurned());
		healthConnectEntity.setTotalSleepMinutes(heDto.getTotalSleepMinutes());
		healthConnectEntity.setDeepSleepMinutes(heDto.getDeepSleepMinutes());
		healthConnectEntity.setRemSleepMinutes(heDto.getRemSleepMinutes());
		healthConnectEntity.setLightSleepMinutes(heDto.getLightSleepMinutes());
		healthConnectEntity.setCurrentTime(heDto.getCurrentTime());
		
		List<StepDataEntity> stepData = Optional.ofNullable(heDto.getStepData())
	            .orElse(List.of())
	            .stream()
	            .map(step -> new StepDataEntity(step, healthConnectEntity))
	            .collect(Collectors.toList());
		
	    List<HeartRateDataEntity> heartRates = Optional.ofNullable(heDto.getHeartRateData())
	            .orElse(List.of())
	            .stream()
	            .map(hrDto -> new HeartRateDataEntity(hrDto.getBpm(), hrDto.getTime(), healthConnectEntity))
	            .collect(Collectors.toList());
		
		healthConnectEntity.addStepData(stepData);
		healthConnectEntity.addHeartRateData(heartRates);	
		
		healthConnectRepository.save(healthConnectEntity);
		
		return new HealthConnectResponseDto("헬스커넥트 데이터를 성공적으로 수신 및 저장하였습니다.");
		
		
	}
	
	@Transactional(readOnly = true)
	public List<HealthConnectDto> getHealthConnectData(Long usersId){
		UsersEntity user = usersRepository.findById(usersId)
				.orElseThrow(()-> new EntityNotFoundException("사용자를 찾을 수 없습니다. usersID: "+usersId));
		List<HealthConnectEntity> entities = healthConnectRepository.findByUser_usersId(usersId);
		return entities.stream()
				.map(this::convertToDto)
				.collect(Collectors.toList());
	}
	
	
	@Transactional
	public void deleteHealthConnectDataByUsersId(Long usersId) {
		UsersEntity user = usersRepository.findById(usersId)
				.orElseThrow(()-> new EntityNotFoundException("사용자를 찾을 수 없습니다. usersID: "+usersId));
		List<HealthConnectEntity> entities = healthConnectRepository.findByUser_usersId(usersId);
		healthConnectRepository.deleteAll(entities);
	}
	
	@Transactional
	public void deleteHealthConnectDataByUsersIdAndHealthId(Long usersId, Long healthId) {
		UsersEntity user = usersRepository.findById(usersId)
				.orElseThrow(()-> new EntityNotFoundException("사용자를 찾을 수 없습니다. usersID: "+usersId));
		HealthConnectEntity entity = healthConnectRepository.findByUsersAndHealth(usersId,healthId);
		healthConnectRepository.delete(entity);
	}
	
	
	private HealthConnectDto convertToDto(HealthConnectEntity entity) {
		List<Integer> stepData = entity.getStepData().stream()
				.map(StepDataEntity::getStepCount)
				.collect(Collectors.toList());
		List<HealthConnectDto.HeartRateDataDto> heartRateData = entity.getHeartRateData().stream()
				.map(hrEntity -> new HealthConnectDto.HeartRateDataDto(hrEntity.getBpm(),hrEntity.getTime()))
				.collect(Collectors.toList());
		
		return HealthConnectDto.builder()
				.healthId(entity.getId())
				.stepData(stepData)
				.heartRateData(heartRateData)
				.caloriesBurnedData(entity.getCaloriesBurnedData())
				.distanceWalked(entity.getDistanceWalked())
				.activeCaloriesBurned(entity.getActiveCaloriesBurned())
				.totalSleepMinutes(entity.getTotalSleepMinutes())
				.deepSleepMinutes(entity.getDeepSleepMinutes())
				.remSleepMinutes(entity.getRemSleepMinutes())
				.lightSleepMinutes(entity.getLightSleepMinutes())
				.currentTime(entity.getCurrentTime())
				.build();
	}
	

}
