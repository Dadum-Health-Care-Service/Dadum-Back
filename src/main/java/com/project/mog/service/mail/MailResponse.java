package com.project.mog.service.mail;

import org.springframework.stereotype.Component;

@Component
public class MailResponse {
	
	public String buildMailResponse(String usersName, String tempPassword) {
		String htmlContent = String.format("""
				<!DOCTYPE html>
	            <html lang="ko">
	            <head>
	                <meta charset="UTF-8">
	                <title>비밀번호 찾기 안내</title>
	            </head>
	            <body style="font-family: Arial, sans-serif; background-color: #f0f8ff; padding: 20px;">
	                <div style="max-width: 600px; margin: 30px auto; background-color: #ffffff; padding: 30px; border-radius: 8px; border: 1px solid #eeeeee;">
	                    <div style="text-align: center; margin-bottom: 2rem;">
				        	<h1 style="font-size: 2.5rem; font-weight: 700; margin-bottom: 1.5rem; background-color: #2563eb; -webkit-background-clip: text; -webkit-text-fill-color: transparent; background-clip: text;">🎯 다듬</h1>
				        	<h2 style="color: #4facfe;">비밀번호 찾기 결과 안내 이메일입니다.</h2>
	                    </div>
	                    <p style="color: #555555;">%s 회원님의 요청으로 임시 비밀번호를 발급했습니다.</p>
	                    <p style="color: #555555;">아래의 임시 비밀번호로 로그인 후, <strong style="color: #d9534f;">반드시 비밀번호를 변경해주세요.</strong></p>
	                    <div style="display: flex; justify-content: center;">
		                    <div style="padding: 15px; margin: 20px 0; background-color: #e6f3ff; border-radius: 4px; text-align: center; width: 300px">
					            <p style="margin: 20px; color: #5cb85c; font-size: 2rem; gap: 0.2rem;"><strong>%s</strong></p>
		                    </div>
	                    </div>
	                    <p style="color: #555555;">감사합니다.</p>
	                </div>
	            </body>
	            </html>
				""",usersName, tempPassword);
		return htmlContent;
	}

}
