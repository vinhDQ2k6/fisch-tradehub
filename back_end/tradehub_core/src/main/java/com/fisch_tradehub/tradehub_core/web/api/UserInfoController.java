package com.fisch_tradehub.tradehub_core.web.api;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fisch_tradehub.tradehub_core.service.UserInfoService;
import com.fisch_tradehub.tradehub_core.web.dto.UserInfoDTO;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/user/info")
@RequiredArgsConstructor
public class UserInfoController {

    private final UserInfoService userInfoService;

    @GetMapping
    public ResponseEntity<UserInfoDTO> getMyInfo(@AuthenticationPrincipal UserDetails userDetails) {
        UserInfoDTO info = userInfoService.getUserInfo(userDetails.getUsername());
        return ResponseEntity.ok(info);
    }

    @PostMapping
    public ResponseEntity<UserInfoDTO> updateMyInfo(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UserInfoDTO request) {
        UserInfoDTO updated = userInfoService.updateUserInfo(userDetails.getUsername(), request);
        return ResponseEntity.ok(updated);
    }
}
