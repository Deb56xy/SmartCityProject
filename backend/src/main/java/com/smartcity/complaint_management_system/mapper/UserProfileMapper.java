package com.smartcity.complaint_management_system.mapper;

import com.smartcity.complaint_management_system.dto.ProfileResponse;
import com.smartcity.complaint_management_system.model.User;

public class UserProfileMapper {

    public static ProfileResponse mapToUserProfile(User user) {

        ProfileResponse response = new ProfileResponse();
        response.setName(user.getName());
        response.setState(user.getState());
        response.setDistrict(user.getDistrict());
        response.setPhone(user.getPhoneNumber());
        response.setEmail(user.getEmail());
        response.setLocation(user.getLocation());

        return response;
    }
}
