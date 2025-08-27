package org.vt.service.impl;

import com.ogya.logging.avro.schema.OrderObject;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.vt.config.mybatis.MyBatisUtils;
import org.vt.config.mybatis.entity.CheckStatus;
import org.vt.config.mybatis.mapper.CheckStatusMapper;
import org.vt.model.MessageResponse;
import org.vt.service.FileService;
import org.vt.service.KafkaProducerService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class FileServiceImpl implements FileService {

    private Logger logger = LoggerFactory.getLogger("Voltage Backend Service");

    @Autowired
    private KafkaProducerService kafkaProducerService;

    @Override
    public ResponseEntity<MessageResponse> orderCsv(Authentication authentication){
        String username = authentication.getName();
        String uuid = UUID.randomUUID().toString();

        //send kafka message
        OrderObject orderObject = OrderObject.newBuilder()
                .setTimestamp(System.currentTimeMillis())
                .setUsername(username)
                .setStatusId(uuid)
                .build();

        kafkaProducerService.send(orderObject);

        MyBatisUtils myBatisUtils = new MyBatisUtils();
        SqlSessionFactory sqlSessionFactory = myBatisUtils.createFactory();
        try (SqlSession session = sqlSessionFactory.openSession(false)) {
            CheckStatusMapper checkStatusMapper = myBatisUtils.createMapper(CheckStatusMapper.class,session);

            CheckStatus checkStatus = new CheckStatus();
            checkStatus.setStatusId(uuid);
            checkStatus.setUsername(username);
            checkStatus.setStatus("waiting");
            checkStatus.setLastUpdated(LocalDateTime.now());
            checkStatusMapper.insertStatus(checkStatus);

            session.commit();
        }catch (Exception e) {
            logger.error("get data failed", e);
            throw new RuntimeException(e);
        }
        return ResponseEntity.ok(new MessageResponse("Succes Ordering CSV File"));
    }

    @Override
    public ResponseEntity<List<CheckStatus>> getCheckStatus(Long limit, Long offset){
        MyBatisUtils myBatisUtils = new MyBatisUtils();
        SqlSessionFactory sqlSessionFactory = myBatisUtils.createFactory();
        try(SqlSession session = sqlSessionFactory.openSession(false)){
            CheckStatusMapper checkStatusMapper = myBatisUtils.createMapper(CheckStatusMapper.class,session);
            List<CheckStatus> listCheckStatus = checkStatusMapper.getAllCheckStatus(limit,offset);

            return ResponseEntity.ok(listCheckStatus);
        }catch (Exception e) {
            logger.error("get data failed", e);
            throw new RuntimeException(e);
        }
    }
}
