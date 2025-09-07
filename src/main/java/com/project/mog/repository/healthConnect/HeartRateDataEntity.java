package com.project.mog.repository.healthConnect;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "heart_rate_data")
public class HeartRateDataEntity {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	private double bpm;
	private String time;
	
	private HealthConnectEntity healthConnect;
	
	public HeartRateDataEntity(double bpm,String time) {
		this.bpm=bpm;
		this.time=time;
	}
	

}
