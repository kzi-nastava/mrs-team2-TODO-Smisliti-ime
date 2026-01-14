package rs.getgo.backend.services;

import rs.getgo.backend.dtos.user.CreateUserDTO;
import rs.getgo.backend.dtos.user.CreatedUserDTO;
import rs.getgo.backend.dtos.login.CreateLoginDTO;
import rs.getgo.backend.dtos.login.CreatedLoginDTO;

public interface AuthService {

    CreatedUserDTO register(CreateUserDTO request);

    CreatedLoginDTO login(CreateLoginDTO request);

    void forgotPassword(String email);

    Long verifyResetToken(String token);
}