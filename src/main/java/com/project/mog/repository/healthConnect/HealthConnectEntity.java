package com.project.mog.repository.healthConnect;

import java.util.ArrayList;
import java.util.List;

import com.project.mog.repository.users.UsersEntity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "health_connect")
public class HealthConnectEntity {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "heartconnectid")
	private Long id;
	
	@ElementCollection
	@CollectionTable(name = "step_data", joinColumns = @JoinColumn(name="health_connect_id"))
	@Column(name = "step")
	private List<Integer> stepData;
	
	@OneToMany(mappedBy = "healthConnect", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<HeartRateDataEntity> heartRateData = new ArrayList<>();
	
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
	
	public HealthConnectEntity(UsersEntity user,List<Integer> stepData, double caloriesBurnedData, double distanceWalked,
							double activeCaloriesBurned, Long totalSleepMinutes, Long deepSleepMinutes,
							Long remSleepMinutes, Long lightSleepMinutes) {
		this.stepData=stepData;
		this.caloriesBurnedData=caloriesBurnedData;
		this.distanceWalked=distanceWalked;
		this.activeCaloriesBurned=activeCaloriesBurned;
		this.totalSleepMinutes=totalSleepMinutes;
		this.deepSleepMinutes=deepSleepMinutes;
		this.remSleepMinutes=remSleepMinutes;
		this.lightSleepMinutes=lightSleepMinutes;
	}
	
	public void addHeartRateData(List<HeartRateDataEntity> heartRates) {
		this.heartRateData.clear();
		heartRates.forEach(hr->{
			hr.setHealthConnect(this);
			this.heartRateData.add(hr);
		});
	}
	
}
