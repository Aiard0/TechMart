package br.zernis.service;

import br.zernis.entity.User;
import br.zernis.exception.NotFoundException;
import br.zernis.repository.UserRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class UserService {

    @Inject
    UserRepository userRepository;

    public List<User> getAll() {
        return userRepository.listAll();
    }

    public User getById(UUID id) {
        return userRepository.findByIdOptional(id).orElseThrow(() -> new NotFoundException("Usuário não encontrado com id: " + id));
    }

}
