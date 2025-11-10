package com.project.mog.controller.routine;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "루틴 결과 조회 요청 DTO")
public class RoutineEndTotalRequest {
	@Schema(description = "시작 날짜", example = "2024-01-01T00:00:00")
	LocalDateTime startDate;
	@Schema(description = "종료 날짜", example = "2024-12-31T23:59:59")
	LocalDateTime endDate;
}
