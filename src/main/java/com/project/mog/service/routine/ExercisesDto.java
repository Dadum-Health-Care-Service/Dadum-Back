package com.project.mog.service.routine;

import com.project.mog.repository.routine.ExercisesEntity;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ExercisesDto {
	private Long id; //운동 아이디
	private String names;
	private String category;
	private String equipment;
	private String force;
	private String[] instrucions;
	private String level;
	private String mechanic;
	private String primaryMuscles;
	private String secondaryMuscles;
	
	public static ExercisesDto toDto(ExercisesEntity exEntity) {
		if (exEntity==null) return null;
		return ExercisesDto.builder()
				.id(exEntity.getExId())
				.names(exEntity.getExName())
				.category(exEntity.getExCategory())
				.force(exEntity.getExForce())
				.instrucions(exEntity.getExInstructions())
				.level(exEntity.getExLevel())
				.mechanic(exEntity.getExLevel())
				.primaryMuscles(exEntity.getExPrimaryMuscles()[0])
				.secondaryMuscles(exEntity.getExSecondaryMuscles()[0])
				.build();
	}
}
