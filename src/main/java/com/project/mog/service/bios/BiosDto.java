package com.project.mog.service.bios;

import java.time.LocalDateTime;

import com.project.mog.repository.bios.BiosEntity;
import com.project.mog.repository.users.UsersEntity;
import com.project.mog.service.users.UsersDto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BiosDto {
	@Schema(hidden = true)
	private long bioId;
	@Schema(description = "age",example="25")
	private long age;
	@Schema(description = "gender",example="false")
	private boolean gender;
	@Schema(description = "height",example="170")
	private long height;
	@Schema(description = "weight",example="70")
	private long weight;
	
	public BiosEntity toEntity() {
		
		return BiosEntity.builder()
				.bioId(bioId)
				.age(age)
				.gender(gender)
				.height(height)
				.weight(weight)
				.build();
	}
	public static BiosDto toDto(BiosEntity bEntity) {
		if (bEntity==null) return null;
		return BiosDto.builder()
				.bioId(bEntity.getBioId())
				.age(bEntity.getAge())
				.gender(bEntity.isGender())
				.height(bEntity.getHeight())
				.weight(bEntity.getWeight())
				.build();
	}
}
