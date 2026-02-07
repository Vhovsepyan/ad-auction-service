package com.example.auction.auction;

import com.example.auction.api.dto.AuctionRequest;
import com.example.auction.api.dto.AuctionResponse;
import com.example.auction.campaign.Campaign;
import com.example.auction.campaign.CampaignRepository;
import com.example.auction.freqcap.FrequencyCapService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

@Service
public class AuctionService {

    private final CampaignRepository campaignRepository;
    private final FrequencyCapService frequencyCapService;

    // Bulkhead: cap concurrent DB operations (protect connection pool + DB)
    private final Semaphore dbSemaphore;

    // Metrics
    private final Timer auctionTimer;
    private final Counter dbRejectedCounter;

    // Fail-fast policy
    private static final long DB_ACQUIRE_TIMEOUT_MS = 15;

    public AuctionService(CampaignRepository campaignRepository,
                          FrequencyCapService frequencyCapService,
                          MeterRegistry meterRegistry) {
        this.campaignRepository = campaignRepository;
        this.frequencyCapService = frequencyCapService;

        // Start with 20 as a reasonable default (often matches DB pool).
        // Later we tune based on pool size and load test.
        this.dbSemaphore = new Semaphore(20);

        this.auctionTimer = meterRegistry.timer("auction.latency");
        this.dbRejectedCounter = meterRegistry.counter("bulkhead.rejected", "dependency", "db");
    }

    public AuctionResponse runAuction(AuctionRequest request) {
        // Measure total auction latency (service boundary timer)
        return auctionTimer.record(() -> executeAuction(request));
    }

    private AuctionResponse executeAuction(AuctionRequest request) {
        if (!tryAcquire(dbSemaphore, DB_ACQUIRE_TIMEOUT_MS)) {
            dbRejectedCounter.increment();
            // Fail fast: do not queue infinitely, protect p99
            throw new OverloadedException("DB bulkhead saturated");
        }

        try {
            List<Campaign> candidates =
                    campaignRepository.findByGeoAndAdSlotAndActiveTrue(request.getGeo(), request.getAdSlot());

            Campaign winner = candidates.stream()
                    .filter(c -> frequencyCapService.canServe(request.getUserId(), c.getId()))
                    .max(Comparator.comparingDouble(Campaign::getBidPrice))
                    .orElse(null);

            if (winner == null) {
                // No fill
                return new AuctionResponse(null, 0.0);
            }

            // For now we record immediately.
            // Later we’ll change this to event/outbox.
            frequencyCapService.recordServe(request.getUserId(), winner.getId());

            return new AuctionResponse("cmp-" + winner.getId(), winner.getBidPrice());
        } finally {
            dbSemaphore.release();
        }
    }

    private static boolean tryAcquire(Semaphore sem, long timeoutMs) {
        try {
            return sem.tryAcquire(timeoutMs, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }
}
