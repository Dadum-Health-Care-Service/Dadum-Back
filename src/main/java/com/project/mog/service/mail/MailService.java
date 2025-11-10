package com.project.mog.service.mail;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MailService {
	private final JavaMailSender mailSender;
	private final MailResponse mailResponse;
	
	private static final String title ="[Dadum]비밀번호 찾기 안내 이메일";
	
	@Value("${spring.mail.username}")
	private String from;
	
	public MailDto createMail(String password, String usersName, String to) {
		String htmlContent = mailResponse.buildMailResponse(usersName, password);
		MailDto mailDto = new MailDto(from, to, title,htmlContent);
		return mailDto;
	}
	
	public void sendMail(MailDto mailDto) {
		MimeMessage message = mailSender.createMimeMessage();
		try {
			MimeMessageHelper helper = new MimeMessageHelper(message,true,"UTF-8");
			
			helper.setTo(mailDto.getTo());
			helper.setSubject(mailDto.getTitle());
			helper.setText(mailDto.getHtmlContent(),true);
			helper.setFrom(mailDto.getFrom());
			helper.setReplyTo(mailDto.getFrom());
			
			mailSender.send(message);
		} catch(MessagingException e) {
			e.printStackTrace();
			throw new RuntimeException("메일 전송에 실패했습니다.",e);
		}
		
	}
	
}
