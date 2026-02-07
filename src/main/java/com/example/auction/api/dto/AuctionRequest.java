package com.example.auction.api.dto;

import jakarta.validation.constraints.NotBlank;

public class AuctionRequest {

    @NotBlank
    private String userId;

    @NotBlank
    private String geo;

    @NotBlank
    private String device;

    @NotBlank
    private String adSlot;

    public AuctionRequest() { }

    public AuctionRequest(String userId, String geo, String device, String adSlot) {
        this.userId = userId;
        this.geo = geo;
        this.device = device;
        this.adSlot = adSlot;
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getGeo() { return geo; }
    public void setGeo(String geo) { this.geo = geo; }

    public String getDevice() { return device; }
    public void setDevice(String device) { this.device = device; }

    public String getAdSlot() { return adSlot; }
    public void setAdSlot(String adSlot) { this.adSlot = adSlot; }
}
