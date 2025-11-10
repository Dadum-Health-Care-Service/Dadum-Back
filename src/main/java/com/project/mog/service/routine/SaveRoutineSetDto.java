package com.project.mog.service.routine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.project.mog.repository.routine.SaveRoutineEntity;
import com.project.mog.repository.routine.SaveRoutineSetEntity;

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
public class SaveRoutineSetDto {
	private long srsId;
	private long weight;
	private long many;
	private long rest;
	
	public SaveRoutineSetEntity toEntity(SaveRoutineEntity srEntity) {
		return SaveRoutineSetEntity.builder()
				.weight(weight)
				.many(many)
				.rest(rest)
				.saveRoutine(srEntity)
				.build();
	}
	
	public static SaveRoutineSetDto toDto(SaveRoutineSetEntity saveRoutineSetEntity) {
		if(saveRoutineSetEntity==null) return null;
		return SaveRoutineSetDto.builder()
				.srsId(saveRoutineSetEntity.getSrsId())
				.weight(saveRoutineSetEntity.getWeight())
				.many(saveRoutineSetEntity.getMany())
				.rest(saveRoutineSetEntity.getRest())
				.build();
	}

}
