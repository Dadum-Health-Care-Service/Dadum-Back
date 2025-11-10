package com.project.mog.repository.role;

import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
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
@Table(name="roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RolesEntity {
    @Id
	@Column(length=19,nullable=false)
	@SequenceGenerator(name = "SEQ_ROLE_GENERATOR",sequenceName = "SEQ_ROLE",allocationSize = 1,initialValue = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE,generator ="SEQ_ROLE_GENERATOR" )	
	private Long roleId;

    @Column(length=20,nullable=false,unique=true)
    private String roleName;

    @Column(nullable=false)
    private String roleDescription;



    @OneToMany(mappedBy = "role", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<RoleAssignmentEntity> roleAssignments;
}
