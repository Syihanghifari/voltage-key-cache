package org.vt.service;

import org.springframework.security.core.Authentication;

import javax.servlet.http.HttpServletResponse;

public interface CacheMakerService {

    void getCacheZip(Authentication authentication, HttpServletResponse response);
    void getFpeProcessorZip(HttpServletResponse response);
    void getCacheZip(String identity, HttpServletResponse response);
}
