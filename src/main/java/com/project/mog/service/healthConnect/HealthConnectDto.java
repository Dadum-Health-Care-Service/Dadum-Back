package com.project.mog.service.healthConnect;

import java.util.List;

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
public class HealthConnectDto {
	private Long healthId;
	private List<Integer> stepData;
	private List<HeartRateDataDto> heartRateData;
	private double caloriesBurnedData;
	private double distanceWalked;
	private double activeCaloriesBurned;
	private Long totalSleepMinutes;
	private Long deepSleepMinutes;
	private Long remSleepMinutes;
	private Long lightSleepMinutes;
	
	@Getter
	@Setter
	@NoArgsConstructor
	@AllArgsConstructor
	public static class HeartRateDataDto{
		private double bpm;
		private String time;
	}
	
	@Getter
	@Setter
	@NoArgsConstructor
	@AllArgsConstructor
	public static class StepDataDto{
		private int stepCount;
	}

}
