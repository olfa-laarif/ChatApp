package olfa.laarif.chatapp.controller;

import olfa.laarif.chatapp.controller.request.UserLoginDto;
import olfa.laarif.chatapp.controller.request.UserRegistrationDto;
import olfa.laarif.chatapp.entity.UserEntity;
import olfa.laarif.chatapp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;


@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;


    @PostMapping("/register")
    public ResponseEntity<UserEntity> registerUser(@RequestBody UserRegistrationDto registrationDto) {
        Optional<UserEntity> existingUser = userService.findByPhoneNumber(registrationDto.getPhoneNumber());
        if (existingUser.isPresent()) {
            return ResponseEntity.badRequest().body(null);
        } else {
            UserEntity newUser = userService.register(registrationDto);
            return ResponseEntity.ok(newUser);
        }
    }

    @PostMapping("/register/admin")
    public ResponseEntity<UserEntity> registerAdmin(@RequestBody UserRegistrationDto registrationDto) {
        Optional<UserEntity> existingUser = userService.findByPhoneNumber(registrationDto.getPhoneNumber());
        if (existingUser.isPresent()) {
            return ResponseEntity.badRequest().body(null);
        }
        try {
            UserEntity admin = userService.registerAdmin(registrationDto);
            return ResponseEntity.status(201).body(admin);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(409).body(null);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<String> loginUser(@RequestBody UserLoginDto loginDto) {
        Optional<String> token = userService.login(loginDto);
        return token.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.status(401).body("Invalid credentials"));
    }
}



