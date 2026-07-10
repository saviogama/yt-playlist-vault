package com.savio.ytplaylistvault.user;

import com.savio.ytplaylistvault.error.DuplicateResourceException;
import com.savio.ytplaylistvault.user.dto.CreateUserRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
  private final UserRepository userRepository;

  public UserService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Transactional
  public User createUser(CreateUserRequest request) {
    userRepository
        .findByGoogleSubject(request.googleSubject())
        .ifPresent(
            user -> {
              throw new DuplicateResourceException("User already exists for this Google subject");
            });

    User user = new User(request.googleSubject(), request.email(), request.displayName());

    return userRepository.save(user);
  }

  @Transactional
  public User syncAuthenticatedUser(String googleSubject, String email, String displayName) {
    return userRepository
        .findByGoogleSubject(googleSubject)
        .map(
            existingUser -> {
              existingUser.updateProfile(email, displayName);
              return existingUser;
            })
        .orElseGet(() -> userRepository.save(new User(googleSubject, email, displayName)));
  }
}
