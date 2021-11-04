package com.company.oauth2keycloak.dto.response;

import lombok.Data;

@Data
public class UserProfile {
    private String sub;
    private Boolean email_verified;
    private String user_name;
    private String preferred_username;
    private String name;
    private String given_name;
    private String family_name;
    private String email;

}
