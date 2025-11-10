package com.project.mog.repository.auth;

import java.time.LocalDateTime;

import com.project.mog.repository.bios.BiosEntity;
import com.project.mog.repository.users.UsersEntity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name="auth")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthEntity {
	@Id
	@Column(length=19,nullable=false)
	@SequenceGenerator(name = "SEQ_AUTH_GENERATOR",sequenceName = "SEQ_AUTH",allocationSize = 1,initialValue = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE,generator ="SEQ_AUTH_GENERATOR")
	private long authId;
	
	@Column(length=100,nullable=false)
	private String password;
	
	@Column(name = "ISPASSWORDLESS", nullable = false, columnDefinition = "NUMBER(1) DEFAULT 0")
	private boolean isPasswordless;
	
	@Column(length=1000,nullable=true)
	private String webPushToken;
	
	@Column(nullable=false)
	private LocalDateTime connectTime;
	
	@PrePersist//영속화 되기전 아래 메소드 실행
	public void setCreateDate() {
		this.connectTime= LocalDateTime.now();		
	}
	
	@PreUpdate//영속화 되기전 아래 메소드 실행
	public void setConnectTime() {
		this.connectTime= LocalDateTime.now();		
	}
	
	@OneToOne(fetch = FetchType.LAZY, cascade=CascadeType.ALL)
    @JoinColumn(name = "usersId", referencedColumnName = "usersId", nullable = false)
	private UsersEntity user;
	
}
