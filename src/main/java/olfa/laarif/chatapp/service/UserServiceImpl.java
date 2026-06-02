package olfa.laarif.chatapp.service;


import olfa.laarif.chatapp.config.JwtUtil;
import olfa.laarif.chatapp.controller.request.UserLoginDto;
import olfa.laarif.chatapp.controller.request.UserRegistrationDto;
import olfa.laarif.chatapp.entity.UserEntity;
import olfa.laarif.chatapp.repository.UserRepository;
import org.springframework.stereotype.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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
        user.setClerkId(passwordEncoder.encode(registrationDto.getPassword()));
        user.setEmail(registrationDto.getEmail());

        return userRepository.save(user);
    }

    public Optional<String> login(UserLoginDto loginDto) {
        Optional<UserEntity> userOptional = userRepository.findByPhoneNumber(loginDto.getPhoneNumber());
        if (userOptional.isPresent()) {
            UserEntity user = userOptional.get();
           if (passwordEncoder.matches(loginDto.getPassword(), user.getClerkId())) {
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

