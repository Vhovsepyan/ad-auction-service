package com.example.auction.campaign;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CampaignRepository extends JpaRepository<Campaign, Long> {
    List<Campaign> findByGeoAndAdSlotAndActiveTrue(String geo, String adSlot);
}
