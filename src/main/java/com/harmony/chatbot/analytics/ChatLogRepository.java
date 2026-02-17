package com.harmony.chatbot.analytics;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ChatLogRepository extends JpaRepository<ChatLogEntity, Long> {

    Page<ChatLogEntity> findAllByOrderByAskedAtDesc(Pageable pageable);

    List<ChatLogEntity> findBySessionIdOrderByAskedAtAsc(String sessionId);

    @Query("SELECT DISTINCT c.sessionId FROM ChatLogEntity c ORDER BY c.sessionId DESC")
    List<String> findDistinctSessionIds(Pageable pageable);

    @Query("SELECT c.question, COUNT(c) as freq FROM ChatLogEntity c GROUP BY c.question ORDER BY freq DESC")
    List<Object[]> findTopQuestions(Pageable pageable);

    @Query("SELECT CAST(c.askedAt AS date), COUNT(c) FROM ChatLogEntity c GROUP BY CAST(c.askedAt AS date) ORDER BY CAST(c.askedAt AS date) DESC")
    List<Object[]> findQuestionsPerDay(Pageable pageable);

    @Query("SELECT COUNT(c) FROM ChatLogEntity c WHERE c.rating = 1")
    long countPositiveRatings();

    @Query("SELECT COUNT(c) FROM ChatLogEntity c WHERE c.rating = -1")
    long countNegativeRatings();
}
