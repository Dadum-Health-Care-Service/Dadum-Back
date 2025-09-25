package com.project.mog.service.routine;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.project.mog.annotation.UserAuthorizationCheck;
import com.project.mog.controller.routine.RoutineEndTotalRequest;
import com.project.mog.repository.routine.RoutineEndDetailEntity;
import com.project.mog.repository.routine.RoutineEndDetailRepository;
import com.project.mog.repository.routine.RoutineEndTotalEntity;
import com.project.mog.repository.routine.RoutineEndTotalRepository;
import com.project.mog.repository.routine.RoutineEntity;
import com.project.mog.repository.routine.RoutineRepository;
import com.project.mog.repository.routine.RoutineResultEntity;
import com.project.mog.repository.routine.RoutineResultRepository;
import com.project.mog.repository.routine.SaveRoutineEntity;
import com.project.mog.repository.routine.SaveRoutineRepository;
import com.project.mog.repository.routine.SaveRoutineSetEntity;
import com.project.mog.repository.routine.SaveRoutineSetRepository;
import com.project.mog.repository.users.UsersEntity;
import com.project.mog.repository.users.UsersRepository;
import com.project.mog.service.users.UsersDto;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RoutineService {
	private final EntityManager entityManager;
	private final UsersRepository usersRepository;
	private final RoutineRepository routineRepository;
	private final SaveRoutineRepository saveRoutineRepository;
	private final SaveRoutineSetRepository saveRoutineSetRepository;
	private final RoutineEndTotalRepository routineEndTotalRepository;
	private final RoutineEndDetailRepository routineEndDetailRepository;
	private final RoutineResultRepository routineResultRepository;

	public static class RoutineStartResponse {
		public boolean success;
		public String message;
		public Long routineId;
		public java.time.LocalDateTime startedAt;
		public RoutineStartResponse(boolean success, String message, Long routineId, java.time.LocalDateTime startedAt) {
			this.success = success;
			this.message = message;
			this.routineId = routineId;
			this.startedAt = startedAt;
		}
	}
	
	public RoutineEntity toEntity(UsersEntity uEntity, RoutineDto routineDto) {
		RoutineEntity rEntity = RoutineEntity.builder()
								.routineName(routineDto.getRoutineName())
								.user(uEntity)
								.build();
		rEntity.setSaveRoutine(routineDto.getSaveRoutineDto().stream().map(srDto->{
			SaveRoutineEntity srEntity = srDto.toEntity(rEntity);
			srEntity.setSaveRoutineSet(srDto.getSet().stream().map(srs->{
				return SaveRoutineSetEntity.builder().weight(srs.getWeight()).many(srs.getMany()).saveRoutine(srEntity).build();
			}).toList());
			return srEntity;
			}).collect(Collectors.toList()));
		return rEntity;
	}
	public RoutineEndTotalEntity toEntity(RoutineEndTotalDto retDto, Long setId) {
		RoutineEntity rEntity = routineRepository.findById(setId).orElseThrow(()->new IllegalArgumentException("루틴을 찾을 수 없습니다"));
		RoutineEndTotalEntity retEntity = RoutineEndTotalEntity.builder()
				.routineResult(retDto.getRoutineResult().toEntity())
				.build();
		List<RoutineEndDetailEntity> redEntity = retDto.getRoutineEndDetails().stream()
													.map(detailDto -> detailDto.toEntity(retEntity))
													.collect(Collectors.toList());
		retEntity.setRoutineEndDetail(redEntity);
		return retEntity;
	}
	

	public List<RoutineDto> getAllRoutines(String authEmail) {
		UsersEntity currentUser = usersRepository.findByEmail(authEmail).orElseThrow(()->new IllegalArgumentException("유효하지 않은 사용자입니다"));
		return routineRepository.findByUsersId(currentUser.getUsersId()).stream().map(RoutineDto::toDto).collect(Collectors.toList());
	}
	
	public RoutineDto getRoutine(String authEmail, Long setId) {
		UsersEntity currentUser = usersRepository.findByEmail(authEmail).orElseThrow(()->new IllegalArgumentException("유효하지 않은 사용자입니다"));
		RoutineEntity routineEntity = routineRepository.findByUsersIdAndSetId(currentUser.getUsersId(),setId).orElseThrow(()->new IllegalArgumentException("해당 루틴 정보가 없습니다"));
		List<SaveRoutineEntity> saveRoutineEntity = saveRoutineRepository.findAllBySetId(setId);
		routineEntity.setSaveRoutine(saveRoutineEntity);
		return RoutineDto.toDto(routineEntity); 
	}

	public RoutineDto createRoutine(String authEmail, RoutineDto routineDto) {
		UsersEntity uEntity = usersRepository.findByEmail(authEmail).orElseThrow(()->new IllegalArgumentException("유효하지 않은 사용자입니다"));
		RoutineEntity routineEntity = routineRepository.save(toEntity(uEntity,routineDto));
		return RoutineDto.toDto(routineEntity);
	}
	
	public RoutineDto updateRoutine(String authEmail,Long setId, RoutineDto routineDto) {
		UsersEntity uEntity = usersRepository.findByEmail(authEmail)
			    .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 사용자입니다"));

			RoutineEntity routineEntity = routineRepository.findById(routineDto.getSetId())
			    .orElseThrow(() -> new IllegalArgumentException("루틴이 존재하지 않습니다"));

			// 루틴 이름 업데이트
			routineEntity.setRoutineName(routineDto.getRoutineName());
			List<SaveRoutineDto> saveRoutineDtos = routineDto.getSaveRoutineDto();
			//수정할 SaveRoutine 매핑
			List<SaveRoutineEntity> saveRoutineEntities = routineEntity.getSaveRoutine();
			Map<Long, SaveRoutineEntity> saveRoutineEntityMap = saveRoutineEntities.stream()
				    .collect(Collectors.toMap(SaveRoutineEntity::getSrId, Function.identity()));
			for (SaveRoutineDto srDto:saveRoutineDtos) {
				//매치된 SaveRoutine
				SaveRoutineEntity matchedSaveRoutineEntity = routineEntity.getSaveRoutine().stream().filter(entity->entity.getSrId()==srDto.getSrId()).findFirst().orElse(null);
				//매치된 SaveRoutine 존재시
				if(matchedSaveRoutineEntity!=null) {
					matchedSaveRoutineEntity.setSrName(srDto.getSrName());
					matchedSaveRoutineEntity.setExId(srDto.getExId());
					matchedSaveRoutineEntity.setReps(srDto.getReps());
					List<SaveRoutineSetEntity> saveRoutineSetEntities = matchedSaveRoutineEntity.getSaveRoutineSet();
					//수정할 SaveRoutineSet 매핑
					Map<Long, SaveRoutineSetEntity> saveRoutineSetEntityMap = saveRoutineSetEntities.stream()
						    .collect(Collectors.toMap(SaveRoutineSetEntity::getSrsId, Function.identity()));
					List<SaveRoutineSetDto> saveRoutineSetDtos =  srDto.getSet();
					//매치된 SaveRoutineSet 재설정
					for(SaveRoutineSetDto srsDto:saveRoutineSetDtos) {
						//매치된 SaveRoutineSet
						SaveRoutineSetEntity matchedSaveRoutineSetEntity = matchedSaveRoutineEntity.getSaveRoutineSet().stream().filter(entity->entity.getSrsId()==srsDto.getSrsId()).findFirst().orElse(null);
						//매치된 SaveRoutineSet 존재시
						if(matchedSaveRoutineSetEntity!=null) {
							matchedSaveRoutineSetEntity.setMany(srsDto.getMany());
							matchedSaveRoutineSetEntity.setWeight(srsDto.getWeight());
							matchedSaveRoutineSetEntity.setSaveRoutine(matchedSaveRoutineEntity);
							//수정된 후 맵에서 삭제
							saveRoutineSetEntityMap.remove(srsDto.getSrsId());
						}
						//매치된 SaveRoutineSet 미존재시 추가
						else {
							SaveRoutineSetEntity srsEntity = srsDto.toEntity(matchedSaveRoutineEntity);
							srsEntity.setSaveRoutine(matchedSaveRoutineEntity);
							saveRoutineSetRepository.save(srsEntity);
						}
					}
					//SaveRoutineSetEntity 기준 재순회
					for(SaveRoutineSetEntity saveRoutineSetEntity:saveRoutineSetEntityMap.values() ) {
						//수정 이후맵에 남아있는 경우 삭제
						matchedSaveRoutineEntity.getSaveRoutineSet().remove(saveRoutineSetEntity);
						saveRoutineSetRepository.delete(saveRoutineSetEntity);
					}
					//SaveRoutineSet CRUD 후 루틴 부모 설정
					matchedSaveRoutineEntity.setRoutine(routineEntity);
					//수정된 후 맵에서 삭제
					saveRoutineEntityMap.remove(srDto.getSrId());
				}
				//매치된 SaveRoutine 미존재시 추가
				else {
					srDto.getSet().forEach(srs->System.out.println("SRS"+"id:"+srs.getSrsId()+"many:"+srs.getMany()));
					SaveRoutineEntity srEntity = srDto.toEntity(routineEntity);
					List<SaveRoutineSetEntity> srsEntities = srDto.getSet().stream().map(srs -> srs.toEntity(srEntity)).toList();
					srsEntities.forEach(srs->saveRoutineSetRepository.save(srs));
					srEntity.setSaveRoutineSet(srsEntities);
					saveRoutineSetRepository.saveAll(srsEntities);
					saveRoutineRepository.save(srEntity);
				}
				//SaveRoutineEntity 기준 재순회
				for(SaveRoutineEntity saveRoutineEntity:saveRoutineEntityMap.values() ) {
					//수정 이후맵에 남아있는 경우 삭제
					routineEntity.getSaveRoutine().remove(saveRoutineEntity);
					saveRoutineRepository.delete(saveRoutineEntity);
				}
			}
			entityManager.flush();
			entityManager.clear();
			RoutineEntity afterRoutineEntity = routineRepository.findById(routineDto.getSetId())
				    .orElseThrow(() -> new IllegalArgumentException("루틴이 존재하지 않습니다"));
			return RoutineDto.toDto(afterRoutineEntity);

	}
	
	public SaveRoutineDto getSaveRoutine(Long setId, Long srId) {
		SaveRoutineEntity saveRoutineEntity = saveRoutineRepository.findBySetIdAndSrId(setId,srId);
		
		return SaveRoutineDto.toDto(saveRoutineEntity);
	}

	
	public SaveRoutineDto createSaveRoutine(SaveRoutineDto saveRoutineDto, Long setId) {
		RoutineEntity routineEntity = routineRepository.findById(setId).orElseThrow(()->new IllegalArgumentException("루틴을 찾을 수 없습니다"));
		SaveRoutineEntity saveRoutineEntity = saveRoutineDto.toEntity(routineEntity);
		saveRoutineRepository.save(saveRoutineEntity);
		return SaveRoutineDto.toDto(saveRoutineEntity);
	}

	public SaveRoutineDto deleteSaveRoutine(Long srId) {
		SaveRoutineEntity saveRoutineEntity = saveRoutineRepository.findById(srId).orElseThrow(()->new IllegalArgumentException("삭제할 루틴 상세를 찾을 수 없습니다"));
		saveRoutineRepository.deleteById(srId);
		return SaveRoutineDto.toDto(saveRoutineEntity);
	}

	public RoutineEndTotalDto createRoutineEndTotal(RoutineEndTotalDto retDto, Long setId) {
		RoutineEntity routineEntity = routineRepository.findById(setId).orElseThrow(()->new IllegalArgumentException("루틴을 찾을 수 없습니다"));
		
		RoutineResultEntity rrEntity = retDto.getRoutineResult()!=null?retDto.getRoutineResult().toEntity():null;
		if(rrEntity!=null)routineResultRepository.save(rrEntity);
		RoutineEndTotalEntity retEntity = RoutineEndTotalEntity.builder()
											.tStart(retDto.getTStart())
											.tEnd(retDto.getTEnd())
											.routine(routineEntity)
											.routineResult(rrEntity)
											.build();
		List<RoutineEndDetailEntity> redEntity = retDto.getRoutineEndDetails().stream().map(red->{red.setRoutineEndTotalEntity(retEntity);return red.toEntity(retEntity);}).collect(Collectors.toList());
		retEntity.setRoutineEndDetail(redEntity);
		routineEndTotalRepository.save(retEntity);
		return RoutineEndTotalDto.toDto(retEntity);
	}
	
	public List<RoutineEndTotalDto> getRoutineEndTotal(String authEmail, RoutineEndTotalRequest routineEndTotalRequest) {
		UsersEntity userEntity = usersRepository.findByEmail(authEmail).orElseThrow(()->new IllegalArgumentException("유효하지 않은 사용자입니다"));
		List<RoutineEntity> routineEntities = routineRepository.findByUsersId(userEntity.getUsersId());
		List<RoutineEndTotalEntity> routineEndTotalEntities = routineEndTotalRequest==null?
																routineEntities.stream().flatMap(routine->routineEndTotalRepository.findAllBySetId(routine.getSetId()).stream()).collect(Collectors.toList())
																:routineEntities.stream().flatMap(routine->routineEndTotalRepository.findAllBySetIdAndDateBetween(routine.getSetId(),routineEndTotalRequest.getStartDate(),routineEndTotalRequest.getEndDate()).stream()).collect(Collectors.toList());
				
		return routineEndTotalEntities.stream().map(RoutineEndTotalDto::toDto).collect(Collectors.toList());
	}
	public RoutineDto deleteRoutine(String authEmail, Long setId) {
		UsersEntity userEntity = usersRepository.findByEmail(authEmail).orElseThrow(()->new IllegalArgumentException("유효하지 않은 사용자입니다"));
		RoutineEntity routineEntity = routineRepository.findByUsersIdAndSetId(userEntity.getUsersId(),setId).orElseThrow(()-> new IllegalArgumentException("루틴을 찾을 수 없습니다"));
		routineRepository.delete(routineEntity);
		return RoutineDto.toDto(routineEntity);
	}

	@Transactional
	public RoutineStartResponse startRoutine(String authEmail, Long routineId) {
		UsersEntity user = usersRepository.findByEmail(authEmail).orElseThrow(() -> new IllegalArgumentException("유효하지 않은 사용자입니다"));
		RoutineEntity routine = routineRepository.findByUsersIdAndSetId(user.getUsersId(), routineId)
				.orElseThrow(() -> new IllegalArgumentException("루틴을 찾을 수 없습니다"));
		RoutineEndTotalEntity ret = RoutineEndTotalEntity.builder()
				.tStart(java.time.LocalDateTime.now())
				.routine(routine)
				.build();
		routineEndTotalRepository.save(ret);
		return new RoutineStartResponse(true, "루틴이 시작되었습니다", routineId, ret.getTStart());
	}
	
	public void deleteRoutineByUsersId(Long usersId) {
		UsersEntity userEntity = usersRepository.findById(usersId).orElseThrow(()-> new IllegalArgumentException("유효하지 않은 사용자입니다"));
		List<RoutineEntity> routineEntities = routineRepository.findByUsersId(usersId);
		routineRepository.deleteAll(routineEntities);
	}
	
	
}
