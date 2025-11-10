package com.project.mog.repository.gathering;

/**
 * 모임 일정 유형을 나타내는 열거형
 */
public enum ScheduleType {
    ONE_TIME("일회성"),
    WEEKLY("매주"),
    MONTHLY("매월"),
    CUSTOM("사용자 정의");

    private final String description;

    ScheduleType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
