package org.vt.model;

import org.vt.config.mybatis.entity.Menus;

import java.util.List;

public class AuthResponse {
    private String token;
    private List<Menus> menus;

    public AuthResponse(String token,List<Menus> menus) {
        this.token = token;
        this.menus = menus;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public List<Menus> getMenus() {
        return menus;
    }

    public void setMenus(List<Menus> menus) {
        this.menus = menus;
    }
}
