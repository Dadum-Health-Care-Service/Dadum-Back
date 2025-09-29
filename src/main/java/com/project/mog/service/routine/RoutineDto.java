package com.project.mog.service.routine;

import java.util.List;
import java.util.stream.Collectors;

import com.project.mog.repository.routine.RoutineEntity;
import com.project.mog.repository.routine.SaveRoutineEntity;
import com.project.mog.service.bios.BiosDto;
import com.project.mog.service.users.UsersDto;

import io.swagger.v3.oas.annotations.media.Schema;
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
public class RoutineDto {
	@Schema(hidden = true)
	private long setId;
	private String routineName;
	@Schema(hidden = true)
	private long usersId;
	private List<SaveRoutineDto> saveRoutineDto;
	
	
	public static RoutineDto toDto(RoutineEntity rEntity) {
		if(rEntity==null) return null;
		List<SaveRoutineEntity> srEntity = rEntity.getSaveRoutine();
		List<SaveRoutineDto> srDto;
		if(srEntity==null) {
			srDto = List.of();
		}
		else {
			srDto = srEntity.stream().map(SaveRoutineDto::toDto).collect(Collectors.toList());
			
		}
		return RoutineDto.builder()
				.setId(rEntity.getSetId())
				.routineName(rEntity.getRoutineName())
				.usersId(rEntity.getUser().getUsersId())
				.saveRoutineDto(srDto)
				.build();
	}
}
