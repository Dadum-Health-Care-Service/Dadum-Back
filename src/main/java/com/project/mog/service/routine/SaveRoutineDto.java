package com.project.mog.service.routine;

import java.util.List;
import java.util.stream.Collectors;

import com.project.mog.repository.routine.RoutineEntity;
import com.project.mog.repository.routine.SaveRoutineEntity;
import com.project.mog.repository.routine.SaveRoutineSetEntity;

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
@Schema(description = "저장된 루틴 DTO")
public class SaveRoutineDto {
	@Schema(hidden = true)
	private long srId;
	private long exId;
	private String srName;
	private long reps;
	@Schema(hidden = true)
	private long setId;
	private List<SaveRoutineSetDto> set;
	
	public SaveRoutineEntity toEntity(RoutineEntity rEntity) {
		SaveRoutineEntity srEntity = SaveRoutineEntity.builder()
				.exId(exId)
				.srName(srName)
				.reps(reps)
				.routine(rEntity)
				.build();
		List<SaveRoutineSetEntity> saveRoutineSets = set.stream().map(srs->srs.toEntity(srEntity)).toList();
		srEntity.setSaveRoutineSet(saveRoutineSets);
		
		return srEntity;
	}
	
	
	
	public static SaveRoutineDto toDto(SaveRoutineEntity srEntity){
	    if (srEntity == null) return null;

	    return SaveRoutineDto.builder()
	        .srId(srEntity.getSrId())
	        .exId(srEntity.getExId())
	        .srName(srEntity.getSrName())
	        .set(srEntity.getSaveRoutineSet().stream().map(srs->SaveRoutineSetDto.toDto(srs)).toList())
	        .reps(srEntity.getReps())
	        .setId(srEntity.getRoutine().getSetId())
	        .build();
	}
}
