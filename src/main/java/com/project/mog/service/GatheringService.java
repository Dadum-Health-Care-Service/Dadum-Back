package com.project.mog.service;

import com.project.mog.dto.gathering.*;
import com.project.mog.repository.gathering.GatheringEntity;
import com.project.mog.repository.gathering.GatheringParticipantEntity;
import com.project.mog.repository.gathering.GatheringParticipantRepository;
import com.project.mog.repository.gathering.GatheringRepository;
import com.project.mog.repository.gathering.ParticipantRole;
import com.project.mog.repository.gathering.ScheduleType;
import com.project.mog.repository.users.UsersEntity;
import com.project.mog.repository.users.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.DayOfWeek;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class GatheringService {
    
    @Autowired
    private GatheringRepository gatheringRepository;
    
    @Autowired
    private GatheringParticipantRepository participantRepository;
    
    @Autowired
    private UsersRepository usersRepository;
    
    // 모임 생성
    @Transactional
    public GatheringResponse createGathering(GatheringCreateRequest request, Long creatorId) {
        UsersEntity creator = usersRepository.findById(creatorId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        
        ScheduleType scheduleType = ScheduleType.valueOf(request.getScheduleType() != null ? request.getScheduleType() : "ONE_TIME");
        
        System.out.println("=== 모임 생성 디버깅 ===");
        System.out.println("요청된 scheduleType: " + request.getScheduleType());
        System.out.println("변환된 scheduleType: " + scheduleType);
        System.out.println("scheduleDetails: " + request.getScheduleDetails());
        System.out.println("nextMeetingDate: " + request.getNextMeetingDate());
        System.out.println("meetingTime: " + request.getMeetingTime());
        System.out.println("dayOfWeek: " + request.getDayOfWeek());
        
        // nextMeetingDate 처리
        LocalDateTime nextMeetingDateTime = null;
        
        if (scheduleType == ScheduleType.WEEKLY && request.getDayOfWeek() != null && request.getMeetingTime() != null) {
            // 주간 모임의 경우 다음 해당 요일 계산
            try {
                LocalTime meetingTime = LocalTime.parse(request.getMeetingTime());
                nextMeetingDateTime = calculateNextWeeklyMeeting(request.getDayOfWeek(), meetingTime);
                System.out.println("주간 모임 계산 결과: " + nextMeetingDateTime);
            } catch (Exception e) {
                System.out.println("주간 모임 계산 실패: " + e.getMessage());
            }
        } else if (scheduleType == ScheduleType.MONTHLY && request.getScheduleDetails() != null && request.getMeetingTime() != null && request.getDayOfWeek() != null) {
            // 월간 모임의 경우 주차와 요일을 고려하여 계산
            try {
                LocalTime meetingTime = LocalTime.parse(request.getMeetingTime());
                nextMeetingDateTime = calculateNextMonthlyMeetingWithWeek(request.getDayOfWeek(), request.getScheduleDetails(), meetingTime);
                System.out.println("월간 모임 계산 결과: " + nextMeetingDateTime);
            } catch (Exception e) {
                System.out.println("월간 모임 계산 실패: " + e.getMessage());
            }
        } else if (scheduleType == ScheduleType.CUSTOM) {
            // 사용자 정의 모임의 경우 scheduleDetails에서 날짜/시간 파싱 시도
            try {
                nextMeetingDateTime = parseCustomSchedule(request.getScheduleDetails());
            } catch (Exception e) {
                // 파싱 실패 시 null로 유지
            }
        } else if (request.getNextMeetingDate() != null && request.getMeetingTime() != null) {
            // 일회성 모임의 경우 직접 날짜/시간 설정
            try {
                LocalDate meetingDate = LocalDate.parse(request.getNextMeetingDate());
                LocalTime meetingTime = LocalTime.parse(request.getMeetingTime());
                nextMeetingDateTime = LocalDateTime.of(meetingDate, meetingTime);
                System.out.println("일회성 모임 설정: " + nextMeetingDateTime);
            } catch (Exception e) {
                System.out.println("일회성 모임 파싱 실패: " + e.getMessage());
            }
        }
        
        System.out.println("최종 nextMeetingDateTime: " + nextMeetingDateTime);
        
        GatheringEntity gathering = GatheringEntity.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .category(request.getCategory())
                .maxParticipants(request.getMaxParticipants())
                .currentParticipants(1) // 생성자가 자동으로 참여하므로 1로 설정
                .address(request.getAddress())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .scheduleType(scheduleType)
                .scheduleDetails(scheduleType == ScheduleType.WEEKLY || scheduleType == ScheduleType.MONTHLY ? 
                    request.getDayOfWeek() : request.getScheduleDetails())
                .nextMeetingDate(nextMeetingDateTime)
                .creator(creator)
                .build();
        
        GatheringEntity savedGathering = gatheringRepository.save(gathering);
        
        // 생성자를 자동으로 참여자로 추가 (방장)
        GatheringParticipantEntity creatorParticipant = GatheringParticipantEntity.builder()
                .gathering(savedGathering)
                .user(creator)
                .role(ParticipantRole.CREATOR)
                .joinedAt(LocalDateTime.now())
                .build();
        
        participantRepository.save(creatorParticipant);
        
        // 방장 추가 후 currentParticipants 업데이트
        savedGathering.setCurrentParticipants(1);
        gatheringRepository.save(savedGathering);
        
        return convertToResponse(savedGathering);
    }
    
    // 모든 활성 모임 조회
    @Transactional(readOnly = true)
    public List<GatheringResponse> getAllActiveGatherings() {
        List<GatheringEntity> gatherings = gatheringRepository.findActiveGatherings();
        return gatherings.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    // 카테고리별 모임 조회
    @Transactional(readOnly = true)
    public List<GatheringResponse> getGatheringsByCategory(String category) {
        List<GatheringEntity> gatherings = gatheringRepository.findActiveGatheringsByCategory(category);
        return gatherings.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    // 특정 모임 상세 조회
    @Transactional(readOnly = true)
    public GatheringResponse getGatheringById(Long gatheringId) {
        GatheringEntity gathering = gatheringRepository.findById(gatheringId)
                .orElseThrow(() -> new IllegalArgumentException("모임을 찾을 수 없습니다."));
        
        return convertToResponseWithParticipants(gathering);
    }
    
    // 모임 참여
    public void joinGathering(Long gatheringId, Long userId) {
        GatheringEntity gathering = gatheringRepository.findById(gatheringId)
                .orElseThrow(() -> new IllegalArgumentException("모임을 찾을 수 없습니다."));
        
        if (!"ACTIVE".equals(gathering.getStatus())) {
            throw new IllegalArgumentException("참여할 수 없는 모임입니다.");
        }
        
        if (gathering.getCurrentParticipants() >= gathering.getMaxParticipants()) {
            throw new IllegalArgumentException("모임이 가득 찼습니다.");
        }
        
        // 이미 참여했는지 확인
        Optional<GatheringParticipantEntity> existingParticipant = 
                participantRepository.findByGatheringIdAndUserId(gatheringId, userId);
        
        if (existingParticipant.isPresent()) {
            throw new IllegalArgumentException("이미 참여한 모임입니다.");
        }
        
        UsersEntity user = usersRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        
        // 참여자 추가
        GatheringParticipantEntity participant = GatheringParticipantEntity.builder()
                .gathering(gathering)
                .user(user)
                .role(ParticipantRole.MEMBER)
                .build();
        
        participantRepository.save(participant);
        
        // 참여자 수 증가
        gathering.setCurrentParticipants(gathering.getCurrentParticipants() + 1);
        gatheringRepository.save(gathering);
    }
    
    // 모임 나가기
    public void leaveGathering(Long gatheringId, Long userId) {
        GatheringParticipantEntity participant = participantRepository
                .findByGatheringIdAndUserId(gatheringId, userId)
                .orElseThrow(() -> new IllegalArgumentException("참여하지 않은 모임입니다."));
        
        participantRepository.delete(participant);
        
        // 참여자 수 감소
        GatheringEntity gathering = gatheringRepository.findById(gatheringId)
                .orElseThrow(() -> new IllegalArgumentException("모임을 찾을 수 없습니다."));
        
        gathering.setCurrentParticipants(gathering.getCurrentParticipants() - 1);
        gatheringRepository.save(gathering);
    }
    
    // 모임 참여자 목록 조회
    @Transactional(readOnly = true)
    public List<ParticipantResponse> getGatheringParticipants(Long gatheringId) {
        List<GatheringParticipantEntity> participants = participantRepository.findByGatheringId(gatheringId);
        return participants.stream()
                .map(this::convertToParticipantResponse)
                .collect(Collectors.toList());
    }
    
    // 사용자가 특정 모임에 참여했는지 확인
    @Transactional(readOnly = true)
    public boolean isParticipant(Long gatheringId, Long userId) {
        return participantRepository.findByGatheringIdAndUserId(gatheringId, userId).isPresent();
    }
    
    // 모임 삭제 (생성자만 가능)
    public void deleteGathering(Long gatheringId, Long userId) {
        GatheringEntity gathering = gatheringRepository.findById(gatheringId)
                .orElseThrow(() -> new IllegalArgumentException("모임을 찾을 수 없습니다."));
        
        if (gathering.getCreator().getUsersId() != userId) {
            throw new IllegalArgumentException("모임을 삭제할 권한이 없습니다.");
        }
        
        // 모임 상태를 비활성으로 변경
        gathering.setStatus("INACTIVE");
        gatheringRepository.save(gathering);
    }
    
    // Entity를 Response로 변환 (참여자 정보 제외)
    private GatheringResponse convertToResponse(GatheringEntity entity) {
        System.out.println("=== 모임 조회 디버깅 ===");
        System.out.println("모임 ID: " + entity.getGatheringId());
        System.out.println("모임 제목: " + entity.getTitle());
        System.out.println("scheduleType: " + entity.getScheduleType());
        System.out.println("scheduleDetails: " + entity.getScheduleDetails());
        System.out.println("nextMeetingDate: " + entity.getNextMeetingDate());
        
        return GatheringResponse.builder()
                .gatheringId(entity.getGatheringId())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .category(entity.getCategory())
                .maxParticipants(entity.getMaxParticipants())
                .currentParticipants(entity.getCurrentParticipants())
                .address(entity.getAddress())
                .latitude(entity.getLatitude())
                .longitude(entity.getLongitude())
                .status(entity.getStatus())
                .scheduleType(entity.getScheduleType().toString())
                .scheduleDetails(entity.getScheduleDetails())
                .nextMeetingDate(entity.getNextMeetingDate())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .creatorId(entity.getCreator().getUsersId())
                .creatorName(entity.getCreator().getUsersName())
                .creatorNickname(entity.getCreator().getNickName())
                .build();
    }
    
    // Entity를 Response로 변환 (참여자 정보 포함)
    private GatheringResponse convertToResponseWithParticipants(GatheringEntity entity) {
        List<ParticipantResponse> participants = getGatheringParticipants(entity.getGatheringId());
        
        return GatheringResponse.builder()
                .gatheringId(entity.getGatheringId())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .category(entity.getCategory())
                .maxParticipants(entity.getMaxParticipants())
                .currentParticipants(entity.getCurrentParticipants())
                .address(entity.getAddress())
                .latitude(entity.getLatitude())
                .longitude(entity.getLongitude())
                .status(entity.getStatus())
                .scheduleType(entity.getScheduleType().toString())
                .scheduleDetails(entity.getScheduleDetails())
                .nextMeetingDate(entity.getNextMeetingDate())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .creatorId(entity.getCreator().getUsersId())
                .creatorName(entity.getCreator().getUsersName())
                .creatorNickname(entity.getCreator().getNickName())
                .participants(participants)
                .build();
    }
    
    // 참여자 Entity를 Response로 변환
    private ParticipantResponse convertToParticipantResponse(GatheringParticipantEntity entity) {
        return ParticipantResponse.builder()
                .participantId(entity.getParticipantId())
                .userId(entity.getUser().getUsersId())
                .nickname(entity.getUser().getNickName())
                .profileImg(entity.getUser().getProfileImg())
                .role(entity.getRole().toString())
                .joinedAt(entity.getJoinedAt())
                .build();
    }
    
    // 사용자가 참여한 모임 목록 조회
    @Transactional(readOnly = true)
    public List<GatheringResponse> getParticipatedGatherings(Long userId) {
        List<GatheringParticipantEntity> participants = participantRepository.findByUserId(userId);
        
        return participants.stream()
                .map(participant -> convertToResponse(participant.getGathering()))
                .collect(Collectors.toList());
    }
    
    // 모든 모임의 참여자 수를 실제 참여자 수로 동기화
    @Transactional
    public void syncAllGatheringParticipants() {
        List<GatheringEntity> allGatherings = gatheringRepository.findAll();
        System.out.println("=== 참여자 수 동기화 시작 ===");
        System.out.println("총 모임 수: " + allGatherings.size());
        
        for (GatheringEntity gathering : allGatherings) {
            long actualParticipantCount = participantRepository.countByGatheringId(gathering.getGatheringId());
            int oldCount = gathering.getCurrentParticipants();
            
            System.out.println("모임 ID: " + gathering.getGatheringId() + 
                             ", 제목: " + gathering.getTitle() + 
                             ", 기존 참여자 수: " + oldCount + 
                             ", 실제 참여자 수: " + actualParticipantCount);
            
            gathering.setCurrentParticipants((int) actualParticipantCount);
            gatheringRepository.save(gathering);
        }
        System.out.println("=== 참여자 수 동기화 완료 ===");
    }
    
    // 주간 모임의 다음 일정 계산
    private LocalDateTime calculateNextWeeklyMeeting(String dayOfWeek, LocalTime meetingTime) {
        System.out.println("calculateNextWeeklyMeeting 호출됨: dayOfWeek=" + dayOfWeek + ", meetingTime=" + meetingTime);
        
        try {
            DayOfWeek targetDay = DayOfWeek.valueOf(dayOfWeek.toUpperCase());
            LocalDate today = LocalDate.now();
            
            System.out.println("targetDay: " + targetDay + ", today: " + today);
            
            // 오늘부터 다음 해당 요일까지의 날짜 계산
            LocalDate nextMeetingDate = today.with(TemporalAdjusters.nextOrSame(targetDay));
            
            System.out.println("nextMeetingDate (첫 번째): " + nextMeetingDate);
            
            // 만약 오늘이 해당 요일이고 시간이 지났다면 다음 주로
            if (nextMeetingDate.equals(today) && meetingTime.isBefore(LocalTime.now())) {
                nextMeetingDate = today.with(TemporalAdjusters.next(targetDay));
                System.out.println("nextMeetingDate (다음 주): " + nextMeetingDate);
            }
            
            LocalDateTime result = LocalDateTime.of(nextMeetingDate, meetingTime);
            System.out.println("최종 결과: " + result);
            return result;
        } catch (Exception e) {
            System.out.println("calculateNextWeeklyMeeting 오류: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    // 월간 모임의 다음 일정 계산 (매월 첫 번째 해당 요일)
    private LocalDateTime calculateNextMonthlyMeeting(String dayOfWeek, LocalTime meetingTime) {
        DayOfWeek targetDay = DayOfWeek.valueOf(dayOfWeek.toUpperCase());
        LocalDate today = LocalDate.now();
        
        // 이번 달의 첫 번째 해당 요일
        LocalDate thisMonthFirst = today.with(TemporalAdjusters.firstDayOfMonth())
                                       .with(TemporalAdjusters.nextOrSame(targetDay));
        
        // 이번 달의 첫 번째 해당 요일이 오늘보다 미래이면 그것을 사용
        if (thisMonthFirst.isAfter(today) || 
            (thisMonthFirst.equals(today) && meetingTime.isAfter(LocalTime.now()))) {
            return LocalDateTime.of(thisMonthFirst, meetingTime);
        } else {
            // 다음 달의 첫 번째 해당 요일
            LocalDate nextMonthFirst = today.with(TemporalAdjusters.firstDayOfNextMonth())
                                          .with(TemporalAdjusters.nextOrSame(targetDay));
            return LocalDateTime.of(nextMonthFirst, meetingTime);
        }
    }
    
    // 월간 모임의 다음 일정 계산 (주차와 요일 고려)
    private LocalDateTime calculateNextMonthlyMeetingWithWeek(String weekOfMonth, String dayOfWeek, LocalTime meetingTime) {
        DayOfWeek targetDay = DayOfWeek.valueOf(dayOfWeek.toUpperCase());
        LocalDate today = LocalDate.now();
        
        // 이번 달의 해당 주차 요일 계산
        LocalDate thisMonthDate = calculateMonthlyDate(today, weekOfMonth, targetDay);
        
        // 이번 달의 해당 날짜가 오늘보다 미래이면 그것을 사용
        if (thisMonthDate.isAfter(today) || 
            (thisMonthDate.equals(today) && meetingTime.isAfter(LocalTime.now()))) {
            return LocalDateTime.of(thisMonthDate, meetingTime);
        } else {
            // 다음 달의 해당 주차 요일
            LocalDate nextMonth = today.with(TemporalAdjusters.firstDayOfNextMonth());
            LocalDate nextMonthDate = calculateMonthlyDate(nextMonth, weekOfMonth, targetDay);
            return LocalDateTime.of(nextMonthDate, meetingTime);
        }
    }
    
    // 특정 월의 주차별 요일 계산
    private LocalDate calculateMonthlyDate(LocalDate monthStart, String weekOfMonth, DayOfWeek dayOfWeek) {
        LocalDate firstDay = monthStart.with(TemporalAdjusters.firstDayOfMonth());
        LocalDate firstTargetDay = firstDay.with(TemporalAdjusters.nextOrSame(dayOfWeek));
        
        switch (weekOfMonth.toUpperCase()) {
            case "FIRST":
                return firstTargetDay;
            case "SECOND":
                return firstTargetDay.plusWeeks(1);
            case "THIRD":
                return firstTargetDay.plusWeeks(2);
            case "FOURTH":
                return firstTargetDay.plusWeeks(3);
            case "LAST":
                // 마지막 주의 해당 요일
                LocalDate lastDay = monthStart.with(TemporalAdjusters.lastDayOfMonth());
                LocalDate lastTargetDay = lastDay.with(TemporalAdjusters.previousOrSame(dayOfWeek));
                return lastTargetDay;
            default:
                return firstTargetDay;
        }
    }
    
    // 사용자 정의 일정 파싱 (간단한 날짜 형식 지원)
    private LocalDateTime parseCustomSchedule(String scheduleDetails) {
        if (scheduleDetails == null || scheduleDetails.trim().isEmpty()) {
            return null;
        }
        
        // 다양한 날짜 형식 시도
        String[] dateFormats = {
            "yyyy-MM-dd HH:mm",
            "yyyy-MM-dd",
            "MM/dd/yyyy HH:mm",
            "MM/dd/yyyy",
            "yyyy.MM.dd HH:mm",
            "yyyy.MM.dd"
        };
        
        for (String format : dateFormats) {
            try {
                if (format.contains("HH:mm")) {
                    return LocalDateTime.parse(scheduleDetails.trim(), 
                        java.time.format.DateTimeFormatter.ofPattern(format));
                } else {
                    LocalDate date = LocalDate.parse(scheduleDetails.trim(), 
                        java.time.format.DateTimeFormatter.ofPattern(format));
                    return date.atStartOfDay();
                }
            } catch (Exception e) {
                // 다음 형식 시도
            }
        }
        
        // 파싱 실패 시 null 반환
        return null;
    }
    
    // 기존 모임들의 nextMeetingDate 업데이트
    @Transactional
    public void updateExistingGatheringsSchedule() {
        List<GatheringEntity> gatherings = gatheringRepository.findAll();
        System.out.println("=== 기존 모임 일정 업데이트 시작 ===");
        System.out.println("총 모임 수: " + gatherings.size());
        
        for (GatheringEntity gathering : gatherings) {
            System.out.println("모임 ID: " + gathering.getGatheringId() + 
                             ", 제목: " + gathering.getTitle() + 
                             ", scheduleType: " + gathering.getScheduleType() + 
                             ", scheduleDetails: " + gathering.getScheduleDetails() + 
                             ", nextMeetingDate: " + gathering.getNextMeetingDate());
            
            if (gathering.getNextMeetingDate() == null && gathering.getScheduleType() != null) {
                LocalDateTime nextMeetingDateTime = null;
                
                if (gathering.getScheduleType() == ScheduleType.WEEKLY && gathering.getScheduleDetails() != null) {
                    // scheduleDetails에서 요일 추출 (예: "FRIDAY")
                    try {
                        String dayOfWeek = gathering.getScheduleDetails();
                        System.out.println("주간 모임 처리: dayOfWeek = " + dayOfWeek);
                        // 기본 시간을 오후 7시로 설정 (실제로는 meetingTime이 필요하지만 기존 데이터에는 없음)
                        LocalTime defaultTime = LocalTime.of(19, 0);
                        nextMeetingDateTime = calculateNextWeeklyMeeting(dayOfWeek, defaultTime);
                        System.out.println("계산된 다음 일정: " + nextMeetingDateTime);
                    } catch (Exception e) {
                        System.out.println("주간 모임 처리 실패: " + e.getMessage());
                    }
                } else if (gathering.getScheduleType() == ScheduleType.MONTHLY && gathering.getScheduleDetails() != null) {
                    try {
                        String dayOfWeek = gathering.getScheduleDetails();
                        System.out.println("월간 모임 처리: dayOfWeek = " + dayOfWeek);
                        LocalTime defaultTime = LocalTime.of(19, 0);
                        nextMeetingDateTime = calculateNextMonthlyMeeting(dayOfWeek, defaultTime);
                        System.out.println("계산된 다음 일정: " + nextMeetingDateTime);
                    } catch (Exception e) {
                        System.out.println("월간 모임 처리 실패: " + e.getMessage());
                    }
                }
                
                if (nextMeetingDateTime != null) {
                    gathering.setNextMeetingDate(nextMeetingDateTime);
                    gatheringRepository.save(gathering);
                    System.out.println("✅ 모임 ID " + gathering.getGatheringId() + " 일정 업데이트: " + nextMeetingDateTime);
                } else {
                    System.out.println("❌ 모임 ID " + gathering.getGatheringId() + " 일정 업데이트 실패");
                }
            } else {
                System.out.println("⏭️ 모임 ID " + gathering.getGatheringId() + " 건너뛰기 (이미 일정 있음 또는 scheduleType 없음)");
            }
        }
        System.out.println("=== 기존 모임 일정 업데이트 완료 ===");
    }
    
    // 매일 일정 업데이트 (수동 호출용)
    @Transactional
    public void updateDailySchedules() {
        System.out.println("=== 매일 일정 업데이트 시작 (수동) ===");
        
        // 모든 활성 모임 조회
        List<GatheringEntity> gatherings = gatheringRepository.findAll();
        
        for (GatheringEntity gathering : gatherings) {
            if (gathering.getScheduleType() == null || gathering.getNextMeetingDate() == null) {
                continue;
            }
            
            // 현재 일정이 지났는지 확인
            if (gathering.getNextMeetingDate().isBefore(LocalDateTime.now())) {
                System.out.println("모임 ID " + gathering.getGatheringId() + " 일정이 지났음: " + gathering.getNextMeetingDate());
                
                LocalDateTime nextMeetingDateTime = null;
                
                if (gathering.getScheduleType() == ScheduleType.WEEKLY && gathering.getScheduleDetails() != null) {
                    try {
                        String dayOfWeek = gathering.getScheduleDetails();
                        LocalTime defaultTime = LocalTime.of(19, 0); // 기본 시간 7시
                        nextMeetingDateTime = calculateNextWeeklyMeeting(dayOfWeek, defaultTime);
                        System.out.println("다음 주간 일정 계산: " + nextMeetingDateTime);
                    } catch (Exception e) {
                        System.out.println("주간 모임 계산 실패: " + e.getMessage());
                    }
                } else if (gathering.getScheduleType() == ScheduleType.MONTHLY && gathering.getScheduleDetails() != null) {
                    try {
                        // 매월 모임의 경우 scheduleDetails에 요일이 저장되어 있음
                        String dayOfWeek = gathering.getScheduleDetails();
                        LocalTime defaultTime = LocalTime.of(19, 0); // 기본 시간 7시
                        nextMeetingDateTime = calculateNextMonthlyMeeting(dayOfWeek, defaultTime);
                        System.out.println("다음 월간 일정 계산: " + nextMeetingDateTime);
                    } catch (Exception e) {
                        System.out.println("월간 모임 계산 실패: " + e.getMessage());
                    }
                }
                
                if (nextMeetingDateTime != null) {
                    gathering.setNextMeetingDate(nextMeetingDateTime);
                    gatheringRepository.save(gathering);
                    System.out.println("✅ 모임 ID " + gathering.getGatheringId() + " 다음 일정 업데이트: " + nextMeetingDateTime);
                }
            }
        }
        
        System.out.println("=== 매일 일정 업데이트 완료 (수동) ===");
    }
}
