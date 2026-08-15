package com.campusfind.dto;

public class LeaderboardDto {
    private Long userId;
    private String name;
    private String email;
    private long itemsFound;
    private long itemsReturned;
    private long totalPoints;
    private String rankBadge;
    private int rankNumber;

    public LeaderboardDto(Long userId, String name, String email, long itemsFound, long itemsReturned, long totalPoints, String rankBadge, int rankNumber) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.itemsFound = itemsFound;
        this.itemsReturned = itemsReturned;
        this.totalPoints = totalPoints;
        this.rankBadge = rankBadge;
        this.rankNumber = rankNumber;
    }

    public Long getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public long getItemsFound() {
        return itemsFound;
    }

    public long getItemsReturned() {
        return itemsReturned;
    }

    public long getTotalPoints() {
        return totalPoints;
    }

    public String getRankBadge() {
        return rankBadge;
    }

    public int getRankNumber() {
        return rankNumber;
    }
}
