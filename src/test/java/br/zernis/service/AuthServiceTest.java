package br.zernis.service;

import br.zernis.dto.auth.LoginDTO;
import br.zernis.dto.auth.LoginResponseDTO;
import br.zernis.dto.user.CreateUserDTO;
import br.zernis.dto.user.UserResponseDTO;
import br.zernis.entity.User;
import br.zernis.entity.UserRole;
import br.zernis.exception.AuthenticationException;
import br.zernis.repository.UserRepository;
import br.zernis.util.GenerateToken;
import io.quarkus.elytron.security.common.BcryptUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    UserRepository userRepository;

    @Mock
    GenerateToken generateToken;

    @InjectMocks
    AuthService authService;

    @Captor
    ArgumentCaptor<User> userCaptor;

    @Test
    void login_withValidCredentials_returnsToken() {
        String email = "joao@email.com";
        String password = "123456";
        String hashed = BcryptUtil.bcryptHash(password);
        String fakeToken = "eyJ.eyJ.TOKEN";

        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail(email);
        user.setPasswordHash(hashed);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(generateToken.generateToken(user)).thenReturn(fakeToken);

        LoginResponseDTO result = authService.login(new LoginDTO(email, password));

        assertEquals(email, result.email());
        assertEquals(fakeToken, result.token());
        verify(userRepository).findByEmail(email);
        verify(generateToken).generateToken(user);
    }

    @Test
    void login_withUnknownEmail_throwsAuthenticationException() {
        String email = "unknown@email.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThrows(AuthenticationException.class,
                () -> authService.login(new LoginDTO(email, "any")));
    }

    @Test
    void login_withWrongPassword_throwsAuthenticationException() {
        String email = "joao@email.com";
        String correctPassword = "123456";
        String hashed = BcryptUtil.bcryptHash(correctPassword);

        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(hashed);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        assertThrows(AuthenticationException.class,
                () -> authService.login(new LoginDTO(email, "wrong-password")));
    }

    @Test
    void register_createsUserAndReturnsDTO() {
        CreateUserDTO dto = new CreateUserDTO("João", "Silva", "joao@email.com", "123456", UserRole.SELLER);

        UserResponseDTO result = authService.register(dto);

        assertNotNull(result);
        assertEquals("João", result.firstName());
        assertEquals("joao@email.com", result.email());
        assertEquals(UserRole.SELLER, result.role());

        verify(userRepository).persist(userCaptor.capture());
        User saved = userCaptor.getValue();
        assertEquals("João", saved.getFirstName());
        assertEquals("joao@email.com", saved.getEmail());
        assertNotEquals("123456", saved.getPasswordHash());
        assertTrue(BcryptUtil.matches("123456", saved.getPasswordHash()));
        assertEquals(UserRole.SELLER, saved.getRole());
    }
}
