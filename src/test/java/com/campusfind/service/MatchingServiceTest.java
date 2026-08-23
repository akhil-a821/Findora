package com.campusfind.service;

import com.campusfind.entity.Match;
import com.campusfind.entity.Report;
import com.campusfind.entity.User;
import com.campusfind.entity.enums.ReportStatus;
import com.campusfind.entity.enums.ReportType;
import com.campusfind.repository.MatchRepository;
import com.campusfind.repository.ReportRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MatchingServiceTest {

    @Mock
    private ReportRepository reportRepository;

    @Mock
    private MatchRepository matchRepository;

    @Mock
    private NotificationService notificationService;

    private MatchingService matchingService;

    private User lostUser;
    private User foundUser;

    @BeforeEach
    void setUp() {
        matchingService = new MatchingService(matchRepository, reportRepository, notificationService);

        lostUser = new User();
        lostUser.setId(1L);
        lostUser.setName("Alice");
        lostUser.setEmail("alice@campus.edu");

        foundUser = new User();
        foundUser.setId(2L);
        foundUser.setName("Bob");
        foundUser.setEmail("bob@campus.edu");
    }

    @Test
    @DisplayName("Should generate a Strong Match (>= 80 pts) for identical category, location, date, brand, color and title")
    void testPerfectMatch() {
        Report lostReport = new Report();
        lostReport.setId(10L);
        lostReport.setUser(lostUser);
        lostReport.setType(ReportType.LOST);
        lostReport.setItemName("Apple AirPods Pro");
        lostReport.setCategory("Electronics");
        lostReport.setLocation("Library");
        lostReport.setDate(LocalDate.now());
        lostReport.setBrand("Apple");
        lostReport.setColor("White");
        lostReport.setDescription("Lost in 2nd floor silent study room");
        lostReport.setStatus(ReportStatus.ACTIVE);

        Report foundReport = new Report();
        foundReport.setId(20L);
        foundReport.setUser(foundUser);
        foundReport.setType(ReportType.FOUND);
        foundReport.setItemName("Apple AirPods Pro");
        foundReport.setCategory("Electronics");
        foundReport.setLocation("Library");
        foundReport.setDate(LocalDate.now());
        foundReport.setBrand("Apple");
        foundReport.setColor("White");
        foundReport.setDescription("Found near 2nd floor desk");
        foundReport.setStatus(ReportStatus.ACTIVE);

        when(reportRepository.findByTypeOrderByCreatedAtDesc(ReportType.FOUND)).thenReturn(List.of(foundReport));
        when(matchRepository.findByLostReportAndFoundReport(any(), any())).thenReturn(java.util.Optional.empty());
        when(matchRepository.save(any(Match.class))).thenAnswer(invocation -> invocation.getArgument(0));

        matchingService.findAndSaveMatchesForReport(lostReport);

        verify(matchRepository, times(1)).save(any(Match.class));
        verify(notificationService, times(2)).createNotification(any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("Should not create match if items belong to the same user")
    void testSameUserMatchIgnored() {
        Report lostReport = new Report();
        lostReport.setId(10L);
        lostReport.setUser(lostUser);
        lostReport.setType(ReportType.LOST);
        lostReport.setItemName("Keys");
        lostReport.setCategory("Keys");
        lostReport.setLocation("Canteen");
        lostReport.setDate(LocalDate.now());
        lostReport.setStatus(ReportStatus.ACTIVE);

        Report foundReport = new Report();
        foundReport.setId(20L);
        foundReport.setUser(lostUser); // Same user
        foundReport.setType(ReportType.FOUND);
        foundReport.setItemName("Keys");
        foundReport.setCategory("Keys");
        foundReport.setLocation("Canteen");
        foundReport.setDate(LocalDate.now());
        foundReport.setStatus(ReportStatus.ACTIVE);

        when(reportRepository.findByTypeOrderByCreatedAtDesc(ReportType.FOUND)).thenReturn(List.of(foundReport));

        matchingService.findAndSaveMatchesForReport(lostReport);

        verify(matchRepository, never()).save(any(Match.class));
    }

    @Test
    @DisplayName("Should not create match if score is below 50")
    void testLowScoreMatchIgnored() {
        Report lostReport = new Report();
        lostReport.setId(10L);
        lostReport.setUser(lostUser);
        lostReport.setType(ReportType.LOST);
        lostReport.setItemName("Water Bottle");
        lostReport.setCategory("Other");
        lostReport.setLocation("Playground");
        lostReport.setDate(LocalDate.now().minusDays(20));
        lostReport.setDescription("Blue bottle");
        lostReport.setStatus(ReportStatus.ACTIVE);

        Report foundReport = new Report();
        foundReport.setId(20L);
        foundReport.setUser(foundUser);
        foundReport.setType(ReportType.FOUND);
        foundReport.setItemName("Scientific Calculator");
        foundReport.setCategory("Electronics");
        foundReport.setLocation("Auditorium");
        foundReport.setDate(LocalDate.now());
        foundReport.setDescription("Casio FX");
        foundReport.setStatus(ReportStatus.ACTIVE);

        when(reportRepository.findByTypeOrderByCreatedAtDesc(ReportType.FOUND)).thenReturn(List.of(foundReport));

        matchingService.findAndSaveMatchesForReport(lostReport);

        verify(matchRepository, never()).save(any(Match.class));
    }
}
