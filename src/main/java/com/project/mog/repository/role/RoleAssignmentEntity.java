package com.project.mog.repository.role;

import java.time.LocalDateTime;

import com.project.mog.repository.users.UsersEntity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
@Table(name="roleassignment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleAssignmentEntity {
    @Id
	@Column(length=19,nullable=false)
	@SequenceGenerator(name = "SEQ_ROLE_ASSIGNMENT_GENERATOR",sequenceName = "SEQ_ROLE_ASSIGNMENT",allocationSize = 1,initialValue = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE,generator ="SEQ_ROLE_ASSIGNMENT_GENERATOR" )	
	private Long assignmentId;

    @Column(nullable=false)
    private Long isActive;

    @Column(nullable=true)
	private LocalDateTime assignedAt;
    
    @Column(nullable=true)
    private LocalDateTime expiredAt;

    @PrePersist//영속화 되기전 아래 메소드 실행
	public void setAssignedAt() {
		this.assignedAt= LocalDateTime.now();
		this.expiredAt=LocalDateTime.now().plusDays(30);
	}
	@PreUpdate//영속화 되기전 아래 메소드 실행
	public void setExpiredAt() {
		this.expiredAt= LocalDateTime.now().plusDays(30);		
	}

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "usersId", nullable = false)
    private UsersEntity user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "roleId", nullable = false)
    private RolesEntity role;
}
