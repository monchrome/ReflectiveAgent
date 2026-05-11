package com.reflective.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AppConfigTest {

    @Test
    void apiUrlIsAnthropicMessagesEndpoint() {
        assertEquals("https://api.anthropic.com/v1/messages", AppConfig.API_URL);
    }

    @Test
    void modelIsSonnet46() {
        assertEquals("claude-sonnet-4-6", AppConfig.MODEL);
    }

    @Test
    void maxTokensIsPositive() {
        assertTrue(AppConfig.MAX_TOKENS > 0);
    }

    @Test
    void isApiKeyMissingMatchesEnvState() {
        String env = System.getenv("ANTHROPIC_API_KEY");
        boolean expected = env == null || env.isBlank();
        assertEquals(expected, AppConfig.isApiKeyMissing());
    }
}
