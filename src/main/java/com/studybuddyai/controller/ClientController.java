package com.studybuddyai.controller;

import com.studybuddyai.dto.UserDto;
import com.studybuddyai.service.ClientService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ClientController {
    private final ClientService clientService;

    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    @GetMapping("/me")
    public ResponseEntity<UserDto> getCurrentUser(@AuthenticationPrincipal OAuth2User principal) {
        UserDto userDto = clientService.getCurrentUser(principal);
        return ResponseEntity.ok(userDto);
    }
}
