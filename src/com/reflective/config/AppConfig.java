package com.reflective.config;

public final class AppConfig {

    public static final String API_KEY = System.getenv("ANTHROPIC_API_KEY");
    public static final String API_URL = "https://api.anthropic.com/v1/messages";
    public static final String MODEL   = "claude-sonnet-4-6";
    public static final int    MAX_TOKENS = 4096;

    private AppConfig() {}

    public static boolean isApiKeyMissing() {
        return API_KEY == null || API_KEY.isBlank();
    }
}