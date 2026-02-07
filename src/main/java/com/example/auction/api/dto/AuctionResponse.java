package com.example.auction.api.dto;

public class AuctionResponse {

    private String campaignId;
    private double price;

    public AuctionResponse() { }

    public AuctionResponse(String campaignId, double price) {
        this.campaignId = campaignId;
        this.price = price;
    }

    public String getCampaignId() { return campaignId; }
    public void setCampaignId(String campaignId) { this.campaignId = campaignId; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
}
