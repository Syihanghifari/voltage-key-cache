package org.vt.service.impl;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.vt.config.mybatis.MyBatisUtils;
import org.vt.config.mybatis.entity.Menus;
import org.vt.config.mybatis.entity.Permissions;
import org.vt.config.mybatis.entity.Role;
import org.vt.config.mybatis.entity.User;
import org.vt.config.mybatis.mapper.UserMapper;
import org.vt.config.util.JwtUtil;
import org.vt.model.AuthResponse;
import org.vt.model.LoginRequest;
import org.vt.model.MessageResponse;
import org.vt.model.RegisterRequest;
import org.vt.service.AuthenticationService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuthenticationServiceImpl implements AuthenticationService {

    private Logger logger = LoggerFactory.getLogger("Voltage Backend Service");

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public AuthResponse login(LoginRequest request){
        MyBatisUtils myBatisUtils = new MyBatisUtils();
        SqlSessionFactory sqlSessionFactory = myBatisUtils.createFactory();
        try (SqlSession session = sqlSessionFactory.openSession(false)) {
            UserMapper userMapper = myBatisUtils.createMapper(UserMapper.class, session);
            User user = userMapper.getUserByUsername(request.getUsername());
            if (user == null) {
                throw new RuntimeException("User not found");
            }

            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                throw new RuntimeException("Incorrect password");
            }

            // Fetch roles from DB
            List<Role> roles = userMapper.getUserRoles(user.getId());
            List<String> roleNames = roles.stream()
                    .map(Role::getName)
                    .collect(Collectors.toList());

            List<Permissions> listPermissions = userMapper.getUserPermissions(user.getId());
            List<Menus> listMenus = new ArrayList<>();
            for(Permissions permissions: listPermissions){
                listMenus.add(userMapper.getMenusByPermissionsId(permissions.getPermissionsId()));
            }

            String token = jwtUtil.generateToken(user.getUsername(), roleNames);
            return new AuthResponse(token,listMenus);
        } catch (Exception e) {
            logger.error("get data failed", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public ResponseEntity<MessageResponse> register (RegisterRequest request){
        MyBatisUtils myBatisUtils = new MyBatisUtils();
        SqlSessionFactory sqlSessionFactory = myBatisUtils.createFactory();
        try (SqlSession session = sqlSessionFactory.openSession(false)) {
            UserMapper userMapper = myBatisUtils.createMapper(UserMapper.class, session);
            // Check if user already exists
            if (userMapper.getUserByUsername(request.getUsername()) != null) {
                return ResponseEntity.badRequest().body(new MessageResponse("Username already exists"));
            }

            // Create new user
            User user = new User();
            user.setUsername(request.getUsername());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setConfigId(request.getConfigId());

            userMapper.insertUser(user); // user.id will be populated thanks to @Options

            // Assign default role: ROLE_USER (assume its ID = 1, or query it by name)
            userMapper.insertUserRole(user.getId(), 2L); //assign default role (ROLE_USER)
            session.commit();
            return ResponseEntity.ok(new MessageResponse("User registered successfully"));
        }catch (Exception e) {
            logger.error("get data failed", e);
            throw new RuntimeException(e);
        }
    }
}
