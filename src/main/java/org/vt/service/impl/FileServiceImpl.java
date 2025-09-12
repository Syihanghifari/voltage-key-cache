package org.vt.service.impl;

import com.ogya.logging.avro.schema.OrderObject;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.vt.LRUCacheComponent;
import org.vt.config.mybatis.MyBatisUtils;
import org.vt.config.mybatis.entity.CheckStatus;
import org.vt.config.mybatis.entity.User;
import org.vt.config.mybatis.mapper.CheckStatusMapper;
import org.vt.config.mybatis.mapper.FileReportMapper;
import org.vt.config.mybatis.mapper.UserMapper;
import org.vt.config.util.AuthenticationException;
import org.vt.config.util.FileServiceException;
import org.vt.model.ListDataResponse;
import org.vt.model.MessageResponse;
import org.vt.service.FileService;
import org.vt.service.KafkaProducerService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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
            throw new FileServiceException(e);
        }
        return ResponseEntity.ok(new MessageResponse("Succes Ordering CSV File With Status id " + uuid));
    }

    @Override
    public ResponseEntity<MessageResponse> orderCsvByUsername(List<String> usernames) {
        MyBatisUtils myBatisUtils = new MyBatisUtils();
        SqlSessionFactory sqlSessionFactory = myBatisUtils.createFactory();
        List<String> listUuid = new ArrayList<>();
        try (SqlSession session = sqlSessionFactory.openSession(false)) {
            CheckStatusMapper checkStatusMapper = myBatisUtils.createMapper(CheckStatusMapper.class, session);
            for (String username : usernames) {
                User user = LRUCacheComponent.getInstance().getUserByUsername(username);
                if (user == null) {
                    throw new FileServiceException("User not found");
                }

                String uuid = UUID.randomUUID().toString();
                listUuid.add(uuid);
                //send kafka message
                OrderObject orderObject = OrderObject.newBuilder()
                        .setTimestamp(System.currentTimeMillis())
                        .setUsername(username)
                        .setStatusId(uuid)
                        .build();

                kafkaProducerService.send(orderObject);




                CheckStatus checkStatus = new CheckStatus();
                checkStatus.setStatusId(uuid);
                checkStatus.setUsername(username);
                checkStatus.setStatus("waiting");
                checkStatus.setLastUpdated(LocalDateTime.now());
                checkStatusMapper.insertStatus(checkStatus);
            }
            session.commit();
        } catch (Exception e) {
            throw new FileServiceException(e);
        }

        return ResponseEntity.ok(new MessageResponse("Succes Ordering CSV File With Status id " + listUuid.toString()));
    }

    @Override
    public ResponseEntity<MessageResponse> orderCsvAllUser(){
        MyBatisUtils myBatisUtils = new MyBatisUtils();
        SqlSessionFactory sqlSessionFactory = myBatisUtils.createFactory();
        try(SqlSession session = sqlSessionFactory.openSession(false)){
            UserMapper userMapper = myBatisUtils.createMapper(UserMapper.class,session);
            CheckStatusMapper checkStatusMapper = myBatisUtils.createMapper(CheckStatusMapper.class,session);
            List<User> listUser = userMapper.getAllUser();
            for(User user : listUser){
                String uuid = UUID.randomUUID().toString();

                //send kafka message
                OrderObject orderObject = OrderObject.newBuilder()
                        .setTimestamp(System.currentTimeMillis())
                        .setUsername(user.getUsername())
                        .setStatusId(uuid)
                        .build();

                kafkaProducerService.send(orderObject);

                CheckStatus checkStatus = new CheckStatus();
                checkStatus.setStatusId(uuid);
                checkStatus.setUsername(user.getUsername());
                checkStatus.setStatus("waiting");
                checkStatus.setLastUpdated(LocalDateTime.now());
                checkStatusMapper.insertStatus(checkStatus);
            }
            session.commit();
        }catch (Exception e){
            throw new FileServiceException(e);
        }
        return ResponseEntity.ok(new MessageResponse("Succes Ordering CSV File For All Username"));
    }

    @Override
    public ResponseEntity<List<CheckStatus>> getCheckStatus(Authentication authentication,Long limit, Long offset){
        MyBatisUtils myBatisUtils = new MyBatisUtils();
        SqlSessionFactory sqlSessionFactory = myBatisUtils.createFactory();
        String username = authentication.getName();

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        List<String> roles = authorities.stream()
                .map(GrantedAuthority::getAuthority)  // extract the role string
                .collect(Collectors.toList());

        try(SqlSession session = sqlSessionFactory.openSession(false)){
            CheckStatusMapper checkStatusMapper = myBatisUtils.createMapper(CheckStatusMapper.class,session);
            List<CheckStatus> listCheckStatus = new ArrayList<>();
            if(roles.contains("ROLE_ADMIN")){
                listCheckStatus = checkStatusMapper.getAllCheckStatus(limit,offset);
            }else if(roles.contains("ROLE_USER")){
                listCheckStatus = checkStatusMapper.getAllCheckStatusByUsername(username,limit,offset);
            }

            return ResponseEntity.ok(listCheckStatus);
        }catch (Exception e) {
            throw new FileServiceException(e);
        }
    }

    @Override
    public ResponseEntity<List<CheckStatus>> getAllCheckStatus(Long limit, Long offset){
        MyBatisUtils myBatisUtils = new MyBatisUtils();
        SqlSessionFactory sqlSessionFactory = myBatisUtils.createFactory();
        try(SqlSession session = sqlSessionFactory.openSession(false)){
            CheckStatusMapper checkStatusMapper = myBatisUtils.createMapper(CheckStatusMapper.class,session);
            List<CheckStatus> listCheckStatus = checkStatusMapper.getAllCheckStatus(limit,offset);
            return ResponseEntity.ok(listCheckStatus);
        }catch (Exception e) {
            throw new FileServiceException(e);
        }
    }

    @Override
    public ResponseEntity<ListDataResponse> getAllReportFile(){
        MyBatisUtils myBatisUtils = new MyBatisUtils();
        SqlSessionFactory sqlSessionFactory = myBatisUtils.createFactory();
        try(SqlSession session = sqlSessionFactory.openSession(false)){
            FileReportMapper fileReportMapper = myBatisUtils.createMapper(FileReportMapper.class,session);
            List<String> lisReportFile = fileReportMapper.getAllReportFile();
            ListDataResponse responses = new ListDataResponse();
            responses.setData(lisReportFile);
            return ResponseEntity.ok(responses);
        }catch (Exception e) {
            throw new FileServiceException(e);
        }
    }
}
