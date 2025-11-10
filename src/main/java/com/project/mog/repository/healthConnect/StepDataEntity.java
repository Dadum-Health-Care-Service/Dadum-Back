package com.project.mog.repository.healthConnect;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "step_data")
public class StepDataEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "step_data_id")
	private Long id;
	
	@Column(name = "step_count")
	private int stepCount;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "health_connect_id", nullable = false)
	private HealthConnectEntity healthConnect;
	
	public StepDataEntity(int stepCount, HealthConnectEntity healthConnect) {
		this.stepCount = stepCount;
		this.healthConnect = healthConnect;
	}
}
