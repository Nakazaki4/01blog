package com.zone01._blog.media;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PendingUploadRepository extends JpaRepository<PendingUpload, Long> {

    List<PendingUpload> findByCreatedAtBefore(Instant cutoff);

    @Modifying
    @Query("delete from PendingUpload p where p.url in :urls")
    void deleteByUrlIn(@Param("urls") Collection<String> urls);
}
