package com.campusfind.service;

import com.campusfind.dto.ReportRequestDto;
import com.campusfind.entity.Report;
import com.campusfind.entity.User;
import com.campusfind.entity.enums.ReportStatus;
import com.campusfind.entity.enums.ReportType;
import com.campusfind.exception.ResourceNotFoundException;
import com.campusfind.repository.ClaimRepository;
import com.campusfind.repository.MatchRepository;
import com.campusfind.repository.ReportRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Service
public class ReportService {

    private final ReportRepository reportRepository;
    private final MatchingService matchingService;
    private final ClaimRepository claimRepository;
    private final MatchRepository matchRepository;
    private final FirebaseStorageService firebaseStorageService;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    public ReportService(ReportRepository reportRepository, MatchingService matchingService, ClaimRepository claimRepository, MatchRepository matchRepository, FirebaseStorageService firebaseStorageService) {
        this.reportRepository = reportRepository;
        this.matchingService = matchingService;
        this.claimRepository = claimRepository;
        this.matchRepository = matchRepository;
        this.firebaseStorageService = firebaseStorageService;
    }

    @Transactional
    public Report createReport(ReportRequestDto dto, User user) throws IOException {
        Report report = new Report();
        report.setUser(user);
        report.setType(dto.getType());
        report.setItemName(dto.getItemName().trim());
        report.setCategory(dto.getCategory());
        report.setDescription(dto.getDescription().trim());
        report.setBrand(dto.getBrand() != null ? dto.getBrand().trim() : null);
        report.setColor(dto.getColor() != null ? dto.getColor().trim() : null);
        report.setDate(dto.getDate());
        report.setTime(dto.getTime());
        report.setLocation(dto.getLocation());
        report.setPrivateVerificationDetails(dto.getPrivateVerificationDetails() != null ? dto.getPrivateVerificationDetails().trim() : null);
        report.setStatus(ReportStatus.ACTIVE);

        if (dto.getImageFile() != null && !dto.getImageFile().isEmpty()) {
            String imageUrl = saveUploadedFile(dto.getImageFile());
            report.setImageUrl(imageUrl);
        } else {
            report.setImageUrl(getDefaultPlaceholder(dto.getCategory()));
        }

        Report savedReport = reportRepository.save(report);

        // Automatically trigger smart matching engine
        matchingService.findAndSaveMatchesForReport(savedReport);

        return savedReport;
    }

    private String saveUploadedFile(MultipartFile file) throws IOException {
        if (firebaseStorageService.isFirebaseEnabled()) {
            try {
                return firebaseStorageService.uploadFile(file);
            } catch (Exception e) {
                // Log and fall back to local disk storage if Firebase upload fails
                org.slf4j.LoggerFactory.getLogger(ReportService.class).warn("Firebase Storage upload failed ({}), falling back to local file storage.", e.getMessage());
            }
        }

        String originalFilename = file.getOriginalFilename();
        String fileExtension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
        }

        // Validate image file extension
        if (!fileExtension.matches("\\.(jpg|jpeg|png|gif|webp)")) {
            throw new IllegalArgumentException("Only image files (JPG, PNG, GIF, WEBP) are allowed.");
        }

        String newFileName = UUID.randomUUID().toString() + fileExtension;
        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        Path filePath = uploadPath.resolve(newFileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return "/uploads/" + newFileName;
    }

    private String getDefaultPlaceholder(String category) {
        if (category == null) return "/images/placeholder-default.svg";
        switch (category.toLowerCase()) {
            case "electronics": return "/images/placeholder-electronics.svg";
            case "wallet": return "/images/placeholder-wallet.svg";
            case "keys": return "/images/placeholder-keys.svg";
            case "id card": return "/images/placeholder-id.svg";
            default: return "/images/placeholder-default.svg";
        }
    }

    public Report getReportById(Long id) {
        return reportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found with ID: " + id));
    }

    public List<Report> getAllReports() {
        return reportRepository.findAll();
    }

    public List<Report> getUserReports(User user) {
        return reportRepository.findByUserOrderByCreatedAtDesc(user);
    }

    public List<Report> searchAndFilterReports(ReportType type, String category, String location, String query) {
        String searchCategory = (category != null && !category.isBlank() && !category.equalsIgnoreCase("ALL")) ? category : null;
        String searchLocation = (location != null && !location.isBlank() && !location.equalsIgnoreCase("ALL")) ? location : null;
        String searchQuery = (query != null && !query.isBlank()) ? query.trim() : null;

        return reportRepository.searchReports(type, searchCategory, searchLocation, searchQuery);
    }

    @Transactional
    public Report updateReportStatus(Long id, ReportStatus status) {
        Report report = getReportById(id);
        report.setStatus(status);
        return reportRepository.save(report);
    }

    @Transactional
    public void deleteReport(Long id, User currentUser) {
        Report report = getReportById(id);
        if (!report.getUser().getId().equals(currentUser.getId()) && !currentUser.getRole().name().equals("ROLE_ADMIN")) {
            throw new IllegalArgumentException("You are not authorized to delete this report.");
        }
        claimRepository.deleteByReport(report);
        matchRepository.deleteByLostReportOrFoundReport(report, report);
        reportRepository.delete(report);
    }

    public long countReports() {
        return reportRepository.count();
    }

    public long countByType(ReportType type) {
        return reportRepository.countByType(type);
    }

    public long countByStatus(ReportStatus status) {
        return reportRepository.countByStatus(status);
    }

    public long countUserReportsByType(User user, ReportType type) {
        return reportRepository.countByUserAndType(user, type);
    }
}
