package rs.getgo.backend.services;

<<<<<<< Updated upstream
=======
import org.springframework.stereotype.Service;
import rs.getgo.backend.model.enums.UserRole;
import rs.getgo.backend.repositories.UserRepository;
import rs.getgo.backend.model.entities.User;
>>>>>>> Stashed changes
import rs.getgo.backend.dtos.user.CreateUserDTO;
import rs.getgo.backend.dtos.user.CreatedUserDTO;
import rs.getgo.backend.dtos.login.CreateLoginDTO;
import rs.getgo.backend.dtos.login.CreatedLoginDTO;

<<<<<<< Updated upstream
=======
import java.util.UUID;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.time.Instant;
import java.io.*;
import java.net.Socket;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
>>>>>>> Stashed changes
public interface AuthService {
    CreatedUserDTO register(CreateUserDTO request);
    CreatedLoginDTO login(CreateLoginDTO request);
    void forgotPassword(String email);
<<<<<<< Updated upstream
}
=======
}
>>>>>>> Stashed changes
