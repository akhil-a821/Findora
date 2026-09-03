package com.campusfind.config;

import com.campusfind.entity.Claim;
import com.campusfind.entity.Report;
import com.campusfind.entity.User;
import com.campusfind.entity.enums.ClaimStatus;
import com.campusfind.entity.enums.NotificationType;
import com.campusfind.entity.enums.ReportStatus;
import com.campusfind.entity.enums.ReportType;
import com.campusfind.entity.enums.Role;
import com.campusfind.repository.ClaimRepository;
import com.campusfind.repository.ReportRepository;
import com.campusfind.repository.UserRepository;
import com.campusfind.service.MatchingService;
import com.campusfind.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final ReportRepository reportRepository;
    private final ClaimRepository claimRepository;
    private final MatchingService matchingService;
    private final NotificationService notificationService;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           ReportRepository reportRepository,
                           ClaimRepository claimRepository,
                           MatchingService matchingService,
                           NotificationService notificationService,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.reportRepository = reportRepository;
        this.claimRepository = claimRepository;
        this.matchingService = matchingService;
        this.notificationService = notificationService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        // Ensure Admin user always exists
        User admin = userRepository.findByEmail("admin@campus.edu")
                .orElseGet(() -> {
                    User newAdmin = new User("System Administrator", "admin@campus.edu", passwordEncoder.encode("admin123"), Role.ROLE_ADMIN);
                    return userRepository.save(newAdmin);
                });

        // Seed demo data only if no reports exist in database
        if (reportRepository.count() > 0) {
            logger.info("Database already contains reports. Skipping demo data seeding.");
            return;
        }

        logger.info("Seeding comprehensive campus demo datasets into Findora...");

        // 1. Create Demo Users
        String defaultUserPassword = passwordEncoder.encode("password123");

        User john = userRepository.findByEmail("john.doe@campus.edu")
                .orElseGet(() -> userRepository.save(new User("John Doe", "john.doe@campus.edu", defaultUserPassword, Role.ROLE_USER)));

        User jane = userRepository.findByEmail("jane.smith@campus.edu")
                .orElseGet(() -> userRepository.save(new User("Jane Smith", "jane.smith@campus.edu", defaultUserPassword, Role.ROLE_USER)));

        User alex = userRepository.findByEmail("alex.rivera@campus.edu")
                .orElseGet(() -> userRepository.save(new User("Alex Rivera", "alex.rivera@campus.edu", defaultUserPassword, Role.ROLE_USER)));

        User priya = userRepository.findByEmail("priya.patel@campus.edu")
                .orElseGet(() -> userRepository.save(new User("Priya Patel", "priya.patel@campus.edu", defaultUserPassword, Role.ROLE_USER)));

        LocalDate today = LocalDate.now();

        // 2. Create LOST Reports
        // Lost 1: Apple AirPods Pro (John) -> Will match Found 1 (Alex)
        Report lostAirPods = new Report();
        lostAirPods.setUser(john);
        lostAirPods.setType(ReportType.LOST);
        lostAirPods.setItemName("Apple AirPods Pro (2nd Gen)");
        lostAirPods.setCategory("Electronics");
        lostAirPods.setDescription("Lost my white AirPods Pro with case while studying at the library reading section on the 2nd floor.");
        lostAirPods.setBrand("Apple");
        lostAirPods.setColor("White");
        lostAirPods.setDate(today.minusDays(2));
        lostAirPods.setTime("15:30");
        lostAirPods.setLocation("Library");
        lostAirPods.setImageUrl("/images/placeholder-electronics.svg");
        lostAirPods.setPrivateVerificationDetails("Charging case has a yellow Pikachu silicone skin; right pod has a small scratch near the base.");
        lostAirPods.setStatus(ReportStatus.ACTIVE);
        lostAirPods = reportRepository.save(lostAirPods);

        // Lost 2: Fossil Wallet (Priya) -> Will match Found 2 (Jane)
        Report lostWallet = new Report();
        lostWallet.setUser(priya);
        lostWallet.setType(ReportType.LOST);
        lostWallet.setItemName("Brown Leather Fossil Wallet");
        lostWallet.setCategory("Wallet");
        lostWallet.setDescription("Brown bi-fold wallet left near the cafeteria snack counter during afternoon rush.");
        lostWallet.setBrand("Fossil");
        lostWallet.setColor("Brown");
        lostWallet.setDate(today.minusDays(3));
        lostWallet.setTime("13:15");
        lostWallet.setLocation("Canteen");
        lostWallet.setImageUrl("/images/placeholder-wallet.svg");
        lostWallet.setPrivateVerificationDetails("Contains library card ID #LIB-8842 and an Indian Metro pass in the hidden compartment.");
        lostWallet.setStatus(ReportStatus.ACTIVE);
        lostWallet = reportRepository.save(lostWallet);

        // Lost 3: Casio Calculator (Jane) -> Will match Found 3 (Alex)
        Report lostCalculator = new Report();
        lostCalculator.setUser(jane);
        lostCalculator.setType(ReportType.LOST);
        lostCalculator.setItemName("Casio fx-991EX ClassWiz Calculator");
        lostCalculator.setCategory("Stationery");
        lostCalculator.setDescription("Scientific calculator left behind in Classroom 302 after calculus lecture.");
        lostCalculator.setBrand("Casio");
        lostCalculator.setColor("Black");
        lostCalculator.setDate(today.minusDays(4));
        lostCalculator.setTime("11:00");
        lostCalculator.setLocation("Classroom");
        lostCalculator.setImageUrl("/images/placeholder-default.svg");
        lostCalculator.setPrivateVerificationDetails("Initials 'J.S.' etched faintly on the underside of the sliding protective cover.");
        lostCalculator.setStatus(ReportStatus.ACTIVE);
        lostCalculator = reportRepository.save(lostCalculator);

        // Lost 4: Water Bottle (Priya)
        Report lostBottle = new Report();
        lostBottle.setUser(priya);
        lostBottle.setType(ReportType.LOST);
        lostBottle.setItemName("Hydro Flask Wide Mouth Water Bottle");
        lostBottle.setCategory("Accessories");
        lostBottle.setDescription("Teal blue insulated steel water bottle left on the bleachers beside the basketball court.");
        lostBottle.setBrand("Hydro Flask");
        lostBottle.setColor("Teal Blue");
        lostBottle.setDate(today.minusDays(1));
        lostBottle.setTime("17:45");
        lostBottle.setLocation("Playground");
        lostBottle.setImageUrl("/images/placeholder-default.svg");
        lostBottle.setPrivateVerificationDetails("Has a NASA circular sticker on the bottom half.");
        lostBottle.setStatus(ReportStatus.ACTIVE);
        lostBottle = reportRepository.save(lostBottle);

        // Lost 5: Campus ID Card (John) -> Already Returned demo case
        Report lostIdCard = new Report();
        lostIdCard.setUser(john);
        lostIdCard.setType(ReportType.LOST);
        lostIdCard.setItemName("University Student ID Card");
        lostIdCard.setCategory("ID Card");
        lostIdCard.setDescription("Student identity card with blue department lanyard dropped near the entrance gate turnstiles.");
        lostIdCard.setBrand("University");
        lostIdCard.setColor("Blue");
        lostIdCard.setDate(today.minusDays(5));
        lostIdCard.setTime("09:10");
        lostIdCard.setLocation("Main Gate");
        lostIdCard.setImageUrl("/images/placeholder-id.svg");
        lostIdCard.setPrivateVerificationDetails("Student Roll Number CS2024-102 with blood group B+.");
        lostIdCard.setStatus(ReportStatus.RETURNED);
        lostIdCard = reportRepository.save(lostIdCard);

        // Lost 6: Backpack (Jane)
        Report lostBackpack = new Report();
        lostBackpack.setUser(jane);
        lostBackpack.setType(ReportType.LOST);
        lostBackpack.setItemName("The North Face Navy Blue Backpack");
        lostBackpack.setCategory("Bags");
        lostBackpack.setDescription("Navy backpack containing engineering notebooks and a pencil pouch, left in the main auditorium.");
        lostBackpack.setBrand("The North Face");
        lostBackpack.setColor("Navy Blue");
        lostBackpack.setDate(today.minusDays(6));
        lostBackpack.setTime("16:00");
        lostBackpack.setLocation("Auditorium");
        lostBackpack.setImageUrl("/images/placeholder-default.svg");
        lostBackpack.setPrivateVerificationDetails("Contains a green engineering notebook with 'Jane - EE Dept' on the front cover.");
        lostBackpack.setStatus(ReportStatus.ACTIVE);
        lostBackpack = reportRepository.save(lostBackpack);

        // 3. Create FOUND Reports and Trigger Matching Engine
        // Found 1: Apple AirPods (Alex) -> Matches Lost 1
        Report foundAirPods = new Report();
        foundAirPods.setUser(alex);
        foundAirPods.setType(ReportType.FOUND);
        foundAirPods.setItemName("Apple AirPods with White Charging Case");
        foundAirPods.setCategory("Electronics");
        foundAirPods.setDescription("Found white Apple wireless earbuds on table #12 in the 2nd floor library study area. Deposited at the circulation desk.");
        foundAirPods.setBrand("Apple");
        foundAirPods.setColor("White");
        foundAirPods.setDate(today.minusDays(2));
        foundAirPods.setTime("16:15");
        foundAirPods.setLocation("Library");
        foundAirPods.setImageUrl("/images/placeholder-electronics.svg");
        foundAirPods.setStatus(ReportStatus.ACTIVE);
        foundAirPods = reportRepository.save(foundAirPods);
        matchingService.findAndSaveMatchesForReport(foundAirPods);

        // Found 2: Bi-fold Wallet (Jane) -> Matches Lost 2
        Report foundWallet = new Report();
        foundWallet.setUser(jane);
        foundWallet.setType(ReportType.FOUND);
        foundWallet.setItemName("Leather Bi-fold Wallet");
        foundWallet.setCategory("Wallet");
        foundWallet.setDescription("Found a brown genuine leather wallet under a table in the campus canteen.");
        foundWallet.setBrand("Fossil");
        foundWallet.setColor("Brown");
        foundWallet.setDate(today.minusDays(3));
        foundWallet.setTime("14:00");
        foundWallet.setLocation("Canteen");
        foundWallet.setImageUrl("/images/placeholder-wallet.svg");
        foundWallet.setStatus(ReportStatus.ACTIVE);
        foundWallet = reportRepository.save(foundWallet);
        matchingService.findAndSaveMatchesForReport(foundWallet);

        // Found 3: Scientific Calculator (Alex) -> Matches Lost 3
        Report foundCalculator = new Report();
        foundCalculator.setUser(alex);
        foundCalculator.setType(ReportType.FOUND);
        foundCalculator.setItemName("Casio Scientific Calculator");
        foundCalculator.setCategory("Stationery");
        foundCalculator.setDescription("Black Casio ClassWiz calculator found on desk row 3 in Classroom 302.");
        foundCalculator.setBrand("Casio");
        foundCalculator.setColor("Black");
        foundCalculator.setDate(today.minusDays(4));
        foundCalculator.setTime("12:30");
        foundCalculator.setLocation("Classroom");
        foundCalculator.setImageUrl("/images/placeholder-default.svg");
        foundCalculator.setStatus(ReportStatus.ACTIVE);
        foundCalculator = reportRepository.save(foundCalculator);
        matchingService.findAndSaveMatchesForReport(foundCalculator);

        // Found 4: Honda Key with Ring (John)
        Report foundKeys = new Report();
        foundKeys.setUser(john);
        foundKeys.setType(ReportType.FOUND);
        foundKeys.setItemName("Honda Vehicle Key with Silver Ring");
        foundKeys.setCategory("Keys");
        foundKeys.setDescription("Single remote Honda key found near the two-wheeler parking lot shade area.");
        foundKeys.setBrand("Honda");
        foundKeys.setColor("Silver");
        foundKeys.setDate(today.minusDays(2));
        foundKeys.setTime("10:00");
        foundKeys.setLocation("Parking Area");
        foundKeys.setImageUrl("/images/placeholder-keys.svg");
        foundKeys.setStatus(ReportStatus.ACTIVE);
        foundKeys = reportRepository.save(foundKeys);
        matchingService.findAndSaveMatchesForReport(foundKeys);

        // Found 5: Student ID Card (Alex) -> Already Returned demo case
        Report foundIdCard = new Report();
        foundIdCard.setUser(alex);
        foundIdCard.setType(ReportType.FOUND);
        foundIdCard.setItemName("Campus Student ID Card with Blue Lanyard");
        foundIdCard.setCategory("ID Card");
        foundIdCard.setDescription("Handed over to security desk at the main entrance gate.");
        foundIdCard.setBrand("University");
        foundIdCard.setColor("Blue");
        foundIdCard.setDate(today.minusDays(5));
        foundIdCard.setTime("09:40");
        foundIdCard.setLocation("Main Gate");
        foundIdCard.setImageUrl("/images/placeholder-id.svg");
        foundIdCard.setStatus(ReportStatus.RETURNED);
        foundIdCard = reportRepository.save(foundIdCard);

        // Found 6: Sony Wireless Headphones (Jane)
        Report foundHeadphones = new Report();
        foundHeadphones.setUser(jane);
        foundHeadphones.setType(ReportType.FOUND);
        foundHeadphones.setItemName("Sony Wireless Over-Ear Headphones");
        foundHeadphones.setCategory("Electronics");
        foundHeadphones.setDescription("Black over-ear headphones left plugged into desktop terminal #18 in the Computer Lab.");
        foundHeadphones.setBrand("Sony");
        foundHeadphones.setColor("Black");
        foundHeadphones.setDate(today.minusDays(1));
        foundHeadphones.setTime("18:10");
        foundHeadphones.setLocation("Computer Lab");
        foundHeadphones.setImageUrl("/images/placeholder-electronics.svg");
        foundHeadphones.setStatus(ReportStatus.ACTIVE);
        foundHeadphones = reportRepository.save(foundHeadphones);
        matchingService.findAndSaveMatchesForReport(foundHeadphones);

        // 4. Create Demo Claims
        // Claim 1: John claims the AirPods found by Alex (Pending review)
        Claim airPodsClaim = new Claim(
                foundAirPods,
                john,
                "I was studying at Table 12 on the second floor of the library from 2:00 PM to 4:00 PM and inadvertently left my AirPods case on the table.",
                "The case has a yellow Pikachu rubber protective cover with a carabiner ring attached, and the right earbud has a slight scratch near the charging contact stem."
        );
        airPodsClaim.setStatus(ClaimStatus.PENDING);
        claimRepository.save(airPodsClaim);

        notificationService.createNotification(
                alex,
                "New Claim Submitted 🎉",
                "John Doe has submitted an ownership claim for your found item 'Apple AirPods with White Charging Case'.",
                NotificationType.CLAIM_SUBMITTED,
                "/my-claims"
        );

        // Claim 2: Approved claim for the returned ID card
        Claim idCardClaim = new Claim(
                foundIdCard,
                john,
                "I realized I dropped my ID card when walking past the turnstiles this morning.",
                "The card belongs to John Doe, Roll CS2024-102, Department of Computer Science."
        );
        idCardClaim.setStatus(ClaimStatus.APPROVED);
        claimRepository.save(idCardClaim);

        // 5. System Welcome Notification for All Users
        List<User> allUsers = Arrays.asList(admin, john, jane, alex, priya);
        for (User u : allUsers) {
            notificationService.createNotification(
                    u,
                    "Welcome to Findora! 🚀",
                    "Welcome to the smart campus lost and found platform. Report items, track potential matches, and easily reclaim belongings.",
                    NotificationType.SYSTEM,
                    "/dashboard"
            );
        }

        logger.info("Demo datasets successfully loaded! Seeded {} users and {} reports with matches and claims.",
                allUsers.size(), reportRepository.count());
    }
}
