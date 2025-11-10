package com.project.mog.service.firewall;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class FirewallService {
    
    // 차단된 IP 정보를 저장하는 클래스
    public static class BlockedIpInfo {
        private String ipAddress;
        private String reason;
        private LocalDateTime blockedAt;
        
        public BlockedIpInfo(String ipAddress, String reason, LocalDateTime blockedAt) {
            this.ipAddress = ipAddress;
            this.reason = reason;
            this.blockedAt = blockedAt;
        }
        
        // getter 메서드들
        public String getIpAddress() { return ipAddress; }
        public String getReason() { return reason; }
        public LocalDateTime getBlockedAt() { return blockedAt; }
    }
    
    // 차단된 IP 목록 (IP -> BlockedIpInfo 매핑)
    private final Map<String, BlockedIpInfo> blockedIps = new ConcurrentHashMap<>();
    
    /**
     * Windows 방화벽으로 IP 차단
     * @param ipAddress 차단할 IP 주소
     * @param reason 차단 사유
     * @throws Exception PowerShell 실행 실패 시
     */
    public void blockIp(String ipAddress, String reason) throws Exception {
        // 이미 차단된 IP 체크
        if (blockedIps.containsKey(ipAddress)) {
            log.info("IP already blocked: {}", ipAddress);
            return;
        }
        
        // 방화벽 규칙 이름 생성
        String ruleName = "Dadum-Block-" + ipAddress.replace(".", "-");
        
        // PowerShell 명령어 생성
        String psCommand = String.format(
            "New-NetFirewallRule -DisplayName '%s' -Direction Inbound -RemoteAddress '%s' -Action Block -ErrorAction Stop",
            ruleName, ipAddress
        );
        
        log.info("🔒 Executing PowerShell command to block IP: {}", ipAddress);
        log.debug("Command: {}", psCommand);
        
        // PowerShell 실행
        ProcessBuilder processBuilder = new ProcessBuilder(
            "powershell.exe", "-Command", psCommand
        );
        processBuilder.redirectErrorStream(true);
        
        Process process = processBuilder.start();
        
        // 출력 읽기
        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
                log.debug("PS Output: {}", line);
            }
        }
        
        int exitCode = process.waitFor();
        
        if (exitCode == 0) {
            // 차단 시각과 함께 정보 저장
            BlockedIpInfo blockedInfo = new BlockedIpInfo(ipAddress, reason, LocalDateTime.now());
            blockedIps.put(ipAddress, blockedInfo);
            log.info("✅ IP blocked successfully - IP: {}, Reason: {}, BlockedAt: {}", 
                    ipAddress, reason, blockedInfo.getBlockedAt());
        } else {
            String errorMsg = String.format(
                "PowerShell exit code: %d, Output: %s", exitCode, output.toString()
            );
            log.error("❌ Failed to block IP {}: {}", ipAddress, errorMsg);
            throw new Exception(errorMsg);
        }
    }
    
    /**
     * Windows 방화벽에서 IP 차단 해제
     * @param ipAddress 해제할 IP 주소
     * @throws Exception PowerShell 실행 실패 시
     */
    public void unblockIp(String ipAddress) throws Exception {
        if (!blockedIps.containsKey(ipAddress)) {
            log.warn("IP not in blocked list: {}", ipAddress);
            // 그래도 방화벽 규칙이 있을 수 있으니 제거 시도
        }
        
        String ruleName = "Dadum-Block-" + ipAddress.replace(".", "-");
        
        // PowerShell 명령어 (ErrorAction SilentlyContinue: 규칙이 없어도 에러 무시)
        String psCommand = String.format(
            "Remove-NetFirewallRule -DisplayName '%s' -ErrorAction SilentlyContinue",
            ruleName
        );
        
        log.info("🔓 Executing PowerShell command to unblock IP: {}", ipAddress);
        log.debug("Command: {}", psCommand);
        
        ProcessBuilder processBuilder = new ProcessBuilder(
            "powershell.exe", "-Command", psCommand
        );
        processBuilder.redirectErrorStream(true);
        
        Process process = processBuilder.start();
        
        // 출력 읽기
        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
                log.debug("PS Output: {}", line);
            }
        }
        
        int exitCode = process.waitFor();
        
        // exit code 0(성공) 또는 1(규칙이 없음) 모두 성공 처리
        if (exitCode == 0 || exitCode == 1) {
            blockedIps.remove(ipAddress);
            log.info("✅ IP unblocked successfully: {}", ipAddress);
        } else {
            String errorMsg = String.format(
                "PowerShell exit code: %d, Output: %s", exitCode, output.toString()
            );
            log.error("❌ Failed to unblock IP {}: {}", ipAddress, errorMsg);
            throw new Exception(errorMsg);
        }
    }
    
    /**
     * 차단된 IP 목록 반환 (기존 방식 - IP만)
     * @return 차단된 IP 주소 리스트
     */
    public List<String> getBlockedIps() {
        return new ArrayList<>(blockedIps.keySet());
    }
    
    /**
     * 차단된 IP 상세 정보 반환 (새로운 방식 - IP, reason, 차단시각 포함)
     * @return 차단된 IP 상세 정보 리스트
     */
    public List<BlockedIpInfo> getBlockedIpDetails() {
        return new ArrayList<>(blockedIps.values());
    }
}