package ma.ensam.okanetransfer.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class RateLimitFilter extends OncePerRequestFilter {
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        Rule rule = resolveRule(request);
        if (rule == null) {
            filterChain.doFilter(request, response);
            return;
        }

        String key = clientIp(request) + ":" + request.getRequestURI();
        Bucket bucket = buckets.computeIfAbsent(key, ignored -> new Bucket(Instant.now(), 0));
        synchronized (bucket) {
            Instant now = Instant.now();
            if (Duration.between(bucket.windowStart, now).compareTo(rule.window) >= 0) {
                bucket.windowStart = now;
                bucket.count = 0;
            }
            bucket.count++;
            if (bucket.count > rule.limit) {
                response.setStatus(429);
                response.setContentType("application/json");
                response.getWriter().write("{\"code\":\"RATE_LIMIT_EXCEEDED\",\"message\":\"Too many requests\"}");
                return;
            }
        }
        filterChain.doFilter(request, response);
    }

    private Rule resolveRule(HttpServletRequest request) {
        if (!HttpMethod.POST.matches(request.getMethod())) {
            return null;
        }
        String path = request.getRequestURI();
        if (path.endsWith("/api/v1/auth/login")) {
            return new Rule(5, Duration.ofMinutes(10));
        }
        if (path.endsWith("/api/v1/auth/verify-otp") || path.endsWith("/api/v1/otp/verify")) {
            return new Rule(3, Duration.ofMinutes(5));
        }
        return null;
    }

    private String clientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private static final class Bucket {
        private Instant windowStart;
        private int count;

        private Bucket(Instant windowStart, int count) {
            this.windowStart = windowStart;
            this.count = count;
        }
    }

    private record Rule(int limit, Duration window) {
    }
}
