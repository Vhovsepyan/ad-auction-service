package com.example.auction.api;

import com.example.auction.api.dto.AuctionRequest;
import com.example.auction.api.dto.AuctionResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping
public class AuctionController {

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "ts", Instant.now().toString()
        ));
    }

    @PostMapping("/auction")
    public ResponseEntity<AuctionResponse> auction(@Valid @RequestBody AuctionRequest request) {
        // Milestone A: hardcoded response (we’ll replace with real logic next)
        AuctionResponse response = new AuctionResponse("cmp-42", 0.87);
        return ResponseEntity.ok(response);
    }
}
