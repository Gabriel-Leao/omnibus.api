package br.com.leao.gabriel.omnibus.application.service;

import br.com.leao.gabriel.omnibus.application.usecase.GetUserCase;
import br.com.leao.gabriel.omnibus.domain.model.User;
import br.com.leao.gabriel.omnibus.domain.port.out.UserRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetUserService implements GetUserCase {
  private final UserRepository userRepository;

  @Override
  public User execute(UUID userId) {
    Optional<User> user = userRepository.findById(userId);
    if (user.isEmpty()) {
      throw new RuntimeException("User not found");
    }
    return user.get();
  }
}
