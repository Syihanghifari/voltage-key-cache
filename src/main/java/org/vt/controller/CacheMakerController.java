package org.vt.controller;


import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.vt.service.CacheMakerService;

import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping(value = "/voltage-key-cache")
@CrossOrigin(origins = "*")
@SecurityRequirement(name = "bearerAuth")
public class CacheMakerController {

    private final CacheMakerService cacheMakerService;

    public CacheMakerController(CacheMakerService cacheMakerService) {
        this.cacheMakerService = cacheMakerService;
    }

    @GetMapping("/download-v1")
    public void getCacheZip1(@RequestParam String identity, HttpServletResponse response){
        cacheMakerService.getCacheZip(identity,response);
    }

    @GetMapping("/download")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public void getCacheZip(Authentication authentication, HttpServletResponse response){
        cacheMakerService.getCacheZip(authentication,response);
    }

}
