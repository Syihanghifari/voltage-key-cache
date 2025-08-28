package org.vt.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.vt.config.mybatis.entity.SdaConfig;
import org.vt.model.MessageResponse;
import org.vt.service.SdaConfigService;

import java.util.List;

@RestController
@RequestMapping(value = "/config")
@CrossOrigin(origins = "*")
@SecurityRequirement(name = "bearerAuth")
public class SdaConfigController {
    @Autowired
    SdaConfigService sdaConfigService;

    @PostMapping("/set")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> setSdaConfig(@RequestBody SdaConfig sdaConfig){
        return sdaConfigService.insertSdaConfig(sdaConfig);
    }

    @GetMapping("/get-all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SdaConfig>> getAllSdaConfig(){
        return sdaConfigService.getAllSdaConfig();
    }
}
