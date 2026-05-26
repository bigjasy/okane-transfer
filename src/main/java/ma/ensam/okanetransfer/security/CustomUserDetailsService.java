package ma.ensam.okanetransfer.security;

import ma.ensam.okanetransfer.domain.user.User;
import ma.ensam.okanetransfer.enums.UserStatus;
import ma.ensam.okanetransfer.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmailIgnoreCase(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPasswordHash())
                .authorities(user.getRole().name())
                .disabled(user.getStatus() == UserStatus.DISABLED || user.getStatus() == UserStatus.PENDING_VERIFICATION)
                .accountLocked(user.getStatus() == UserStatus.SUSPENDED)
                .build();
    }
}
