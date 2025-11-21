package com.fisch_tradehub.tradehub_core.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fisch_tradehub.tradehub_core.common.Constants;
import com.fisch_tradehub.tradehub_core.entity.User;
import com.fisch_tradehub.tradehub_core.entity.UserInfo;
import com.fisch_tradehub.tradehub_core.exception.ResourceNotFoundException;
import com.fisch_tradehub.tradehub_core.repository.UserInfoRepository;
import com.fisch_tradehub.tradehub_core.repository.UserRepository;
import com.fisch_tradehub.tradehub_core.web.dto.UserInfoDTO;

import lombok.RequiredArgsConstructor;

/**
 * Service for managing user profile information.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserInfoService {

    private final UserInfoRepository userInfoRepository;
    private final UserRepository userRepository;

    /**
     * Get user profile information.
     * 
     * @param username the username
     * @return user info or null if not set
     * @throws ResourceNotFoundException if user not found
     */
    public UserInfoDTO getUserInfo(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException(Constants.USER_NOT_FOUND));

        return userInfoRepository.findById(user.getId())
                .map(this::toDto)
                .orElse(null);
    }

    /**
     * Update user profile information.
     * 
     * @param username the username
     * @param request the updated profile data
     * @return the updated user info
     * @throws ResourceNotFoundException if user not found
     */
    @Transactional
    public UserInfoDTO updateUserInfo(String username, UserInfoDTO request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException(Constants.USER_NOT_FOUND));

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
