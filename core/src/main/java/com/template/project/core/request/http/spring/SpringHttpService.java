package com.template.project.core.request.http.spring;

import com.template.project.core.entity.user.User;

public interface SpringHttpService {

    void userRegister(User userObj);

    void userGetDetails(String sessionToken, String userId);

    void userUpdateDetails(String sessionToken, String userId, User userObj);

}
