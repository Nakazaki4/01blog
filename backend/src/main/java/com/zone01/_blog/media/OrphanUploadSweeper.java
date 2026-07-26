package com.zone01._blog.media;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class OrphanUploadSweeper {

    private static final Duration ORPHAN_GRACE = Duration.ofHours(2);

    private final PendingUploadRepository pendingUploads;
    private final MediaService media;

    public OrphanUploadSweeper(PendingUploadRepository pendingUploads, MediaService media) {
        this.pendingUploads = pendingUploads;
        this.media = media;
    }

    @Scheduled(fixedDelay = 60 * 60 * 1000L, initialDelay = 5 * 60 * 1000L)
    @Transactional
    public void sweep() {
        Instant cutoff = Instant.now().minus(ORPHAN_GRACE);
        List<PendingUpload> orphans = pendingUploads.findByCreatedAtBefore(cutoff);
        for (PendingUpload orphan : orphans) {
            media.delete(orphan.getUrl());
            pendingUploads.delete(orphan);
        }
    }
}
