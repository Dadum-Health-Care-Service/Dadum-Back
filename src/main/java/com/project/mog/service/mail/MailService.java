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
//	private static final String messageTitle ="비밀번호 찾기 결과 안내 이메일입니다.\n";
//	private static final String message = " 회원님의 임시 비밀번호는 아래와 같습니다.\n로그인 후 반드시 비밀번호를 변경해주세요.\n\n";
	
	@Value("${spring.mail.username}")
	private String from;
	
	public MailDto createMail(String password, String usersName, String to) {
		String htmlContent = mailResponse.buildMailResponse(usersName, password);
		MailDto mailDto = new MailDto(from, to, title,htmlContent);
		return mailDto;
	}
	
//	public MailDto createMail(String password, String usersName, String to) {
//		MailDto mailDto = new MailDto(from, to, title, messageTitle, usersName+ message + password);
//		return mailDto;
//	}
	
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
//		SimpleMailMessage mailMessage = new SimpleMailMessage();
//		mailMessage.setTo(mailDto.getTo());
//		mailMessage.setSubject(mailDto.getTitle());
//		mailMessage.setText(mailDto.getMessageTitle()+mailDto.getMessage());
//		mailMessage.setFrom(mailDto.getFrom());
//		mailMessage.setReplyTo(mailDto.getFrom());
		
//		mailSender.send(mailMessage);
		
	}
	
}
