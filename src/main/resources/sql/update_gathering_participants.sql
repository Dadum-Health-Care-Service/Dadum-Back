-- 기존 모임들의 currentParticipants를 실제 참여자 수로 업데이트
UPDATE gatherings g
SET current_participants = (
    SELECT COUNT(*)
    FROM gathering_participants gp
    WHERE gp.gathering_id = g.gathering_id
)
WHERE g.gathering_id IN (
    SELECT DISTINCT gathering_id
    FROM gathering_participants
);

-- 업데이트 결과 확인
SELECT 
    g.gathering_id,
    g.title,
    g.current_participants,
    COUNT(gp.participant_id) as actual_participants
FROM gatherings g
LEFT JOIN gathering_participants gp ON g.gathering_id = gp.gathering_id
GROUP BY g.gathering_id, g.title, g.current_participants
ORDER BY g.gathering_id;
