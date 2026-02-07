package com.example.auction.api;

import com.example.auction.api.dto.AuctionRequest;
import com.example.auction.api.dto.AuctionResponse;
import com.example.auction.auction.AuctionService;
import com.example.auction.auction.OverloadedException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping
public class AuctionController {

    private final AuctionService auctionService;

    public AuctionController(AuctionService auctionService) {
        this.auctionService = auctionService;
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "ts", Instant.now().toString()
        ));
    }

    @PostMapping("/auction")
    public ResponseEntity<AuctionResponse> auction(@Valid @RequestBody AuctionRequest request) {
        AuctionResponse response = auctionService.runAuction(request);
        return ResponseEntity.ok(response);
    }

    @ExceptionHandler(OverloadedException.class)
    public ResponseEntity<Map<String, Object>> handleOverload(OverloadedException ex) {
        // ✅ Fail fast under saturation
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of(
                "error", "OVERLOADED",
                "message", ex.getMessage(),
                "ts", Instant.now().toString()
        ));
    }
}
