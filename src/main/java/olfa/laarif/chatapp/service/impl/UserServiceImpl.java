package olfa.laarif.chatapp.service.impl;


import olfa.laarif.chatapp.config.JwtUtil;
import olfa.laarif.chatapp.controller.request.UserLoginDto;
import olfa.laarif.chatapp.controller.request.UserRegistrationDto;
import olfa.laarif.chatapp.entity.UserEntity;
import olfa.laarif.chatapp.enums.Role;
import olfa.laarif.chatapp.repository.UserRepository;
import olfa.laarif.chatapp.service.UserService;
import org.springframework.stereotype.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {


    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private PasswordEncoder passwordEncoder;

    public UserEntity register(UserRegistrationDto registrationDto) {
        UserEntity user = new UserEntity();
        user.setUsername(registrationDto.getUsername());
        user.setPhoneNumber(registrationDto.getPhoneNumber());
        user.setPassword(passwordEncoder.encode(registrationDto.getPassword()));
        user.setEmail(registrationDto.getEmail());
        user.setRole(Role.USER);

        return userRepository.save(user);
    }

    // Bootstrap : crée le premier compte ADMIN. Refuse si un admin existe déjà,
    // pour empêcher quiconque de s'auto-promouvoir une fois le système amorcé.
    public UserEntity registerAdmin(UserRegistrationDto registrationDto) {
        if (userRepository.existsByRole(Role.ADMIN)) {
            throw new IllegalStateException("An admin already exists. Promotion must go through an admin-only endpoint.");
        }

        UserEntity admin = new UserEntity();
        admin.setUsername(registrationDto.getUsername());
        admin.setPhoneNumber(registrationDto.getPhoneNumber());
        admin.setPassword(passwordEncoder.encode(registrationDto.getPassword()));
        admin.setEmail(registrationDto.getEmail());
        admin.setRole(Role.ADMIN);

        return userRepository.save(admin);
    }

    public Optional<String> login(UserLoginDto loginDto) {
        Optional<UserEntity> userOptional = userRepository.findByPhoneNumber(loginDto.getPhoneNumber());
        if (userOptional.isPresent()) {
            UserEntity user = userOptional.get();
           if (passwordEncoder.matches(loginDto.getPassword(), user.getPassword())) {
               String token = jwtUtil.generateToken(user.getPhoneNumber());
               return Optional.of(token);
            }

        }
        return Optional.empty();
    }

    public Optional<UserEntity> findByPhoneNumber(String username) {
        return userRepository.findByPhoneNumber(username);
    }



}

