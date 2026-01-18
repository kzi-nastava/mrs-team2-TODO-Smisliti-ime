package rs.getgo.backend.services;

import rs.getgo.backend.dtos.login.CreateLoginDTO;
import rs.getgo.backend.dtos.login.CreatedLoginDTO;
import rs.getgo.backend.dtos.user.CreateUserDTO;
import rs.getgo.backend.dtos.user.CreatedUserDTO;

public interface AuthService {

    CreatedUserDTO register(CreateUserDTO registerPassengerDTO);

    CreatedLoginDTO login(CreateLoginDTO request);

    void forgotPassword(String email);

    Long verifyResetToken(String token);
}