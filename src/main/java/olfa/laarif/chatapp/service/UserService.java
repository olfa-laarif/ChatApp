package olfa.laarif.chatapp.service;


import olfa.laarif.chatapp.controller.request.UserLoginDto;
import olfa.laarif.chatapp.controller.request.UserRegistrationDto;
import olfa.laarif.chatapp.entity.UserEntity;

import java.util.Optional;

public interface UserService  {
    UserEntity register(UserRegistrationDto registrationDto);

    UserEntity registerAdmin(UserRegistrationDto registrationDto);

    Optional<String> login(UserLoginDto loginDto);

    Optional<UserEntity> findByPhoneNumber(String username);
}