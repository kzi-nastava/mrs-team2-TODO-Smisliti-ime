package rs.getgo.backend.services;

import org.springframework.security.core.Authentication;
import rs.getgo.backend.dtos.login.CreateLoginDTO;
import rs.getgo.backend.dtos.login.CreatedLoginDTO;
import rs.getgo.backend.dtos.user.CreateUserDTO;
import rs.getgo.backend.dtos.user.CreatedUserDTO;
import rs.getgo.backend.model.entities.User;

public interface AuthService {

    CreatedUserDTO register(CreateUserDTO registerPassengerDTO);

    CreatedLoginDTO login(CreateLoginDTO request);

    void forgotPassword(String email);

    Long verifyResetToken(String token);
    User getUserFromAuth(Authentication auth);
}