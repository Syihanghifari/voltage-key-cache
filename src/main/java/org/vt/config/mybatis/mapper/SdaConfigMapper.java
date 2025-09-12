package org.vt.config.mybatis.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.vt.config.mybatis.entity.SdaConfig;
import org.vt.model.SdaConfigRequest;

import java.util.List;

public interface SdaConfigMapper {
    final String GET_SDA_CONFIG_BY_CONFIG_ID = "SELECT * FROM sda_config WHERE config_id = #{configId}";

    final String GET_ALL_SDA_CONFIG_ID = "SELECT * FROM sda_config";

    final String INSERT_SDA_CONFIG_BY_CONFIG_ID = "INSERT INTO sda_config (region, code) " +
            "VALUES (#{region}, #{code})";

    @Select(GET_SDA_CONFIG_BY_CONFIG_ID)
    @Results({
            @Result(property = "region", column = "region"),
            @Result(property = "code", column = "code")
    })
    SdaConfig getSdaConfigByConfidId(Long configId);

    @Select(GET_ALL_SDA_CONFIG_ID)
    @Results({
            @Result(property = "configId", column = "config_id"),
            @Result(property = "region", column = "region"),
            @Result(property = "code", column = "code")
    })
    List<SdaConfig> getAllSdaConfig();

    @Insert(INSERT_SDA_CONFIG_BY_CONFIG_ID)
    void insertSdaConfig(SdaConfigRequest sdaConfigRequest);
}
