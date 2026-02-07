package com.example.auction.campaign;

import jakarta.persistence.*;

@Entity
@Table(name = "campaign")
public class Campaign {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String geo; // e.g. "AM"

    @Column(nullable = false)
    private String adSlot; // e.g. "banner"

    @Column(nullable = false)
    private double bidPrice;

    @Column(nullable = false)
    private boolean active = true;

    public Campaign() {}

    public Campaign(String geo, String adSlot, double bidPrice, boolean active) {
        this.geo = geo;
        this.adSlot = adSlot;
        this.bidPrice = bidPrice;
        this.active = active;
    }

    public Long getId() { return id; }
    public String getGeo() { return geo; }
    public void setGeo(String geo) { this.geo = geo; }
    public String getAdSlot() { return adSlot; }
    public void setAdSlot(String adSlot) { this.adSlot = adSlot; }
    public double getBidPrice() { return bidPrice; }
    public void setBidPrice(double bidPrice) { this.bidPrice = bidPrice; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
