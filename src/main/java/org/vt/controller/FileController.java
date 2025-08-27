package org.vt.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.vt.config.mybatis.entity.CheckStatus;
import org.vt.model.MessageResponse;
import org.vt.service.FileService;

import java.util.List;

@RestController
@RequestMapping(value = "/file")
@CrossOrigin(origins = "*")
@SecurityRequirement(name = "bearerAuth")
public class FileController {

    @Autowired
    FileService fileService;

    @GetMapping("/get-csv")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> getFile(Authentication authentication){
        return fileService.orderCsv(authentication);
    }

    @GetMapping("/get-status")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<List<CheckStatus>> getStatus(@RequestParam Long limit, @RequestParam Long offset){
        return fileService.getCheckStatus(limit,offset);
    }

}
