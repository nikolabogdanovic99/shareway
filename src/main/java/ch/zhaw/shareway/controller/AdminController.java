package ch.zhaw.shareway.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ch.zhaw.shareway.model.FlaggedContent;
import ch.zhaw.shareway.repository.FlaggedContentRepository;
import ch.zhaw.shareway.service.UserService;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private FlaggedContentRepository flaggedContentRepository;

    @Autowired
    private UserService userService;

    @GetMapping("/flagged")
    public ResponseEntity<List<FlaggedContent>> getFlaggedContent() {
        if (!userService.userHasRole("admin")) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        return ResponseEntity.ok(flaggedContentRepository.findAll());
    }

    @DeleteMapping("/flagged/{id}")
    public ResponseEntity<String> deleteFlaggedEntry(@PathVariable String id) {
        if (!userService.userHasRole("admin")) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        flaggedContentRepository.deleteById(id);
        return ResponseEntity.ok("DELETED");
    }
}