package com.project.mog.repository.role;

import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name="rolepermissions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RolePermissions {
    @Id
	@Column(length=19,nullable=false)
	@SequenceGenerator(name = "SEQ_ROLE_PERMISSIONS_GENERATOR",sequenceName = "SEQ_ROLE_PERMISSIONS",allocationSize = 1,initialValue = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE,generator ="SEQ_ROLE_PERMISSIONS_GENERATOR" )	
	private Long permissionId;

    @Column(length=20,nullable=false)
    private String permissionName;

    @Column(nullable=false)
    private String permissionsCategory;

    @Column(nullable=false)
    private String permissionsDescription;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "roleId", referencedColumnName = "roleId", nullable = false)
    private RolesEntity role;
    
}
