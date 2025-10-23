package com.project.mog.service.firebase;
import org.springframework.core.io.ClassPathResource;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.google.firebase.messaging.WebpushConfig;
import com.google.firebase.messaging.WebpushNotification;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class FirebaseService {
    
    private FirebaseMessaging firebaseMessaging;
    
    @PostConstruct
    public void initialize() {
        try {
            // 클래스패스에서 서비스 계정 키 파일 직접 읽기
            ClassPathResource resource = new ClassPathResource("firebase-adminsdk.json");
            InputStream serviceAccount = resource.getInputStream();
            
            FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build();
                
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
            }
            
            this.firebaseMessaging = FirebaseMessaging.getInstance();
            log.info("Firebase 서비스 초기화 완료");
            
        } catch (Exception e) {
            log.error("Firebase 초기화 실패", e);
        }
    }
    
    public void sendNotification(String token, String title, String body) {
        try {
            Map<String,String> data = new HashMap<>();
            data.put("title", title);
            data.put("body", body);
            data.put("type", "REQUEST_ROLE");
            Message message = Message.builder()
                .setToken(token)
                .putAllData(data)
                .setNotification(Notification.builder()
                    .setTitle(title)
                    .setBody(body)
                    .setImage("/img/Dadum-icon.png")
                    .build())
                .setWebpushConfig(WebpushConfig.builder()
                    .setNotification(WebpushNotification.builder()
                        .setIcon("/img/Dadum-icon.png")
                        .setBadge("/img/Dadum-icon.png")
                        .setTitle(title)
                        .setBody(body)
                        .build())
                    .build())
                .build();
                
            String response = firebaseMessaging.send(message);
            log.info("알림 전송 성공: {}", response);
            
        } catch (Exception e) {
            log.error("알림 전송 실패 - 토큰: {}, 오류: {}", token, e.getMessage());
        }
    }
}