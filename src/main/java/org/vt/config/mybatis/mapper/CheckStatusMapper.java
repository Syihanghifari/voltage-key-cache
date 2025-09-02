package org.vt.config.mybatis.mapper;

import org.apache.ibatis.annotations.*;
import org.vt.config.mybatis.entity.CheckStatus;

import java.util.List;

public interface CheckStatusMapper {
    final String INSERT_CHECK_STATUS = "INSERT INTO check_status (status_id,username, status, last_updated) " +
            "VALUES (#{statusId},#{username}, #{status}, #{lastUpdated})";

    final String GET_ALL_CHECK_STATUS =
            "SELECT status_id, username, status, " +
                    "       CASE " +
                    "           WHEN status = 'success' THEN NULL " +
                    "           ELSE ROW_NUMBER() OVER (PARTITION BY status ORDER BY last_updated) " +
                    "       END AS \"order\", " + // ⬅️ order is a reserved word, wrap in double quotes
                    "       CASE " +
                    "           WHEN status = 'success' THEN NULL " +
                    "           ELSE COUNT(*) OVER (PARTITION BY status) " +
                    "       END AS total_order, " +
                    "       last_updated, " +
                    "       url_path " +
                    "FROM check_status " +
                    "ORDER BY status, last_updated " +
                    "LIMIT #{limit} OFFSET #{offset}";

    final String GET_CHECK_STATUS_BY_USERNAME =
            "WITH ordered_status AS (" +
            "    SELECT" +
            "        status_id," +
            "        username," +
            "        status," +
            "        CASE " +
            "            WHEN status = 'success' THEN NULL" +
            "            ELSE ROW_NUMBER() OVER (PARTITION BY status ORDER BY last_updated)" +
            "        END AS \"order\"," +
            "        CASE " +
            "            WHEN status = 'success' THEN NULL" +
            "            ELSE COUNT(*) OVER (PARTITION BY status)" +
            "        END AS total_order," +
            "        last_updated," +
            "        url_path " +
            "    FROM check_status" +
            ") " +
            "SELECT * " +
            "FROM ordered_status " +
            "WHERE username = #{username} " +
            "ORDER BY status, last_updated " +
            "LIMIT #{limit} OFFSET #{offset}";

    @Insert(INSERT_CHECK_STATUS)
    void insertStatus(CheckStatus checkStatus);

    @Select(GET_CHECK_STATUS_BY_USERNAME)
    @Results({
            @Result(property = "statusId", column = "status_id"),
            @Result(property = "username", column = "username"),
            @Result(property = "status", column = "status"),
            @Result(property = "order", column = "order"),
            @Result(property = "totalOrder", column = "total_order"),
            @Result(property = "lastUpdated", column = "last_updated"),
            @Result(property = "urlPath", column = "url_path")
    })
    List<CheckStatus> getAllCheckStatusByUsername (String username,Long limit, Long offset);

    @Select(GET_ALL_CHECK_STATUS)
    @Results({
            @Result(property = "statusId", column = "status_id"),
            @Result(property = "username", column = "username"),
            @Result(property = "status", column = "status"),
            @Result(property = "order", column = "order"),
            @Result(property = "totalOrder", column = "total_order"),
            @Result(property = "lastUpdated", column = "last_updated"),
            @Result(property = "urlPath", column = "url_path")
    })
    List<CheckStatus> getAllCheckStatus(Long limit, Long offset);
}
