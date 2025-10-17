package com.project.mog.controller.news;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

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
     * 피트니스 관련 뉴스 검색 (네이버 검색 API 사용)
     */
    @GetMapping("/fitness")
    public ResponseEntity<String> getFitnessNews(
            @RequestParam(defaultValue = "운동 건강 피트니스 다이어트") String query,
            @RequestParam(defaultValue = "50") int display) {

        log.info("네이버 피트니스 뉴스 검색 요청 - query: {}, display: {}", query, display);

        try {
            // 1️⃣ 기존 방식으로 네이버 뉴스 API 호출
            String searchQuery = query;
            String text = URLEncoder.encode(searchQuery, StandardCharsets.UTF_8);

            log.info("실제 네이버 API 검색어: {}", searchQuery);

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
                // 2️⃣ JSON 파싱 및 필터링
                JSONObject json = new JSONObject(response.toString());
                JSONArray items = json.getJSONArray("items");
                JSONArray filtered = new JSONArray();

                // 필터링 단어 세트 (강화된 조건)
                List<String> include = List.of("운동", "피트니스", "헬스", "트레이닝", "체중", "다이어트", "근육", "스트레칭", "건강", "요가", "홈트", "웨이트", "러닝", "달리기", "걷기", "체력", "몸매", "살빼기", "헬스장", "짐", "운동기구", "근력", "유산소", "무산소");
                List<String> exclude = List.of("정치", "대통령", "의원", "자동차", "선수", "팀", "야구", "축구", "리그", "감독", "골프", "e스포츠", "테슬라", "전기차", "모델", "주식", "투자", "금융", "부동산", "아파트", "분양", "건설", "건설사", "GS건설", "현대건설", "삼성물산", "대림산업", "한화건설", "역세권", "숲세권", "단지", "아파트", "오피스텔", "빌라", "빌딩", "상가", "임대", "매매", "전세", "월세", "경제", "증권", "은행", "금융권", "코스피", "코스닥", "주가", "시장", "거래", "매출", "영업이익", "수익", "손실", "기업", "회사", "법인", "대표", "CEO", "사장", "이사", "임원");

                // 3️⃣ 기사 필터링 (강화된 조건)
                for (int i = 0; i < items.length(); i++) {
                    JSONObject item = items.getJSONObject(i);
                    String title = item.optString("title", "").replaceAll("<.*?>", ""); // HTML 태그 제거
                    String desc = item.optString("description", "").replaceAll("<.*?>", ""); // HTML 태그 제거

                    // HTML 엔티티 디코딩
                    title = decodeHtmlEntities(title);
                    desc = decodeHtmlEntities(desc);

                    String content = (title + " " + desc).toLowerCase();

                    boolean hasInclude = include.stream().anyMatch(keyword -> content.contains(keyword.toLowerCase()));
                    boolean hasExclude = exclude.stream().anyMatch(keyword -> content.contains(keyword.toLowerCase()));

                    // 조건 강화: 포함 단어가 있고, 제외 단어가 없어야 통과
                    if (hasInclude && !hasExclude) {
                        // 필터링된 아이템의 제목과 설명도 디코딩된 버전으로 업데이트
                        item.put("title", title);
                        item.put("description", desc);
                        filtered.put(item);
                    }
                }

                // 4️⃣ 응답 재구성
                JSONObject result = new JSONObject();
                result.put("items", filtered);
                result.put("total", filtered.length());
                result.put("lastBuildDate", json.optString("lastBuildDate", ""));
                result.put("start", json.optInt("start", 1));
                result.put("display", filtered.length());

                log.info("필터링 완료 - 원본: {}개, 필터링 후: {}개", items.length(), filtered.length());

                return ResponseEntity.ok(result.toString());
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
     * HTML 엔티티를 디코딩하는 메서드
     */
    private String decodeHtmlEntities(String text) {
        if (text == null) return "";
        
        return text
                .replace("&quot;", "\"")
                .replace("&apos;", "'")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&amp;", "&")
                .replace("&#39;", "'")
                .replace("&#x27;", "'")
                .replace("&#x2F;", "/")
                .replace("&#x60;", "`")
                .replace("&#x3D;", "=");
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
