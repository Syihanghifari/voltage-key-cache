package org.vt.service.impl;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.vt.LRUCacheComponent;
import org.vt.config.mybatis.MyBatisUtils;
import org.vt.config.mybatis.entity.*;
import org.vt.config.mybatis.mapper.UserMapper;
import org.vt.config.util.AuthenticationException;
import org.vt.config.util.JwtUtil;
import org.vt.model.AuthResponse;
import org.vt.model.LoginRequest;
import org.vt.model.MessageResponse;
import org.vt.model.RegisterRequest;
import org.vt.service.AuthenticationService;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AuthenticationServiceImpl implements AuthenticationService {

    private final Logger logger = LoggerFactory.getLogger("Authentication Service");

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
            User user = LRUCacheComponent.getInstance().getUserByUsername(request.getUsername());
            if (user == null) {
                throw new AuthenticationException("User not found");
            }

            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                throw new AuthenticationException("Incorrect password");
            }
            Integer rolesId = userMapper.getUserRolesByUserId(user.getId());

            Role userRole = getRoleByUserId(rolesId);

            List<String> roleNames = Collections.singletonList(userRole.getName());

            List<Permissions> listPermissions = getPermissionsByRoleId(rolesId);

            List<Menus> listUserMenus = getMenusByPermissions(listPermissions);

            String token = jwtUtil.generateToken(user.getUsername(), roleNames);
            return new AuthResponse(token,listUserMenus);
        } catch (Exception e) {
            logger.error("Login failed for user: {}", request.getUsername(), e);
            throw new AuthenticationException("Login failed");
        }
    }

    @Override
    public ResponseEntity<MessageResponse> register(RegisterRequest request){
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
            logger.error("Register failed for user: {}", request.getUsername(), e);
            throw new AuthenticationException("Register failed");
        }
    }

    @Override
    public  ResponseEntity<List<User>> getAllUser (){
        MyBatisUtils myBatisUtils = new MyBatisUtils();
        SqlSessionFactory sqlSessionFactory = myBatisUtils.createFactory();
        List<User> listUser = new ArrayList<>();
        try (SqlSession session = sqlSessionFactory.openSession(false)) {
            UserMapper userMapper = myBatisUtils.createMapper(UserMapper.class, session);
            listUser = userMapper.getAllUser();
        }catch (Exception e) {
            logger.error("Get Data All User Failed", e);
            throw new AuthenticationException("Get All User Failed");
        }
        return ResponseEntity.ok(listUser);
    }

    private List<Permissions> getPermissionsByRoleId(Integer roleId) {
        List<RolePermissions> allMappings = LRUCacheComponent.getInstance().getAllRolePermissions();
        Set<Integer> permissionIds = allMappings.stream()
                .filter(rp -> rp.getRoleId().equals(roleId))
                .map(RolePermissions::getPermissionsId)
                .collect(Collectors.toSet());

        List<Permissions> allPermissions = LRUCacheComponent.getInstance().getAllPermissions();
        return allPermissions.stream()
                .filter(p -> permissionIds.contains(p.getPermissionsId()))
                .toList();
    }

    private List<Menus> getMenusByPermissions(List<Permissions> permissions) {
        Set<Integer> permissionIds = permissions.stream()
                .map(Permissions::getPermissionsId)
                .collect(Collectors.toSet());

        List<Menus> allMenus = LRUCacheComponent.getInstance().getAllMenus();
        return allMenus.stream()
                .filter(menu -> permissionIds.contains(menu.getPermissionsId()))
                .toList();
    }

    private Role getRoleByUserId(Integer roleId) {
        return LRUCacheComponent.getInstance()
                .getAllRole()
                .stream()
                .filter(r -> r.getId().equals(roleId))
                .findFirst()
                .orElseThrow(() -> new AuthenticationException("Role not found for roleId = " + roleId));
    }


}
