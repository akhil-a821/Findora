package com.campusfind.service;

import com.campusfind.dto.ReportRequestDto;
import com.campusfind.entity.Report;
import com.campusfind.entity.User;
import com.campusfind.entity.enums.ReportStatus;
import com.campusfind.entity.enums.ReportType;
import com.campusfind.repository.ClaimRepository;
import com.campusfind.repository.MatchRepository;
import com.campusfind.repository.ReportRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private ReportRepository reportRepository;

    @Mock
    private MatchingService matchingService;

    @Mock
    private ClaimRepository claimRepository;

    @Mock
    private MatchRepository matchRepository;

    @Mock
    private FirebaseStorageService firebaseStorageService;

    private ReportService reportService;

    private User user;

    @BeforeEach
    void setUp() {
        reportService = new ReportService(
                reportRepository,
                matchingService,
                claimRepository,
                matchRepository,
                firebaseStorageService
        );

        user = new User();
        user.setId(1L);
        user.setName("John Student");
        user.setEmail("john@campus.edu");
    }

    @Test
    @DisplayName("Should successfully create a LOST report without image")
    void testCreateReportWithoutImage() throws IOException {
        ReportRequestDto dto = new ReportRequestDto();
        dto.setType(ReportType.LOST);
        dto.setItemName("Blue Backpack");
        dto.setCategory("Bags");
        dto.setDescription("Left on bench near Main Gate");
        dto.setLocation("Main Gate");
        dto.setDate(LocalDate.now());
        dto.setBrand("Wildcraft");
        dto.setColor("Navy Blue");

        when(reportRepository.save(any(Report.class))).thenAnswer(invocation -> {
            Report r = invocation.getArgument(0);
            r.setId(50L);
            return r;
        });

        Report created = reportService.createReport(dto, user);

        assertNotNull(created);
        assertEquals("Blue Backpack", created.getItemName());
        assertEquals(ReportType.LOST, created.getType());
        assertEquals(ReportStatus.ACTIVE, created.getStatus());
        assertEquals(user, created.getUser());
        verify(reportRepository, times(1)).save(any(Report.class));
        verify(matchingService, times(1)).findAndSaveMatchesForReport(created);
    }

    @Test
    @DisplayName("Should reject invalid image file extensions")
    void testInvalidImageExtension() {
        ReportRequestDto dto = new ReportRequestDto();
        dto.setType(ReportType.FOUND);
        dto.setItemName("USB Stick");
        dto.setCategory("Electronics");
        dto.setDescription("Found in Lab");
        dto.setLocation("Computer Lab");
        dto.setDate(LocalDate.now());

        MockMultipartFile badFile = new MockMultipartFile(
                "imageFile",
                "script.exe",
                "application/x-msdownload",
                new byte[]{1, 2, 3}
        );
        dto.setImageFile(badFile);

        assertThrows(IllegalArgumentException.class, () -> {
            reportService.createReport(dto, user);
        });

        verify(reportRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should search and filter reports properly")
    void testSearchAndFilterReports() {
        Report r1 = new Report();
        r1.setItemName("iPhone 13");
        r1.setCategory("Electronics");
        r1.setLocation("Library");
        r1.setType(ReportType.LOST);

        when(reportRepository.searchReports(ReportType.LOST, "Electronics", "Library", "iphone"))
                .thenReturn(List.of(r1));

        List<Report> results = reportService.searchAndFilterReports(ReportType.LOST, "Electronics", "Library", "iphone");
        assertEquals(1, results.size());
        assertEquals("iPhone 13", results.get(0).getItemName());
    }
}
