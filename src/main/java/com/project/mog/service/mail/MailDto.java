package com.project.mog.service.mail;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MailDto {
	private String from;
	private String to;
	private String title;
	private String htmlContent;
//	private String messageTitle;
//	private String message;
}
