package ch.zhaw.shareway.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ch.zhaw.shareway.model.User;
import ch.zhaw.shareway.model.UserCreateDTO;
import ch.zhaw.shareway.repository.UserRepository;

@RestController
@RequestMapping("/api")
public class UserController {

    @Autowired
    UserRepository userRepository;

    // POST /api/users - Neuen User erstellen
    @PostMapping("/users")
    public ResponseEntity<User> createUser(@RequestBody UserCreateDTO dto) {
        User user = new User(
            dto.getAuth0Id(),
            dto.getEmail(),
            dto.getName(),
            dto.getRole()
        );
        // pictureUrl setzen falls vorhanden (optional)
        // rating und reviewCount haben default-Werte
        // createdAt wird automatisch gesetzt
        
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
}