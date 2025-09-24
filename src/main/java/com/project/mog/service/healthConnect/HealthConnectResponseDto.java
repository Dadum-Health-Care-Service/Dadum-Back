package com.project.mog.service.healthConnect;

import lombok.Getter;

@Getter
public class HealthConnectResponseDto {

	private final String status;
	private final String message;
	
	public HealthConnectResponseDto(String message) {
		this.status="success";
		this.message=message;
	}
}
