package org.vt.config.mybatis.mapper;

import org.apache.ibatis.annotations.*;
import org.vt.config.mybatis.entity.*;

import java.util.List;

public interface UserMapper {
    final String GET_USER_BY_USERNAME = "SELECT * FROM users WHERE username = #{username}";
    final String GET_ALL_USER = "SELECT * FROM users";
    final String GET_USER_ROLES = "SELECT r.id, r.name FROM roles r " +
            "JOIN user_roles ur ON r.id = ur.role_id " +
            "WHERE ur.user_id = #{userId}";
    final String INSERT_USER = "INSERT INTO users (username, password, config_id) " +
            "VALUES (#{username}, #{password}, #{configId})";

    final String INSERT_USER_ROLE = "INSERT INTO user_roles (user_id, role_id) VALUES (#{userId}, #{roleId})";

    final String GET_USER_PERMISSIONS = "SELECT DISTINCT p.* FROM permissions p " +
            "JOIN role_permissions rp ON p.permissions_id = rp.permissions_id " +
            "JOIN user_roles ur ON rp.role_id = ur.role_id " +
            "WHERE ur.user_id = #{userId}";

    final String GET_MENUS_BY_PERMISSIONS_ID = "SELECT * FROM menus m WHERE m.permissions_id = #{permissionsId}";
    final String GET_USER_ROLES_BY_USER_ID = "SELECT * FROM user_roles WHERE user_id = #{userId}";
    final String GET_ALL_ROLE_PERMISSIONS = "SELECT * FROM role_permissions";
    final String GET_ALL_PERMISSIONS = "SELECT * FROM permissions";
    final String GET_ALL_ROLES = "SELECT * FROM roles";
    final String GET_ALL_MENUS = "SELECT * FROM menus";

    @Select(GET_USER_BY_USERNAME)
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "username", column = "username"),
            @Result(property = "password", column = "password"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "configId", column = "config_id")
    })
    User getUserByUsername(String username);

    @Select(GET_ALL_USER)
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "username", column = "username"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "configId", column = "config_id")
    })
    List<User> getAllUser();

    @Select(GET_USER_ROLES)
    List<Role> getUserRoles(Long userId);

    @Insert(INSERT_USER)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertUser(User user);

    @Insert(INSERT_USER_ROLE)
    void insertUserRole(@Param("userId") Long userId, @Param("roleId") Long roleId);

    @Select(GET_USER_PERMISSIONS)
    @Results({
            @Result(property = "permissionsId", column = "permissions_id"),
            @Result(property = "name", column = "name")
    })
    List<Permissions> getUserPermissions(Long userId);

    @Select(GET_MENUS_BY_PERMISSIONS_ID)
    @Results({
            @Result(property = "menuId", column = "menu_id"),
            @Result(property = "name", column = "name"),
            @Result(property = "route", column = "route"),
            @Result(property = "permissionsId", column = "permissions_id")
    })
    Menus getMenusByPermissionsId(Integer permissionsId);

    @Select(GET_USER_ROLES_BY_USER_ID)
    Integer getUserRolesByUserId(@Param("userId") Long userId);

    @Select(GET_ALL_ROLES)
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "name", column = "name")
    })
    List<Role> getAllRoles();

    @Select(GET_ALL_ROLE_PERMISSIONS)
    @Results({
            @Result(property = "roleId", column = "role_id"),
            @Result(property = "permissionsId", column = "permissions_id")
    })
    List<RolePermissions> getAllRolePermissions();

    @Select(GET_ALL_PERMISSIONS)
    @Results({
            @Result(property = "permissionsId", column = "permissions_id"),
            @Result(property = "name", column = "name")
    })
    List<Permissions> getAllPermissions();

    @Select(GET_ALL_MENUS)
    @Results({
            @Result(property = "menuId", column = "menu_id"),
            @Result(property = "name", column = "name"),
            @Result(property = "route", column = "route"),
            @Result(property = "permissionsId", column = "permissions_id")
    })
    List<Menus> getAllMenus();

}
