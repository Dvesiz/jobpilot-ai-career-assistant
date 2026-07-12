package com.resumeor.auth;

import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CaptchaService {
    private static final String CHARACTERS = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ";
    private static final Duration TTL = Duration.ofMinutes(5);
    private final SecureRandom random = new SecureRandom();
    private final Map<String, CaptchaEntry> entries = new ConcurrentHashMap<>();

    public CaptchaChallenge createChallenge() {
        StringBuilder code = new StringBuilder(4);
        for (int index = 0; index < 4; index++) {
            code.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        return createChallenge(code.toString());
    }

    CaptchaChallenge createChallenge(String code) {
        entries.entrySet().removeIf(entry -> entry.getValue().expiresAt().isBefore(Instant.now()));
        String id = UUID.randomUUID().toString();
        entries.put(id, new CaptchaEntry(code.toUpperCase(), Instant.now().plus(TTL)));
        String image = Base64.getEncoder().encodeToString(svg(code).getBytes(StandardCharsets.UTF_8));
        return new CaptchaChallenge(id, "data:image/svg+xml;base64," + image);
    }

    public void verifyAndConsume(String id, String code) {
        CaptchaEntry entry = id == null ? null : entries.remove(id);
        if (entry == null || entry.expiresAt().isBefore(Instant.now()) || code == null
                || !entry.code().equalsIgnoreCase(code.trim())) {
            throw new IllegalArgumentException("验证码错误或已过期，请重新获取");
        }
    }

    private String svg(String code) {
        int first = random.nextInt(7) - 3;
        int second = random.nextInt(7) - 3;
        int third = random.nextInt(7) - 3;
        int fourth = random.nextInt(7) - 3;
        return "<svg xmlns='http://www.w3.org/2000/svg' width='132' height='48' viewBox='0 0 132 48'>"
                + "<rect width='132' height='48' rx='5' fill='#edf3ee'/>"
                + "<path d='M4 12L128 35M8 39L124 9M24 2L102 46' stroke='#9db7aa' stroke-width='1' opacity='.55'/>"
                + "<circle cx='17' cy='34' r='2' fill='#ca6a35' opacity='.55'/><circle cx='116' cy='16' r='2' fill='#174d43' opacity='.5'/>"
                + glyph(code.charAt(0), 18, first) + glyph(code.charAt(1), 48, second)
                + glyph(code.charAt(2), 78, third) + glyph(code.charAt(3), 108, fourth)
                + "</svg>";
    }

    private String glyph(char character, int x, int rotation) {
        return "<text x='" + x + "' y='33' text-anchor='middle' transform='rotate(" + rotation + " " + x
                + " 24)' fill='#183e35' font-family='Arial,sans-serif' font-size='24' font-weight='700'>" + character + "</text>";
    }

    private record CaptchaEntry(String code, Instant expiresAt) {
    }
}
