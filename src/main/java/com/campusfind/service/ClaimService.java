package com.campusfind.service;

import com.campusfind.dto.ClaimRequestDto;
import com.campusfind.entity.Claim;
import com.campusfind.entity.Report;
import com.campusfind.entity.User;
import com.campusfind.entity.enums.ClaimStatus;
import com.campusfind.entity.enums.NotificationType;
import com.campusfind.entity.enums.ReportStatus;
import com.campusfind.exception.ResourceNotFoundException;
import com.campusfind.repository.ClaimRepository;
import com.campusfind.repository.ReportRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ClaimService {

    private final ClaimRepository claimRepository;
    private final ReportRepository reportRepository;
    private final NotificationService notificationService;

    public ClaimService(ClaimRepository claimRepository, ReportRepository reportRepository, NotificationService notificationService) {
        this.claimRepository = claimRepository;
        this.reportRepository = reportRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public Claim submitClaim(ClaimRequestDto dto, User claimant) {
        Report report = reportRepository.findById(dto.getReportId())
                .orElseThrow(() -> new ResourceNotFoundException("Report not found with ID: " + dto.getReportId()));

        if (report.getUser().getId().equals(claimant.getId())) {
            throw new IllegalArgumentException("You cannot submit a claim for your own found report.");
        }

        if (claimRepository.existsByReportAndClaimant(report, claimant)) {
            throw new IllegalArgumentException("You have already submitted a claim for this item.");
        }

        Claim claim = new Claim(report, claimant, dto.getExplanation().trim(), dto.getVerificationAnswer().trim());
        Claim savedClaim = claimRepository.save(claim);

        // Notify the finder of the new claim
        notificationService.createNotification(
                report.getUser(),
                "New Claim Submitted 🎉",
                claimant.getName() + " has submitted a claim for your found item '" + report.getItemName() + "'.",
                NotificationType.CLAIM_SUBMITTED,
                "/my-claims"
        );

        return savedClaim;
    }

    @Transactional
    public Claim approveClaim(Long claimId, User finder) {
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new ResourceNotFoundException("Claim not found with ID: " + claimId));

        Report report = claim.getReport();
        if (!report.getUser().getId().equals(finder.getId())) {
            throw new IllegalArgumentException("Only the finder who reported this item can approve claims.");
        }

        claim.setStatus(ClaimStatus.APPROVED);
        report.setStatus(ReportStatus.CLAIMED);
        reportRepository.save(report);
        Claim savedClaim = claimRepository.save(claim);

        // Notify the claimant
        notificationService.createNotification(
                claim.getClaimant(),
                "Claim Approved! 🎉",
                "Your claim for '" + report.getItemName() + "' was approved by " + finder.getName() + ". You can now coordinate returning the item.",
                NotificationType.CLAIM_APPROVED,
                "/my-claims"
        );

        return savedClaim;
    }

    @Transactional
    public Claim rejectClaim(Long claimId, User finder) {
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new ResourceNotFoundException("Claim not found with ID: " + claimId));

        Report report = claim.getReport();
        if (!report.getUser().getId().equals(finder.getId())) {
            throw new IllegalArgumentException("Only the finder who reported this item can reject claims.");
        }

        claim.setStatus(ClaimStatus.REJECTED);
        Claim savedClaim = claimRepository.save(claim);

        // Notify claimant
        notificationService.createNotification(
                claim.getClaimant(),
                "Claim Status Update",
                "Your claim for '" + report.getItemName() + "' was not verified by the finder.",
                NotificationType.CLAIM_REJECTED,
                "/my-claims"
        );

        return savedClaim;
    }

    @Transactional
    public Report markItemAsReturned(Long reportId, User currentUser) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found with ID: " + reportId));

        if (!report.getUser().getId().equals(currentUser.getId()) && !currentUser.getRole().name().equals("ROLE_ADMIN")) {
            throw new IllegalArgumentException("You are not authorized to mark this item as returned.");
        }

        report.setStatus(ReportStatus.RETURNED);
        Report updated = reportRepository.save(report);

        // Notify claims users if applicable
        List<Claim> claims = claimRepository.findByReportOrderByCreatedAtDesc(report);
        for (Claim c : claims) {
            if (c.getStatus() == ClaimStatus.APPROVED) {
                notificationService.createNotification(
                        c.getClaimant(),
                        "Item Returned ❤️",
                        "The item '" + report.getItemName() + "' has been marked as RETURNED. Thank you for using Findora!",
                        NotificationType.ITEM_RETURNED,
                        "/dashboard"
                );
            }
        }

        notificationService.createNotification(
                report.getUser(),
                "Item Returned ❤️",
                "Your report for '" + report.getItemName() + "' is now marked as RETURNED. Item successfully reunited with owner!",
                NotificationType.ITEM_RETURNED,
                "/dashboard"
        );

        return updated;
    }

    public boolean hasUserClaimedReport(Report report, User claimant) {
        if (report == null || claimant == null) return false;
        return claimRepository.existsByReportAndClaimant(report, claimant);
    }

    public List<Claim> getUserSubmittedClaims(User claimant) {
        return claimRepository.findByClaimantOrderByCreatedAtDesc(claimant);
    }

    public List<Claim> getClaimsReceivedForFinder(User finder) {
        return claimRepository.findClaimsForFinder(finder);
    }

    public long countPendingClaims() {
        return claimRepository.countByStatus(ClaimStatus.PENDING);
    }
}
