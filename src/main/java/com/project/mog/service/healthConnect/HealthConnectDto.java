package com.project.mog.service.healthConnect;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class HealthConnectDto {
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
	public static class HeartRateDataDto{
		private double bpm;
		private String time;
	}

}
