package com.foodflow.userservice.Security;

import org.springframework.stereotype.Component;

@Component("userAuthorization")
public class UserAuthorization {

    public boolean isOwner(Long userId) {

        Long currentUserId = SecurityUtils.getCurrentUserId();

        return currentUserId.equals(userId);
    }
}