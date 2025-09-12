package org.vt.service;

import org.springframework.http.ResponseEntity;
import org.vt.config.mybatis.entity.SdaConfig;
import org.vt.model.MessageResponse;
import org.vt.model.SdaConfigRequest;

import java.util.List;

public interface SdaConfigService {
    ResponseEntity<MessageResponse> insertSdaConfig(SdaConfigRequest sdaConfigRequest);
    ResponseEntity<List<SdaConfig>> getAllSdaConfig();
}
