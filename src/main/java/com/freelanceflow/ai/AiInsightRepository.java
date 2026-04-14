package com.freelanceflow.ai;

import com.freelanceflow.common.enums.InsightType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface AiInsightRepository extends JpaRepository<AiInsight, Long> {

    Page<AiInsight> findByUserId(Long userId, Pageable pageable);

    Page<AiInsight> findByUserIdAndInsightType(Long userId, InsightType type, Pageable pageable);

    @Modifying
    @Query("DELETE FROM AiInsight a WHERE a.validUntil < :date")
    void deleteByValidUntilBefore(@Param("date") LocalDate date);
}
