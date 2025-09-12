package org.vt.service.impl;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.vt.config.mybatis.MyBatisUtils;
import org.vt.config.mybatis.entity.SdaConfig;
import org.vt.config.mybatis.mapper.SdaConfigMapper;
import org.vt.config.util.SdaConfigException;
import org.vt.model.MessageResponse;
import org.vt.model.SdaConfigRequest;
import org.vt.service.SdaConfigService;

import java.util.ArrayList;
import java.util.List;

@Service
public class SdaConfigServiceImpl implements SdaConfigService {

    private final Logger logger = LoggerFactory.getLogger("SDA Config Service");

    @Override
    public ResponseEntity<MessageResponse> insertSdaConfig(SdaConfigRequest sdaConfigRequest){
        MyBatisUtils myBatisUtils = new MyBatisUtils();
        SqlSessionFactory sqlSessionFactory = myBatisUtils.createFactory();
        try(SqlSession session = sqlSessionFactory.openSession(false)){
            SdaConfigMapper sdaConfigMapper = myBatisUtils.createMapper(SdaConfigMapper.class,session);
            sdaConfigMapper.insertSdaConfig(sdaConfigRequest);

            session.commit();
        }catch (Exception e) {
            throw new SdaConfigException(e);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(new MessageResponse("Succes Adding Config"));
    }

    @Override
    public ResponseEntity<List<SdaConfig>> getAllSdaConfig(){
        MyBatisUtils myBatisUtils = new MyBatisUtils();
        SqlSessionFactory sqlSessionFactory = myBatisUtils.createFactory();
        List<SdaConfig> listSdaConfig;
        try(SqlSession session = sqlSessionFactory.openSession(false)){
            SdaConfigMapper sdaConfigMapper = myBatisUtils.createMapper(SdaConfigMapper.class,session);
            listSdaConfig = sdaConfigMapper.getAllSdaConfig();
        }catch (Exception e) {
            throw new SdaConfigException(e);
        }
        return ResponseEntity.ok(listSdaConfig);
    }
}
