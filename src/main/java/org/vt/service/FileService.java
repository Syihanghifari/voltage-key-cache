package org.vt.service;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.vt.config.mybatis.entity.CheckStatus;
import org.vt.model.MessageResponse;

import java.util.List;

public interface FileService {
    ResponseEntity<MessageResponse> orderCsv(Authentication authentication);
    ResponseEntity<List<CheckStatus>> getCheckStatus(Long limit, Long offset);
}
