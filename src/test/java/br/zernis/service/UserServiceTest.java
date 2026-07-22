package br.zernis.service;

import br.zernis.entity.User;
import br.zernis.entity.UserRole;
import br.zernis.exception.NotFoundException;
import br.zernis.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    UserRepository userRepository;

    @InjectMocks
    UserService userService;

    @Test
    void getAll_returnsAllUsers() {
        User u1 = new User();
        u1.setId(UUID.randomUUID());
        u1.setEmail("u1@email.com");

        User u2 = new User();
        u2.setId(UUID.randomUUID());
        u2.setEmail("u2@email.com");

        when(userRepository.listAll()).thenReturn(List.of(u1, u2));

        List<User> result = userService.getAll();

        assertEquals(2, result.size());
        verify(userRepository).listAll();
    }

    @Test
    void getById_withExistingUser_returnsUser() {
        UUID id = UUID.randomUUID();
        User user = new User();
        user.setId(id);
        user.setEmail("joao@email.com");

        when(userRepository.findByIdOptional(id)).thenReturn(Optional.of(user));

        User result = userService.getById(id);

        assertEquals(id, result.getId());
        assertEquals("joao@email.com", result.getEmail());
    }

    @Test
    void getById_withNonExistingUser_throwsNotFoundException() {
        UUID id = UUID.randomUUID();
        when(userRepository.findByIdOptional(id)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.getById(id));
    }
}
