package com.project.mog.service;

import com.project.mog.dto.vote.VoteCountResponse;
import com.project.mog.dto.vote.VoteRequest;
import com.project.mog.repository.vote.VoteEntity;
import com.project.mog.repository.vote.VoteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class VoteService {
    
    private final VoteRepository voteRepository;
    
    @Transactional
    public void vote(Long userId, VoteRequest request) {
        log.info("투표 요청: userId={}, placeUrl={}, upvote={}", 
                userId, request.getPlaceUrl(), request.getUpvote());
        
        // 기존 투표 확인
        Optional<VoteEntity> existingVote = voteRepository.findByUserIdAndPlaceUrlAndIsActiveTrue(
                userId, request.getPlaceUrl());
        
        if (existingVote.isPresent()) {
            VoteEntity vote = existingVote.get();
            // 같은 투표면 취소, 다른 투표면 변경
            if (vote.getIsUpvote().equals(request.getUpvote())) {
                // 투표 취소
                vote.setIsActive(false);
                voteRepository.save(vote);
                log.info("투표 취소: userId={}, placeUrl={}", userId, request.getPlaceUrl());
            } else {
                // 투표 변경
                vote.setIsUpvote(request.getUpvote());
                voteRepository.save(vote);
                log.info("투표 변경: userId={}, placeUrl={}, upvote={}", 
                        userId, request.getPlaceUrl(), request.getUpvote());
            }
        } else {
            // 새 투표 생성
            VoteEntity vote = VoteEntity.builder()
                    .userId(userId)
                    .placeUrl(request.getPlaceUrl())
                    .isUpvote(request.getUpvote())
                    .isActive(true)
                    .build();
            
            voteRepository.save(vote);
            log.info("새 투표 생성: userId={}, placeUrl={}, upvote={}", 
                    userId, request.getPlaceUrl(), request.getUpvote());
        }
    }
    
    @Transactional(readOnly = true)
    public VoteCountResponse getVoteCounts(String placeUrl) {
        log.info("투표 수 조회: placeUrl={}", placeUrl);
        
        Long upvotes = voteRepository.countUpvotesByPlaceUrl(placeUrl);
        Long downvotes = voteRepository.countDownvotesByPlaceUrl(placeUrl);
        
        return VoteCountResponse.builder()
                .placeUrl(placeUrl)
                .upvotes(upvotes)
                .downvotes(downvotes)
                .build();
    }
}
