package org.vt.service;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.vt.config.mybatis.entity.CheckStatus;
import org.vt.model.ListDataResponse;
import org.vt.model.MessageResponse;

import java.util.List;

public interface FileService {
    ResponseEntity<MessageResponse> orderCsv(Authentication authentication);
    ResponseEntity<MessageResponse> orderCsvByUsername(List<String> usernames);
    ResponseEntity<MessageResponse> orderCsvAllUser();
    ResponseEntity<List<CheckStatus>> getCheckStatus(Authentication authentication,Long limit, Long offset);
    ResponseEntity<List<CheckStatus>> getAllCheckStatus(Long limit, Long offset);
    ResponseEntity<ListDataResponse> getAllReportFile();
}
