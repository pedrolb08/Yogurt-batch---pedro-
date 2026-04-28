package com.pedro.demo.demain.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.pedro.demo.domain.model.TemperaturaLog;
import com.pedro.demo.domain.model.YogurtBatch;

@Repository
public interface TemperaturaLogRepository extends JpaRepository<TemperaturaLog, Long> {
    
    List<TemperaturaLog> findByBatch(YogurtBatch batch);
    
    List<TemperaturaLog> findByBatchAndTypeOrderByRecordedAtDesc(Long batchId, TemperaturaLog.LogType type);
    
    @Query("SELECT tl FROM TemperaturaLog tl WHERE tl.batch.id = :batchId AND tl.recordedAt BETWEEN :startTime AND :endTime")
    List<TemperaturaLog> findByBatchAndTimeRange(@Param("batchId") Long batchId, 
                                                  @Param("startTime") LocalDateTime startTime, 
                                                  @Param("endTime") LocalDateTime endTime);
    
    @Query("SELECT AVG(tl.temperature) FROM TemperaturaLog tl WHERE tl.batch.id = :batchId AND tl.type = :type")
    Double getAverageTemperatureByBatchAndType(@Param("batchId") Long batchId, @Param("type") TemperaturaLog.LogType type);
    
    @Query("SELECT MAX(tl.temperature) FROM TemperaturaLog tl WHERE tl.batch.id = :batchId")
    Double getMaxTemperatureByBatch(@Param("batchId") Long batchId);
    
    @Query("SELECT MIN(tl.temperature) FROM TemperaturaLog tl WHERE tl.batch.id = :batchId")
    Double getMinTemperatureByBatch(@Param("batchId") Long batchId);
}


