package com.project.mog.service.routine;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.project.mog.repository.routine.RoutineEndDetailEntity;
import com.project.mog.repository.routine.RoutineEndTotalEntity;
import com.project.mog.repository.routine.RoutineEntity;
import com.project.mog.repository.routine.RoutineResultEntity;

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
public class RoutineEndTotalDto {
	@Schema(hidden = true)
	private long id;
	@Schema(hidden = true)
	private long retId;
	@JsonProperty("tStart")
	private LocalDateTime tStart;
	@JsonProperty("tEnd")
	private LocalDateTime tEnd;
	private List<RoutineEndDetailDto> routineEndDetails;
	private RoutineResultDto routineResult;
	
	
	
	public static RoutineEndTotalDto toDto(RoutineEndTotalEntity retEntity) {
		if(retEntity==null) return null;
		List<RoutineEndDetailEntity> redEntity = retEntity.getRoutineEndDetail();
		RoutineResultEntity rrEntity = retEntity.getRoutineResult();
		List<RoutineEndDetailDto> redDto;
		if(redEntity==null) {
			redDto = List.of();
		}
		else {
			redDto = redEntity.stream().map(RoutineEndDetailDto::toDto).collect(Collectors.toList());
		}
		
		return RoutineEndTotalDto.builder()
								.id(retEntity.getRoutine().getSetId())
								.retId(retEntity.getRetId())
								.tStart(retEntity.getTStart())
								.tEnd(retEntity.getTEnd())
								.routineEndDetails(redDto)
								.routineResult(RoutineResultDto.toDto(rrEntity))
								.build();
	
	}
}