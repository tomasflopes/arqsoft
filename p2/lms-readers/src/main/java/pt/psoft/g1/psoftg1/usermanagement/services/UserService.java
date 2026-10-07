package pt.psoft.g1.psoftg1.usermanagement.services;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.psoft.g1.psoftg1.exceptions.NotFoundException;
import pt.psoft.g1.psoftg1.usermanagement.api.UserViewAMQP;
import pt.psoft.g1.psoftg1.usermanagement.model.User;
import pt.psoft.g1.psoftg1.usermanagement.repositories.UserRepository;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User createUser(CreateUserRequest request) {
        User user = User.newUser(request.getUsername(), request.getPassword(), request.getName());
        return userRepository.save(user);
    }

    /**
     * Cria/sincroniza um User a partir de dados AMQP
     * Usado quando o microserviço recebe eventos de criação de users
     */
    public User createFromAMQP(UserViewAMQP userViewAMQP) {
        // Verifica se já existe
        Optional<User> existing = userRepository.findByUsername(userViewAMQP.getUsername());
        if (existing.isPresent()) {
            return existing.get();
        }

        // Cria user mínimo - sem password pois é apenas réplica para consulta
        User user = User.newUser(userViewAMQP.getUsername(), "default", userViewAMQP.getFullName());
        
        return userRepository.save(user);
    }

    /**
     * Atualiza um User a partir de dados AMQP
     */
    public User updateFromAMQP(UserViewAMQP userViewAMQP) {
        Optional<User> existing = userRepository.findByUsername(userViewAMQP.getUsername());
        if (existing.isEmpty()) {
            // Se não existe, cria
            return createFromAMQP(userViewAMQP);
        }

        User user = existing.get();
        user.setName(userViewAMQP.getFullName());
        return userRepository.save(user);
    }

    /**
     * Remove um User a partir de dados AMQP
     */
    public void deleteFromAMQP(UserViewAMQP userViewAMQP) {
        Optional<User> existing = userRepository.findByUsername(userViewAMQP.getUsername());
        existing.ifPresent(userRepository::delete);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public List<User> findByName(String name) {
        return userRepository.findByName(name);
    }
    
    public List<User> findByNameLike(String name) {
        return userRepository.findByName(name);
    }

    public List<User> saveAll(Iterable<User> users) {
        return userRepository.saveAll(users);
    }

    public void delete(User user) {
        userRepository.delete(user);
    }

    /**
     * Obtém o User autenticado a partir do contexto de segurança
     */
    public User getAuthenticatedUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new NotFoundException("No authenticated user found");
        }

        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found: " + username));
    }

}