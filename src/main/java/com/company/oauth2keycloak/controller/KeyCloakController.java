package com.company.oauth2keycloak.controller;

import com.company.oauth2keycloak.builder.ResponseBuilder;
import com.company.oauth2keycloak.models.User;
import com.company.oauth2keycloak.service.UserKeyCloakService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.ws.rs.core.Response;

@RestController
@RequestMapping("/api/auth/keycloak/")
@Slf4j
public class KeyCloakController {
    @Autowired
    private UserKeyCloakService service;

    @PostMapping(value = "/createUser",
    produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<String> createUser(@RequestBody User user){
        log.info("User: {}",user);
        Response response = service.saveUser(user);
        return new ResponseEntity<>(HttpStatus.valueOf(response.getStatus()));
    }

    @PostMapping(value = "/login",
    produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> login(@RequestParam String username, @RequestParam String password){
        ResponseBuilder response = new ResponseBuilder.Builder(200)
                .buildMessage("login successfully")
                .buildData(service.login(username,password))
                .build();
        return new ResponseEntity<>(response,HttpStatus.valueOf(response.getCode()));
    }
    @PostMapping(value = "/refresh",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> refresh(@RequestParam String refreshToken){
        ResponseBuilder response = new ResponseBuilder.Builder(200)
                .buildMessage("refresh token successfully")
                .buildData(service.refreshToken(refreshToken))
                .build();
        return new ResponseEntity<>(response,HttpStatus.valueOf(response.getCode()));
    }
    @GetMapping(value = "/getUserProfile",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getUserProfile(@RequestParam String accessToken){
        ResponseBuilder response = new ResponseBuilder.Builder(200)
                .buildMessage("get user information successfully")
                .buildData(service.getUserInfo(accessToken))
                .build();
        return new ResponseEntity<>(response,HttpStatus.valueOf(response.getCode()));
    }

    @PostMapping(value = "/logout",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> logout(@RequestParam String refreshToken){
        ResponseBuilder response = service.logout(refreshToken);
        return new ResponseEntity<>(response,HttpStatus.valueOf(response.getCode()));
    }

    @PostMapping(value = "/updatePassword",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> updatePassword(@RequestParam String username,
                                         @RequestParam String oldPassword,
                                         @RequestParam String newPassword){
        ResponseBuilder response = service.changePassword(username,oldPassword,newPassword);
        return new ResponseEntity<>(response,HttpStatus.valueOf(response.getCode()));
    }

}
