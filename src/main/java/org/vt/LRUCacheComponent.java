package org.vt;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.vt.config.mybatis.MyBatisUtils;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vt.config.mybatis.entity.*;
import org.vt.config.mybatis.mapper.UserMapper;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class LRUCacheComponent {
    private static final Logger logger = LoggerFactory.getLogger(LRUCacheComponent.class);

    private Cache<String, List<Role>> listRolesCache;
    private Cache<String, List<RolePermissions>> listRolePermissionsCache;
    private Cache<String, List<Permissions>> listPermissionsCache;
    private Cache<String, List<Menus>> listMenusCache;
    private Cache<String, User> userCache;
    private static AtomicReference<LRUCacheComponent> _instance =new AtomicReference<>();


    public static LRUCacheComponent getInstance() {
        LRUCacheComponent localRef = _instance.get();
        if (localRef == null) {
            localRef  = new LRUCacheComponent();
            if(_instance.compareAndSet(null,localRef)){
                localRef = _instance.get();
            }
        }
        return localRef;
    }

    public void reload(){
        listRolesCache.invalidateAll();
        listRolePermissionsCache.invalidateAll();
        listPermissionsCache.invalidateAll();
        listMenusCache.invalidateAll();
        userCache.invalidateAll();
    }

    private LRUCacheComponent() {
        logger.info("CACHE INITIATE");

        if( listRolesCache != null ){
            listRolesCache.invalidateAll();
            listRolesCache = null;
        }
        listRolesCache = Caffeine.newBuilder().expireAfterWrite(Duration.ofMinutes(10)).build();

        if( listRolePermissionsCache != null ){
            listRolePermissionsCache.invalidateAll();
            listRolePermissionsCache = null;
        }
        listRolePermissionsCache = Caffeine.newBuilder().expireAfterWrite(Duration.ofMinutes(10)).build();

        if( listPermissionsCache != null ){
            listPermissionsCache.invalidateAll();
            listPermissionsCache = null;
        }
        listPermissionsCache = Caffeine.newBuilder().expireAfterWrite(Duration.ofMinutes(10)).build();

        if( listMenusCache != null ){
            listMenusCache.invalidateAll();
            listMenusCache = null;
        }
        listMenusCache = Caffeine.newBuilder().expireAfterWrite(Duration.ofMinutes(10)).build();

        if( userCache != null ){
            userCache.invalidateAll();
            userCache = null;
        }
        userCache = Caffeine.newBuilder().expireAfterAccess(Duration.ofMinutes(60)).build();
    }

    public User getUserByUsername(String username){
        User user = userCache.getIfPresent(username);
        if(user == null){
            MyBatisUtils myBatisUtils = new MyBatisUtils();
            SqlSessionFactory sqlSessionFactory = myBatisUtils.createFactory();
            try(SqlSession session = sqlSessionFactory.openSession(false)){
                UserMapper userMapper = myBatisUtils.createMapper(UserMapper.class,session);
                user = userMapper.getUserByUsername(username);
                if (user != null){
                    userCache.put(username,user);
                }
            }catch (Exception e){
                logger.error("Failed to Get User Data" , e);
            }
            logger.info("Get Get User Data from DB");
        }else{
            logger.info("Get Get User Data from Cache");
        }
        return user;
    }

    public List<Role> getAllRole(){
        List<Role> listRole = listRolesCache.getIfPresent("list-role");
        if(listRole == null){
            MyBatisUtils myBatisUtils = new MyBatisUtils();
            SqlSessionFactory sqlSessionFactory = myBatisUtils.createFactory();
            try(SqlSession session = sqlSessionFactory.openSession(false)){
                UserMapper userMapper = myBatisUtils.createMapper(UserMapper.class,session);
                listRole = userMapper.getAllRoles();
                if(listRole != null){
                    listRolesCache.put("list-role",listRole);
                }
            }catch (Exception e){
                logger.error("Failed to Load All Role Data" , e);
            }
            logger.info("Get All Role Data from DB");
        }else{
            logger.info("Get All Role Data from Cache");
        }
        return listRole;
    }

    public List<RolePermissions> getAllRolePermissions(){
        List<RolePermissions> listRolePermissions = listRolePermissionsCache.getIfPresent("list-role-permissions");
        if( listRolePermissions == null){
            MyBatisUtils myBatisUtils = new MyBatisUtils();
            SqlSessionFactory sqlSessionFactory = myBatisUtils.createFactory();
            try(SqlSession session = sqlSessionFactory.openSession(false)){
                UserMapper userMapper = myBatisUtils.createMapper(UserMapper.class,session);
                listRolePermissions = userMapper.getAllRolePermissions();
                if( listRolePermissions != null){
                    listRolePermissionsCache.put("list-role-permissions",listRolePermissions);
                }
            }catch (Exception e){
                logger.error("Failed to Load All Role Permissions Data" , e);
            }
            logger.info("Get All Role Pemissions Data from DB");
        }else{
            logger.info("Get All Role Pemissions Data from Cache");
        }
        return listRolePermissions;
    }

    public List<Permissions> getAllPermissions(){
        List<Permissions> listPermissions = listPermissionsCache.getIfPresent("list-permissions");
        if( listPermissions == null ){
            MyBatisUtils myBatisUtils = new MyBatisUtils();
            SqlSessionFactory sqlSessionFactory = myBatisUtils.createFactory();
            try(SqlSession session = sqlSessionFactory.openSession(false)){
                UserMapper userMapper = myBatisUtils.createMapper(UserMapper.class,session);
                listPermissions = userMapper.getAllPermissions();
                if( listPermissions != null){
                    listPermissionsCache.put("list-permissions",listPermissions);
                }
            }catch (Exception e){
                logger.error("Failed to Load All Permissions Data" , e);
            }
            logger.info("Get All Permissions Data from DB");
        }else{
            logger.info("Get All Permissions Data from Cache");
        }
        return listPermissions;
    }

    public List<Menus> getAllMenus(){
        List<Menus> listMenus = listMenusCache.getIfPresent("list-menu");
        if( listMenus == null){
            MyBatisUtils myBatisUtils = new MyBatisUtils();
            SqlSessionFactory sqlSessionFactory = myBatisUtils.createFactory();
            try(SqlSession session = sqlSessionFactory.openSession(false)){
                UserMapper userMapper = myBatisUtils.createMapper(UserMapper.class,session);
                listMenus = userMapper.getAllMenus();
                if( listMenus != null){
                    listMenusCache.put("list-menu",listMenus);
                }
            }catch (Exception e){
                logger.error("Failed to load All Menu Data" , e);
            }
            logger.info("Get All Menu Data from DB");
        }else{
            logger.info("Get All Menu Data from Cache");
        }
        return listMenus;
    }

}
