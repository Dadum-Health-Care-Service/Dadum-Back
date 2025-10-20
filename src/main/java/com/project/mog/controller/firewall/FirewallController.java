package com.project.mog.controller.firewall;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.mog.service.firewall.BlockIpRequest;
import com.project.mog.service.firewall.FirewallService;
import com.project.mog.service.firewall.UnblockIpRequest;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/firewall")
@RequiredArgsConstructor
public class FirewallController {
	private final FirewallService firewallService;
	
	/**
     * IP 차단 - WSL에서 직접 호출
     */
	@PostMapping("/block")
    public ResponseEntity<Map<String, Object>> blockIp(@RequestBody BlockIpRequest request) {
        log.info("REQUEST",request);
		log.info("🔒 [WSL] IP block request - IP: {}, Reason: {}", 
                request.getIpAddress(), request.getReason());
        
        Map<String, Object> response = new HashMap<>();
        try {
            firewallService.blockIp(request.getIpAddress(), request.getReason());
            response.put("status", "success");
            response.put("message", "IP blocked successfully");
            response.put("ipAddress", request.getIpAddress());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("❌ Failed to block IP: {}", request.getIpAddress(), e);
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
	
	/**
     * IP 차단 해제 - 프론트엔드에서 프록시를 통해 호출
     */
    @PostMapping("/unblock")
    public ResponseEntity<Map<String, Object>> unblockIp(@RequestBody UnblockIpRequest request) {
        log.info("🔓 [Frontend] IP unblock request - IP: {}", request.getIpAddress());
        
        Map<String, Object> response = new HashMap<>();
        try {
            firewallService.unblockIp(request.getIpAddress());
            response.put("status", "success");
            response.put("message", "IP unblocked successfully");
            response.put("ipAddress", request.getIpAddress());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("❌ Failed to unblock IP: {}", request.getIpAddress(), e);
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * 차단된 IP 목록 조회
     */
    @GetMapping("/blocked-list")
    public ResponseEntity<Map<String, Object>> getBlockedIps() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<String> blockedIps = firewallService.getBlockedIps();
            response.put("status", "success");
            response.put("blockedIps", blockedIps);
            response.put("count", blockedIps.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}
