package com.project.mog.service.mail;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class SendPasswordRequest {
	private String email;
	private String usersName;
}
