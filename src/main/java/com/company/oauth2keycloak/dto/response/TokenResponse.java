package com.company.oauth2keycloak.dto.response;
import lombok.Data;

@Data
public class TokenResponse {
    private String access_token;
    private int expires_in;
    private String refresh_token;
    private int refresh_expires_in;
    private String token_type;
    private String session_state;
    private String scope;
}
