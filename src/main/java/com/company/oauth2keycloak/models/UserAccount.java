package com.company.oauth2keycloak.models;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "users")
@Data
public class UserAccount {
    @Id
    private String id;

    private String userKeycloakId;
    private String email;
    private String username;
    private List roles;
}
