package com.fisch_tradehub.tradehub_core.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fisch_tradehub.tradehub_core.entity.User;
import com.fisch_tradehub.tradehub_core.entity.UserInfo;
import com.fisch_tradehub.tradehub_core.repository.UserInfoRepository;
import com.fisch_tradehub.tradehub_core.repository.UserRepository;
import com.fisch_tradehub.tradehub_core.web.dto.UserInfoDTO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserInfoService {

    private final UserInfoRepository userInfoRepository;
    private final UserRepository userRepository;

    public UserInfoDTO getUserInfo(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return userInfoRepository.findById(user.getId())
                .map(this::toDto)
                .orElse(null); // Return null if info not set yet
    }

    @Transactional
    public UserInfoDTO updateUserInfo(String username, UserInfoDTO request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserInfo userInfo = userInfoRepository.findById(user.getId())
                .orElse(UserInfo.builder().user(user).build());

        userInfo.setFullname(request.fullname());
        userInfo.setAge(request.age());
        userInfo.setGender(request.gender());

        UserInfo saved = userInfoRepository.save(userInfo);
        return toDto(saved);
    }

    private UserInfoDTO toDto(UserInfo info) {
        return new UserInfoDTO(
                info.getFullname(),
                info.getAge(),
                info.getGender()
        );
    }
}
