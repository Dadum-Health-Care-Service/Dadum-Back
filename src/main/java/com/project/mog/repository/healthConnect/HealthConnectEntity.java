package com.project.mog.repository.healthConnect;

import java.util.ArrayList;
import java.util.List;

import com.project.mog.repository.users.UsersEntity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "healthConnect")
public class HealthConnectEntity {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "health_connect_id")
	private Long id;
	
	@OneToMany(mappedBy = "healthConnect", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<StepDataEntity> stepData = new ArrayList<>();
	
	@OneToMany(mappedBy = "healthConnect", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<HeartRateDataEntity> heartRateData = new ArrayList<>();
	
	public void addStepData(List<StepDataEntity> steps) {
		this.stepData.clear();
		for (StepDataEntity step : steps) {
			step.setHealthConnect(this);
			this.stepData.add(step);
		}
	}
	
	public void addHeartRateData(List<HeartRateDataEntity> heartRates) {
		this.heartRateData.clear();
		for (HeartRateDataEntity hr : heartRates) {
			hr.setHealthConnect(this);
			this.heartRateData.add(hr);
		}
	}
	
	private double caloriesBurnedData;
	private double distanceWalked;
	private double activeCaloriesBurned;
	private Long totalSleepMinutes;
	private Long deepSleepMinutes;
	private Long remSleepMinutes;
	private Long lightSleepMinutes;
	
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usersid", referencedColumnName="usersId", nullable = false)
    private UsersEntity user;
	
	public HealthConnectEntity(UsersEntity user,double caloriesBurnedData, double distanceWalked,
							double activeCaloriesBurned, Long totalSleepMinutes, Long deepSleepMinutes,
							Long remSleepMinutes, Long lightSleepMinutes) {
		this.user=user;
		this.caloriesBurnedData=caloriesBurnedData;
		this.distanceWalked=distanceWalked;
		this.activeCaloriesBurned=activeCaloriesBurned;
		this.totalSleepMinutes=totalSleepMinutes;
		this.deepSleepMinutes=deepSleepMinutes;
		this.remSleepMinutes=remSleepMinutes;
		this.lightSleepMinutes=lightSleepMinutes;
	}
	
}
