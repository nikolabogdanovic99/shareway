package ch.zhaw.shareway.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ch.zhaw.shareway.model.User;
import ch.zhaw.shareway.model.UserCreateDTO;
import ch.zhaw.shareway.model.UserProfileUpdateDTO;
import ch.zhaw.shareway.model.UserRole;
import ch.zhaw.shareway.model.VerificationStatus;
import ch.zhaw.shareway.repository.UserRepository;
import ch.zhaw.shareway.service.UserService;

@RestController
@RequestMapping("/api")
public class UserController {

    @Autowired
    UserRepository userRepository;

    @Autowired
    UserService userService;

    // POST /api/users - Neuen User erstellen
    @PostMapping("/users")
    public ResponseEntity<User> createUser(@RequestBody UserCreateDTO dto) {
        User user = new User(
            dto.getAuth0Id(),
            dto.getEmail(),
            dto.getName(),
            dto.getRole()
        );
        
        User savedUser = userRepository.save(user);
        return new ResponseEntity<>(savedUser, HttpStatus.CREATED);
    }

    // GET /api/users - Alle Users
    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> allUsers = userRepository.findAll();
        return new ResponseEntity<>(allUsers, HttpStatus.OK);
    }

    // GET /api/users/{id} - User by ID
    @GetMapping("/users/{id}")
    public ResponseEntity<User> getUserById(@PathVariable String id) {
        Optional<User> optUser = userRepository.findById(id);
        if (optUser.isPresent()) {
            return new ResponseEntity<>(optUser.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // PUT /api/users/me/profile - Update Profil (Name, Nachname, Profilbild)
    @PutMapping("/users/me/profile")
    public ResponseEntity<User> updateMyProfile(@RequestBody UserProfileUpdateDTO dto) {
        String userEmail = userService.getEmail();
        Optional<User> optUser = userRepository.findByEmail(userEmail);
        
        User user;
        
        // Falls User nicht existiert, erstelle ihn
        if (optUser.isEmpty()) {
            String auth0Id = userService.getAuth0Id();
            String name = userService.getName();
            
            UserRole role;
            if (userService.userHasRole("admin")) {
                role = UserRole.ADMIN;
            } else if (userService.userHasRole("driver")) {
                role = UserRole.DRIVER;
            } else {
                role = UserRole.RIDER;
            }
            
            user = new User(auth0Id, userEmail, name, role);
        } else {
            user = optUser.get();
        }
        
        // Update Profil-Felder (auch leere Werte erlauben)
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setProfileImage(dto.getProfileImage());
        
        User savedUser = userRepository.save(user);
        return new ResponseEntity<>(savedUser, HttpStatus.OK);
    }

    // PUT /api/users/me/verification - Führerschein hochladen für Verifizierung
    @PutMapping("/users/me/verification")
    public ResponseEntity<User> requestVerification(@RequestBody UserProfileUpdateDTO dto) {
        String userEmail = userService.getEmail();
        Optional<User> optUser = userRepository.findByEmail(userEmail);
        
        if (optUser.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        
        User user = optUser.get();
        
        // Führerschein-Bilder speichern
        user.setLicenseImageFront(dto.getLicenseImageFront());
        user.setLicenseImageBack(dto.getLicenseImageBack());
        
        // Wenn beide Bilder vorhanden, setze Status auf PENDING (auch wenn vorher VERIFIED)
        if (user.getLicenseImageFront() != null && !user.getLicenseImageFront().isEmpty() &&
            user.getLicenseImageBack() != null && !user.getLicenseImageBack().isEmpty()) {
            user.setVerificationStatus(VerificationStatus.PENDING);
        } else {
            // Wenn Bilder fehlen/gelöscht, zurück auf UNVERIFIED
            user.setVerificationStatus(VerificationStatus.UNVERIFIED);
        }
        
        User savedUser = userRepository.save(user);
        return new ResponseEntity<>(savedUser, HttpStatus.OK);
    }

    // GET /api/users/me - Eigene User-Daten aus DB
    @GetMapping("/users/me")
    public ResponseEntity<User> getMyUser() {
        String userEmail = userService.getEmail();
        Optional<User> optUser = userRepository.findByEmail(userEmail);
        
        if (optUser.isPresent()) {
            return new ResponseEntity<>(optUser.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // GET /api/users/pending - Alle Users mit Status PENDING (Admin only)
    @GetMapping("/users/pending")
    public ResponseEntity<List<User>> getPendingUsers() {
        if (!userService.userHasRole("admin")) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        
        List<User> pendingUsers = userRepository.findByVerificationStatus(VerificationStatus.PENDING);
        return new ResponseEntity<>(pendingUsers, HttpStatus.OK);
    }
}