package com.college.inventory.service;

import com.college.inventory.dao.UserDao;
import com.college.inventory.model.User;

public class AuthService {
    private final UserDao userDao;
    private User currentUser;

    public AuthService() {
        this.userDao = new UserDao();
    }

    public boolean login(String username, String password) {
        User user = userDao.authenticate(username, password);
        if (user != null) {
            this.currentUser = user;
            return true;
        }
        return false;
    }

    public void logout() {
        this.currentUser = null;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public boolean isAdmin() {
        return currentUser != null && currentUser.isAdmin();
    }

    public UserDao getUserDao() {
        return userDao;
    }
}
