package org.vt.config.mybatis.mapper;

import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface FileReportMapper {
    final String GET_ALL_REPORT_FILE = "SELECT table_name FROM information_schema.tables " +
            "WHERE table_schema = 'ogya_report' AND table_type = 'BASE TABLE'";

    @Select(GET_ALL_REPORT_FILE)
    List<String> getAllReportFile();
}
