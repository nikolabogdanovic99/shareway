package ch.zhaw.shareway.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import ch.zhaw.shareway.model.User;
import ch.zhaw.shareway.model.VerificationStatus;
import ch.zhaw.shareway.repository.UserRepository;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public boolean userHasRole(String role) {
        Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<String> userRoles = jwt.getClaimAsStringList("user_roles");
        if (userRoles != null && userRoles.contains(role)) {
            return true;
        }
        return false;
    }

    public String getEmail() {
        Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return jwt.getClaimAsString("email");
    }

    public String getAuth0Id() {
        Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return jwt.getSubject();
    }

    public String getName() {
        Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String name = jwt.getClaimAsString("name");
        return name != null ? name : getEmail();
    }

    public boolean isAdmin() {
        return userHasRole("admin");
    }

    public boolean canCreateRides() {
        if (isAdmin()) {
            return true;
        }
        String email = getEmail();
        Optional<User> user = userRepository.findByEmail(email);
        return user.isPresent() && user.get().getVerificationStatus() == VerificationStatus.VERIFIED;
    }
}