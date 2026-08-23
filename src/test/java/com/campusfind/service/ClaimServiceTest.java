package com.campusfind.service;

import com.campusfind.dto.ClaimRequestDto;
import com.campusfind.entity.Claim;
import com.campusfind.entity.Report;
import com.campusfind.entity.User;
import com.campusfind.entity.enums.ClaimStatus;
import com.campusfind.entity.enums.ReportStatus;
import com.campusfind.entity.enums.ReportType;
import com.campusfind.entity.enums.Role;
import com.campusfind.repository.ClaimRepository;
import com.campusfind.repository.ReportRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClaimServiceTest {

    @Mock
    private ClaimRepository claimRepository;

    @Mock
    private ReportRepository reportRepository;

    @Mock
    private NotificationService notificationService;

    private ClaimService claimService;

    private User finder;
    private User claimant;
    private User stranger;
    private Report foundReport;

    @BeforeEach
    void setUp() {
        claimService = new ClaimService(claimRepository, reportRepository, notificationService);

        finder = new User();
        finder.setId(1L);
        finder.setName("Finder Bob");
        finder.setEmail("finder@campus.edu");
        finder.setRole(Role.ROLE_USER);

        claimant = new User();
        claimant.setId(2L);
        claimant.setName("Owner Alice");
        claimant.setEmail("claimant@campus.edu");
        claimant.setRole(Role.ROLE_USER);

        stranger = new User();
        stranger.setId(3L);
        stranger.setName("Stranger Charlie");
        stranger.setEmail("stranger@campus.edu");
        stranger.setRole(Role.ROLE_USER);

        foundReport = new Report();
        foundReport.setId(100L);
        foundReport.setUser(finder);
        foundReport.setType(ReportType.FOUND);
        foundReport.setItemName("MacBook Charger");
        foundReport.setStatus(ReportStatus.ACTIVE);
    }

    @Test
    @DisplayName("Should successfully create claim on found item")
    void testSubmitClaimSuccess() {
        ClaimRequestDto dto = new ClaimRequestDto();
        dto.setReportId(100L);
        dto.setExplanation("I left my charger in Library 2nd floor");
        dto.setVerificationAnswer("It has a small red dot sticker on the brick");

        when(reportRepository.findById(100L)).thenReturn(Optional.of(foundReport));
        when(claimRepository.existsByReportAndClaimant(foundReport, claimant)).thenReturn(false);
        when(claimRepository.save(any(Claim.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Claim claim = claimService.submitClaim(dto, claimant);

        assertNotNull(claim);
        assertEquals(foundReport, claim.getReport());
        assertEquals(claimant, claim.getClaimant());
        assertEquals(ClaimStatus.PENDING, claim.getStatus());
        verify(claimRepository, times(1)).save(any(Claim.class));
        verify(notificationService, times(1)).createNotification(eq(finder), any(), any(), any(), any());
    }

    @Test
    @DisplayName("Should prevent user from claiming their own found item")
    void testPreventSelfClaim() {
        ClaimRequestDto dto = new ClaimRequestDto();
        dto.setReportId(100L);
        dto.setExplanation("Trying to claim own found item");
        dto.setVerificationAnswer("Some answer");

        when(reportRepository.findById(100L)).thenReturn(Optional.of(foundReport));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            claimService.submitClaim(dto, finder);
        });

        assertTrue(ex.getMessage().contains("cannot submit a claim for your own found report"));
        verify(claimRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should prevent duplicate claims by same claimant on same report")
    void testPreventDuplicateClaim() {
        ClaimRequestDto dto = new ClaimRequestDto();
        dto.setReportId(100L);
        dto.setExplanation("Duplicate claim attempt");
        dto.setVerificationAnswer("Some answer");

        when(reportRepository.findById(100L)).thenReturn(Optional.of(foundReport));
        when(claimRepository.existsByReportAndClaimant(foundReport, claimant)).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            claimService.submitClaim(dto, claimant);
        });

        assertTrue(ex.getMessage().contains("already submitted a claim"));
        verify(claimRepository, never()).save(any());
    }

    @Test
    @DisplayName("Only finder can approve claim")
    void testOnlyFinderCanApprove() {
        Claim claim = new Claim();
        claim.setId(10L);
        claim.setReport(foundReport);
        claim.setClaimant(claimant);
        claim.setStatus(ClaimStatus.PENDING);

        when(claimRepository.findById(10L)).thenReturn(Optional.of(claim));
        when(claimRepository.save(any(Claim.class))).thenAnswer(i -> i.getArgument(0));

        // Unauthorized user cannot approve
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            claimService.approveClaim(10L, stranger);
        });
        assertTrue(ex.getMessage().contains("Only the finder"));

        // Finder CAN approve
        Claim approvedClaim = claimService.approveClaim(10L, finder);
        assertEquals(ClaimStatus.APPROVED, approvedClaim.getStatus());
        assertEquals(ReportStatus.CLAIMED, foundReport.getStatus());
        verify(reportRepository, times(1)).save(foundReport);
    }

    @Test
    @DisplayName("Only finder or claimant/admin can mark item as returned")
    void testMarkAsReturnedAuthorization() {
        when(reportRepository.findById(100L)).thenReturn(Optional.of(foundReport));

        // Stranger cannot mark as returned
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            claimService.markItemAsReturned(100L, stranger);
        });
        assertTrue(ex.getMessage().contains("not authorized"));

        // Finder CAN mark as returned
        claimService.markItemAsReturned(100L, finder);
        assertEquals(ReportStatus.RETURNED, foundReport.getStatus());
        verify(reportRepository, times(1)).save(foundReport);
    }
}
