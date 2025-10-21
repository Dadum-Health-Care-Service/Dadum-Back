package com.project.mog.service;

import com.project.mog.repository.gathering.GatheringEntity;
import com.project.mog.repository.gathering.GatheringRepository;
import com.project.mog.repository.gathering.ScheduleType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@Service
public class ScheduleUpdateService {

    @Autowired
    private GatheringRepository gatheringRepository;

    // 매일 자정에 실행되어 다음 일정을 업데이트
    @Scheduled(cron = "0 0 0 * * ?") // 매일 자정
    @Transactional
    public void updateDailySchedules() {
        System.out.println("=== 매일 일정 업데이트 시작 ===");
        
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
        
        System.out.println("=== 매일 일정 업데이트 완료 ===");
    }
    
    // 주간 모임의 다음 일정 계산
    private LocalDateTime calculateNextWeeklyMeeting(String dayOfWeek, LocalTime meetingTime) {
        try {
            DayOfWeek targetDay = DayOfWeek.valueOf(dayOfWeek.toUpperCase());
            LocalDate today = LocalDate.now();
            
            // 다음 해당 요일 계산
            LocalDate nextMeetingDate = today.with(TemporalAdjusters.next(targetDay));
            
            return LocalDateTime.of(nextMeetingDate, meetingTime);
        } catch (Exception e) {
            System.out.println("calculateNextWeeklyMeeting 오류: " + e.getMessage());
            return null;
        }
    }
    
    // 월간 모임의 다음 일정 계산
    private LocalDateTime calculateNextMonthlyMeeting(String dayOfWeek, LocalTime meetingTime) {
        try {
            DayOfWeek targetDay = DayOfWeek.valueOf(dayOfWeek.toUpperCase());
            LocalDate today = LocalDate.now();
            
            // 다음 달의 첫 번째 해당 요일
            LocalDate nextMonthFirst = today.with(TemporalAdjusters.firstDayOfNextMonth())
                                          .with(TemporalAdjusters.nextOrSame(targetDay));
            
            return LocalDateTime.of(nextMonthFirst, meetingTime);
        } catch (Exception e) {
            System.out.println("calculateNextMonthlyMeeting 오류: " + e.getMessage());
            return null;
        }
    }
}
