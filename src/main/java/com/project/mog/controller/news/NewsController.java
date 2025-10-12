package com.project.mog.controller.news;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 네이버 검색 API 블로그 검색 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/news")
@RequiredArgsConstructor
public class NewsController {

    @Value("${naver.client.id}")
    private String clientId;

    @Value("${naver.client.secret}")
    private String clientSecret;

    /**
     * 운동 관련 뉴스 검색 (네이버 검색 API 사용)
     */
    @GetMapping("/fitness")
    public ResponseEntity<String> getFitnessNews(
            @RequestParam(defaultValue = "운동 헬스 피트니스") String query,
            @RequestParam(defaultValue = "12") int display) {
        
        log.info("네이버 뉴스 검색 요청 - query: {}, display: {}", query, display);
        
        try {
            // 오늘 날짜 기준 최신 운동/헬스/피트니스 뉴스 검색
            // 검색어를 더 구체적으로 설정하여 최신 뉴스가 나오도록 함
            String searchQuery = "헬스 피트니스";
            String text = URLEncoder.encode(searchQuery, StandardCharsets.UTF_8);
            String apiURL = "https://openapi.naver.com/v1/search/news?query=" + text 
                          + "&display=" + display 
                          + "&sort=date" // 최신순 정렬
                          + "&start=1"; // 가장 최신 뉴스부터
            
            URL url = new URL(apiURL);
            HttpURLConnection con = (HttpURLConnection)url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("X-Naver-Client-Id", clientId);
            con.setRequestProperty("X-Naver-Client-Secret", clientSecret);
            
            int responseCode = con.getResponseCode();
            BufferedReader br;
            
            if (responseCode == 200) { // 정상 호출
                br = new BufferedReader(new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8));
            } else {  // 오류 발생
                br = new BufferedReader(new InputStreamReader(con.getErrorStream(), StandardCharsets.UTF_8));
            }
            
            StringBuilder response = new StringBuilder();
            String inputLine;
            while ((inputLine = br.readLine()) != null) {
                response.append(inputLine);
            }
            br.close();
            
            log.info("네이버 뉴스 검색 완료 - 응답 코드: {}", responseCode);
            
            if (responseCode == 200) {
                return ResponseEntity.ok(response.toString());
            } else {
                return ResponseEntity.status(responseCode).body(response.toString());
            }
            
        } catch (Exception e) {
            log.error("뉴스 검색 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    /**
     * 이미지 검색 (네이버 이미지 검색 API 사용)
     */
    @GetMapping("/image")
    public ResponseEntity<String> searchImage(
            @RequestParam String query,
            @RequestParam(defaultValue = "1") int display) {
        
        log.info("네이버 이미지 검색 요청 - query: {}", query);
        
        try {
            String text = URLEncoder.encode(query + " 운동", StandardCharsets.UTF_8);
            String apiURL = "https://openapi.naver.com/v1/search/image?query=" + text 
                          + "&display=" + display 
                          + "&sort=sim"; // 정확도순 정렬
            
            URL url = new URL(apiURL);
            HttpURLConnection con = (HttpURLConnection)url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("X-Naver-Client-Id", clientId);
            con.setRequestProperty("X-Naver-Client-Secret", clientSecret);
            
            int responseCode = con.getResponseCode();
            BufferedReader br;
            
            if (responseCode == 200) {
                br = new BufferedReader(new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8));
            } else {
                br = new BufferedReader(new InputStreamReader(con.getErrorStream(), StandardCharsets.UTF_8));
            }
            
            StringBuilder response = new StringBuilder();
            String inputLine;
            while ((inputLine = br.readLine()) != null) {
                response.append(inputLine);
            }
            br.close();
            
            log.info("네이버 이미지 검색 완료 - 응답 코드: {}", responseCode);
            
            if (responseCode == 200) {
                return ResponseEntity.ok(response.toString());
            } else {
                return ResponseEntity.status(responseCode).body(response.toString());
            }
            
        } catch (Exception e) {
            log.error("이미지 검색 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }
}
