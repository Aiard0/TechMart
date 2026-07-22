package br.zernis.service;

import br.zernis.dto.auth.LoginDTO;
import br.zernis.dto.auth.LoginResponseDTO;
import br.zernis.dto.user.CreateUserDTO;
import br.zernis.dto.user.UserResponseDTO;
import br.zernis.entity.User;
import br.zernis.exception.AuthenticationException;
import br.zernis.repository.UserRepository;
import br.zernis.util.GenerateToken;
import io.quarkus.elytron.security.common.BcryptUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class AuthService {

    @Inject
    GenerateToken generateToken;

    @Inject
    UserRepository userRepository;

    public LoginResponseDTO login(LoginDTO dto) {
        User user = userRepository.findByEmail(dto.email())
                .orElseThrow(() -> new AuthenticationException("Credenciais inválidas."));

        if (!BcryptUtil.matches(dto.password(), user.getPasswordHash())) {
            throw new AuthenticationException("Credenciais inválidas.");
        }

        String token = generateToken.generateToken(user);
        return new LoginResponseDTO(user.getEmail(), token);
    }

    @Transactional
    public UserResponseDTO register(CreateUserDTO dto) {
        User newUser = new User();

        newUser.setFirstName(dto.firstName());
        newUser.setLastName(dto.lastName());
        newUser.setEmail(dto.email());
        newUser.setPasswordHash(BcryptUtil.bcryptHash(dto.password()));
        newUser.setRole(dto.role());

        userRepository.persist(newUser);

        return UserResponseDTO.fromEntity(newUser);
    }

}
