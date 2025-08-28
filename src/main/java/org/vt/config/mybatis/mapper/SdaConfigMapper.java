package org.vt.config.mybatis.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.vt.config.mybatis.entity.SdaConfig;

import java.util.List;

public interface SdaConfigMapper {
    final String GET_SDA_CONFIG_BY_CONFIG_ID = "SELECT * FROM sda_config WHERE config_id = #{configId}";

    final String GET_ALL_SDA_CONFIG_ID = "SELECT * FROM sda_config";

    final String INSERT_SDA_CONFIG_BY_CONFIG_ID = "INSERT INTO sda_config (region, code, shared_secret) " +
            "VALUES (#{region}, #{code}, #{sharedSecret})";

    @Select(GET_SDA_CONFIG_BY_CONFIG_ID)
    @Results({
            @Result(property = "region", column = "region"),
            @Result(property = "code", column = "code"),
            @Result(property = "sharedSecret", column = "shared_secret")
    })
    SdaConfig getSdaConfigByConfidId(Long configId);

    @Select(GET_ALL_SDA_CONFIG_ID)
    @Results({
            @Result(property = "configId", column = "config_id"),
            @Result(property = "region", column = "region"),
            @Result(property = "code", column = "code"),
            @Result(property = "sharedSecret", column = "shared_secret")
    })
    List<SdaConfig> getAllSdaConfig();

    @Insert(INSERT_SDA_CONFIG_BY_CONFIG_ID)
    void insertSdaConfig(SdaConfig sdaConfig);
}
