package com.resumeor.auth;

import org.springframework.security.crypto.password.PasswordEncoder;
import com.resumeor.common.UnauthorizedException;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public LoginResponse login(LoginRequest request, JwtService jwtService) {
        UserAccount user = userRepository.findByUsername(request.username())
                .filter(account -> passwordEncoder.matches(request.password(), account.password()))
                .orElseThrow(() -> new UnauthorizedException("账号或密码错误"));
        LoginResponse.UserProfile profile = new LoginResponse.UserProfile(user.id(), user.username(), user.nickname());
        return new LoginResponse(jwtService.createToken(user.id(), user.username()), profile);
    }

    public void createDemoAccount() {
        if (userRepository.findByUsername("demo").isEmpty()) {
            userRepository.create("demo", passwordEncoder.encode("demo123"), "林知夏");
        }
    }

    public UserProfile register(RegisterRequest request) {
        if (userRepository.findByUsername(request.username()).isPresent()) {
            throw new IllegalArgumentException("账号已存在");
        }
        userRepository.create(request.username(), passwordEncoder.encode(request.password()), request.nickname());
        return userRepository.findByUsername(request.username()).map(this::toProfile).orElseThrow();
    }

    public UserProfile profile(long userId) {
        return userRepository.findById(userId).map(this::toProfile).orElseThrow(() -> new IllegalArgumentException("用户不存在"));
    }

    public void changePassword(long userId, PasswordChangeRequest request) {
        UserAccount user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        if (!passwordEncoder.matches(request.oldPassword(), user.password())) {
            throw new IllegalArgumentException("原密码错误");
        }
        userRepository.updatePassword(userId, passwordEncoder.encode(request.newPassword()));
    }

    private UserProfile toProfile(UserAccount user) {
        return new UserProfile(user.id(), user.username(), user.nickname());
    }
}
