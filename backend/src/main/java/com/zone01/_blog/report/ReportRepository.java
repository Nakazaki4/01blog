package com.zone01._blog.report;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReportRepository extends JpaRepository<Report, Long> {

    Page<Report> findByStatus(ReportStatus status, Pageable pageable);

    long countByStatus(ReportStatus status);

    long countByReportedUserId(Long userId);

    @Modifying
    @Query("UPDATE Report r SET r.status = :newStatus WHERE r.id = :reportId")
    void updateStatus(@Param("reportId") Long reportId, @Param("newStatus") ReportStatus newStatus);
}
