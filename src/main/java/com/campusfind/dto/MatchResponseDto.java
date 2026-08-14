package com.campusfind.dto;

import com.campusfind.entity.Match;
import java.util.List;

public class MatchResponseDto {
    private Long id;
    private Long lostReportId;
    private String lostItemName;
    private String lostLocation;
    private String lostImageUrl;

    private Long foundReportId;
    private String foundItemName;
    private String foundLocation;
    private String foundImageUrl;

    private int score;
    private String matchLabel;
    private List<String> matchingFactors;

    public MatchResponseDto() {}

    public static MatchResponseDto fromEntity(Match match) {
        MatchResponseDto dto = new MatchResponseDto();
        dto.setId(match.getId());
        dto.setLostReportId(match.getLostReport().getId());
        dto.setLostItemName(match.getLostReport().getItemName());
        dto.setLostLocation(match.getLostReport().getLocation());
        dto.setLostImageUrl(match.getLostReport().getImageUrl());

        dto.setFoundReportId(match.getFoundReport().getId());
        dto.setFoundItemName(match.getFoundReport().getItemName());
        dto.setFoundLocation(match.getFoundReport().getLocation());
        dto.setFoundImageUrl(match.getFoundReport().getImageUrl());

        dto.setScore(match.getScore());
        dto.setMatchLabel(match.getMatchLabel());
        dto.setMatchingFactors(match.getMatchingFactors());
        return dto;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getLostReportId() { return lostReportId; }
    public void setLostReportId(Long lostReportId) { this.lostReportId = lostReportId; }

    public String getLostItemName() { return lostItemName; }
    public void setLostItemName(String lostItemName) { this.lostItemName = lostItemName; }

    public String getLostLocation() { return lostLocation; }
    public void setLostLocation(String lostLocation) { this.lostLocation = lostLocation; }

    public String getLostImageUrl() { return lostImageUrl; }
    public void setLostImageUrl(String lostImageUrl) { this.lostImageUrl = lostImageUrl; }

    public Long getFoundReportId() { return foundReportId; }
    public void setFoundReportId(Long foundReportId) { this.foundReportId = foundReportId; }

    public String getFoundItemName() { return foundItemName; }
    public void setFoundItemName(String foundItemName) { this.foundItemName = foundItemName; }

    public String getFoundLocation() { return foundLocation; }
    public void setFoundLocation(String foundLocation) { this.foundLocation = foundLocation; }

    public String getFoundImageUrl() { return foundImageUrl; }
    public void setFoundImageUrl(String foundImageUrl) { this.foundImageUrl = foundImageUrl; }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }

    public String getMatchLabel() { return matchLabel; }
    public void setMatchLabel(String matchLabel) { this.matchLabel = matchLabel; }

    public List<String> getMatchingFactors() { return matchingFactors; }
    public void setMatchingFactors(List<String> matchingFactors) { this.matchingFactors = matchingFactors; }
}
