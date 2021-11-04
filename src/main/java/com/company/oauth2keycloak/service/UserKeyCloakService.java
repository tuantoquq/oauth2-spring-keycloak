package com.company.oauth2keycloak.service;

import com.company.oauth2keycloak.builder.ResponseBuilder;
import com.company.oauth2keycloak.dto.response.TokenResponse;
import com.company.oauth2keycloak.dto.response.UserProfile;
import com.company.oauth2keycloak.models.User;
import com.company.oauth2keycloak.models.UserAccount;
import com.company.oauth2keycloak.repository.UserRepository;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.ws.rs.core.Response;
import java.util.*;


@Service
@Slf4j
public class UserKeyCloakService {
    @Autowired
    private UserRepository userRepository;

    private final RestTemplate restTemplate = new RestTemplate();
    private final Gson gson = new Gson();

    @Value("${keycloak.resource}")
    private String keycloakClient;

    @Value("${keycloak.auth-server-url}")
    private String keycloakUrl;

    @Value("${keycloak.realm}")
    private String keycloakRealm;

    @Value("${is.keycloak.admin.user}")
    private String keycloakAdminUser;

    @Value("${is.keycloak.admin.password}")
    private String keycloakAdminPassword;

    @Value("${keycloak.credentials.secret}")
    private String keycloakClientSecret;

    private Keycloak getKeycloakInstance() {
        return Keycloak.getInstance(
                keycloakUrl,
                "master",
                keycloakAdminUser,
                keycloakAdminPassword,
                "admin-cli");
    }

    public Response saveUser(User user){
        CredentialRepresentation credentials = new CredentialRepresentation();
        credentials.setType(CredentialRepresentation.PASSWORD);
        credentials.setValue(user.getPassword());
        credentials.setTemporary(false);
        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setUsername(user.getUsername());
        userRepresentation.setEnabled(true);
        userRepresentation.setEmail(user.getEmail());
        userRepresentation.setFirstName(user.getFirstName());
        userRepresentation.setLastName(user.getLastName());
        userRepresentation.setCredentials(Arrays.asList(credentials));
        userRepresentation.setEnabled(true);
        Map<String, List<String>> attributes = new HashMap<>();
        attributes.put("description", Arrays.asList("Create a user"));
        userRepresentation.setAttributes(attributes);
        Keycloak keycloak = getKeycloakInstance();
        Response result = keycloak.realm(keycloakRealm).users().create(userRepresentation);
        log.info("response: {}", result.getStatusInfo());

        String userId = CreatedResponseUtil.getCreatedId(result);
        UserResource userResource = keycloak.realm(keycloakRealm).users().get(userId);
        RoleRepresentation userClientRole = keycloak.realm(keycloakRealm).roles().get("user").toRepresentation();
        userResource.roles().realmLevel().add(Arrays.asList(userClientRole));

        if(result.getStatusInfo().toString().equals("Created")){
            UserAccount account = new UserAccount();
            account.setEmail(user.getEmail());
            account.setUserKeycloakId(userId);
            account.setUsername(user.getUsername());
            account.setRoles(userResource.roles().realmLevel().listAll());

            userRepository.save(account);
        }
        return result;
    }

    public TokenResponse login(String username, String password){
        String loginKeyCloakUrl = "http://localhost:8080/auth/realms/chatbot-security/protocol/openid-connect/token";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type","password");
        params.add("client_id",keycloakClient);
        params.add("client_secret", keycloakClientSecret);
        params.add("username",username);
        params.add("password",password);
        HttpEntity<MultiValueMap<String,String>> entity =new HttpEntity<>(params,headers);

        ResponseEntity<String> response = restTemplate.postForEntity(loginKeyCloakUrl,entity,String.class);
        log.info("login response: {}",response.getBody());
        return gson.fromJson(response.getBody(),TokenResponse.class);
    }
    public TokenResponse refreshToken(String refreshToken){
        String tokenKeyCloakUrl = "http://localhost:8080/auth/realms/chatbot-security/protocol/openid-connect/token";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type","refresh_token");
        params.add("client_id",keycloakClient);
        params.add("client_secret", keycloakClientSecret);
        params.add("refresh_token",refreshToken);
        HttpEntity<MultiValueMap<String,String>> entity =new HttpEntity<>(params,headers);

        ResponseEntity<String> response = restTemplate.postForEntity(tokenKeyCloakUrl,entity,String.class);
        log.info("refresh token response: {}",response.getBody());
        return gson.fromJson(response.getBody(),TokenResponse.class);
    }
    public UserProfile getUserInfo(String accessToken){
        String userProfileUrl = "http://localhost:8080/auth/realms/chatbot-security/protocol/openid-connect/userinfo";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer "+accessToken);

        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.postForEntity(userProfileUrl,entity,String.class);
        return gson.fromJson(response.getBody(),UserProfile.class);
    }
    public ResponseBuilder logout(String refreshToken){
        String logoutUrl = "http://localhost:8080/auth/realms/chatbot-security/protocol/openid-connect/logout";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("client_id",keycloakClient);
        params.add("client_secret",keycloakClientSecret);
        params.add("refresh_token",refreshToken);

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(params,headers);
        ResponseEntity<String> response = restTemplate.postForEntity(logoutUrl,entity,String.class);
        ResponseBuilder svResponse;
        if(response.getStatusCode().value() == 204){
            svResponse = new ResponseBuilder.Builder(200)
                    .buildMessage("logout successfully")
                    .buildData("")
                    .build();
        }else{
            svResponse = new ResponseBuilder.Builder(response.getStatusCodeValue())
                    .buildMessage("error when logout")
                    .buildData(response.getBody())
                    .build();
        }
        return svResponse;
    }

    public ResponseBuilder changePassword(String username, String oldPassword, String newPassword){
        ResponseBuilder svResponse;
        Keycloak keycloak = getKeycloakInstance();
        Optional<UserRepresentation> user = keycloak.realm(keycloakRealm).users().search(username).stream().filter(
            u -> u.getUsername().equals(username)
        ).findFirst();
        if(user.isPresent()){
            CredentialRepresentation credentials = new CredentialRepresentation();
            credentials.setType(CredentialRepresentation.PASSWORD);
            credentials.setValue(newPassword);
            credentials.setTemporary(false);
            UserRepresentation userRepresentation = user.get();
            UserResource userResource = keycloak.realm(keycloakRealm).users().get(userRepresentation.getId());
            userRepresentation.setCredentials(Arrays.asList(credentials));
            userResource.update(userRepresentation);
            svResponse = new ResponseBuilder.Builder(200)
                    .buildMessage("update password successfully")
                    .buildData("")
                    .build();
        }else{
            svResponse = new ResponseBuilder.Builder(400)
                    .buildMessage("username not found")
                    .buildData("")
                    .build();
        }
        return svResponse;
    }

}
