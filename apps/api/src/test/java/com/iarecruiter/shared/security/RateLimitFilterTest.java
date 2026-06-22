package com.iarecruiter.shared.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class RateLimitFilterTest {

    private RateLimitFilter rateLimitFilter;

    @BeforeEach
    void setUp() {
        rateLimitFilter = new RateLimitFilter();
    }

    @Test
    void underLimit_requestsPass() throws Exception {
        for (int i = 0; i < 10; i++) {
            MockHttpServletRequest request = buildLoginRequest("127.0.0.1");
            MockHttpServletResponse response = new MockHttpServletResponse();
            MockFilterChain chain = new MockFilterChain();

            rateLimitFilter.doFilter(request, response, chain);

            assertThat(response.getStatus())
                    .as("Request %d should pass (not 429)", i + 1)
                    .isNotEqualTo(429);
        }
    }

    @Test
    void eleventhRequest_returns429() throws Exception {
        // Send 10 requests that should all pass
        for (int i = 0; i < 10; i++) {
            MockHttpServletRequest request = buildLoginRequest("10.0.0.1");
            MockHttpServletResponse response = new MockHttpServletResponse();
            rateLimitFilter.doFilter(request, response, new MockFilterChain());
        }

        // The 11th request should be rate-limited
        MockHttpServletRequest request = buildLoginRequest("10.0.0.1");
        MockHttpServletResponse response = new MockHttpServletResponse();
        rateLimitFilter.doFilter(request, response, new MockFilterChain());

        assertThat(response.getStatus()).isEqualTo(429);
    }

    // ---- helpers ----

    private MockHttpServletRequest buildLoginRequest(String remoteAddr) {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/auth/login");
        request.setRemoteAddr(remoteAddr);
        return request;
    }
}
