package com.project.mog.repository.routine;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name="saveroutineset")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaveRoutineSetEntity {
	@Id
	@Column(length=19,nullable=false)
	@SequenceGenerator(name = "SEQ_SRS_GENERATOR",sequenceName = "SEQ_SRS",allocationSize = 1,initialValue = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE,generator ="SEQ_SRS_GENERATOR" )	
	private long srsId;
	
	@Column(length=19)
	private long weight;
	
	@Column(length=19)
	private long many;
	
	@Column(length=19)
	private long rest;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "srId", referencedColumnName = "srId", nullable = true)
	private SaveRoutineEntity saveRoutine;
}
