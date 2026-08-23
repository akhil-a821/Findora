package com.campusfind.service;

import com.campusfind.dto.LeaderboardDto;
import com.campusfind.dto.UserRegistrationDto;
import com.campusfind.entity.Report;
import com.campusfind.entity.User;
import com.campusfind.entity.enums.ReportStatus;
import com.campusfind.entity.enums.ReportType;
import com.campusfind.entity.enums.Role;
import com.campusfind.repository.ReportRepository;
import com.campusfind.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ReportRepository reportRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, reportRepository, passwordEncoder);
    }

    @Test
    @DisplayName("Should successfully register a new user")
    void testRegisterUserSuccess() {
        UserRegistrationDto dto = new UserRegistrationDto();
        dto.setName("Sam Miller");
        dto.setEmail("sam@campus.edu");
        dto.setPassword("Secret123!");
        dto.setConfirmPassword("Secret123!");

        when(userRepository.existsByEmail("sam@campus.edu")).thenReturn(false);
        when(passwordEncoder.encode("Secret123!")).thenReturn("encoded_Secret123!");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(10L);
            return u;
        });

        User user = userService.registerUser(dto);

        assertNotNull(user);
        assertEquals("Sam Miller", user.getName());
        assertEquals("sam@campus.edu", user.getEmail());
        assertEquals("encoded_Secret123!", user.getPassword());
        assertEquals(Role.ROLE_USER, user.getRole());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Should reject registration if email is duplicate")
    void testRegisterDuplicateEmail() {
        UserRegistrationDto dto = new UserRegistrationDto();
        dto.setName("Sam Miller");
        dto.setEmail("existing@campus.edu");
        dto.setPassword("Secret123!");
        dto.setConfirmPassword("Secret123!");

        when(userRepository.existsByEmail("existing@campus.edu")).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            userService.registerUser(dto);
        });

        assertTrue(ex.getMessage().contains("already exists"));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should reject registration if passwords do not match")
    void testRegisterPasswordMismatch() {
        UserRegistrationDto dto = new UserRegistrationDto();
        dto.setName("Sam Miller");
        dto.setEmail("sam@campus.edu");
        dto.setPassword("Password123");
        dto.setConfirmPassword("MismatchPass");

        when(userRepository.existsByEmail("sam@campus.edu")).thenReturn(false);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            userService.registerUser(dto);
        });

        assertTrue(ex.getMessage().contains("Passwords do not match"));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should calculate Leaderboard points and rankings correctly")
    void testLeaderboardCalculation() {
        User u1 = new User();
        u1.setId(1L);
        u1.setName("Finder One");
        u1.setEmail("one@campus.edu");

        User u2 = new User();
        u2.setId(2L);
        u2.setName("Finder Two");
        u2.setEmail("two@campus.edu");

        // u1 has 2 found items, 1 returned -> 25*2 + 100*1 = 150 pts
        Report r1 = new Report();
        r1.setType(ReportType.FOUND);
        r1.setStatus(ReportStatus.RETURNED);

        Report r2 = new Report();
        r2.setType(ReportType.FOUND);
        r2.setStatus(ReportStatus.ACTIVE);

        // u2 has 1 found item, 0 returned -> 25*1 = 25 pts
        Report r3 = new Report();
        r3.setType(ReportType.FOUND);
        r3.setStatus(ReportStatus.ACTIVE);

        when(userRepository.findAll()).thenReturn(List.of(u1, u2));
        when(reportRepository.findByUserOrderByCreatedAtDesc(u1)).thenReturn(List.of(r1, r2));
        when(reportRepository.findByUserOrderByCreatedAtDesc(u2)).thenReturn(List.of(r3));

        List<LeaderboardDto> leaderboard = userService.getTopFindersLeaderboard();

        assertEquals(2, leaderboard.size());
        assertEquals("Finder One", leaderboard.get(0).getName());
        assertEquals(150, leaderboard.get(0).getTotalPoints());
        assertEquals(1, leaderboard.get(0).getRankNumber());
        assertEquals("🏆 Campus Legend", leaderboard.get(0).getRankBadge());

        assertEquals("Finder Two", leaderboard.get(1).getName());
        assertEquals(25, leaderboard.get(1).getTotalPoints());
        assertEquals(2, leaderboard.get(1).getRankNumber());
        assertEquals("🌟 Super Finder", leaderboard.get(1).getRankBadge());
    }
}
