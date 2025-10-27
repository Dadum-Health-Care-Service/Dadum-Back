package com.project.mog.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException.Unauthorized;

@RestControllerAdvice
public class GlobalExceptionHandler {

	
	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException ex){
		return ResponseEntity
				.status(HttpStatus.BAD_REQUEST)
				.body("[400 Bad Request] 잘못된 요청 : "+ex.getMessage());
	}
	
	@ExceptionHandler(Unauthorized.class)
	public ResponseEntity<String> handleUnauthorized(Unauthorized ex){
		return ResponseEntity
				.status(HttpStatus.UNAUTHORIZED)
				.body("[401 Unauthorized] 인증되지 않음 : "+ex.getMessage());
	}
	
	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<String> handleIllegalArgument(AccessDeniedException ex){
		return ResponseEntity
				.status(HttpStatus.FORBIDDEN)
				.body("[403 Forbidden] 접근이 거부되었습니다 : "+ex.getMessage());
	}
	
	@ExceptionHandler(IllegalStateException.class)
	public ResponseEntity<String> handleIllegalState(IllegalStateException ex){
		return ResponseEntity
				.status(HttpStatus.NOT_ACCEPTABLE)
				.body("[406 Not Acceptable] 서비스 이용 불가 : "+ex.getMessage());
	}
	
	

}
