package org.vt.service;

import org.springframework.http.ResponseEntity;
import org.vt.config.mybatis.entity.SdaConfig;
import org.vt.model.MessageResponse;

import java.util.List;

public interface SdaConfigService {
    ResponseEntity<MessageResponse> insertSdaConfig(SdaConfig sdaConfig);
    ResponseEntity<List<SdaConfig>> getAllSdaConfig();
}
