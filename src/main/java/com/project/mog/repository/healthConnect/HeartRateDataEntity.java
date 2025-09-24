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
	@Column(name = "heart_rate_data_id")
	private Long id;
	
	private double bpm;
	private String time;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "health_connect_id")
	private HealthConnectEntity healthConnect;
	
	public HeartRateDataEntity(double bpm,String time,HealthConnectEntity healthConnect) {
		this.bpm=bpm;
		this.time=time;
		this.healthConnect=healthConnect;
	}
	

}
