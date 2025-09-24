package com.project.mog.controller.auth;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PasswordlessRegisterRequest {
	private String passwordlessToken;
}
