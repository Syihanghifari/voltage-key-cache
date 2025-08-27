package org.vt.config.mybatis;

import org.apache.ibatis.datasource.pooled.PooledDataSource;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;

import javax.sql.DataSource;

public class MyBatisUtils {

    SqlSession session = null;

    public MyBatisUtils() {
        org.apache.ibatis.logging.LogFactory.useSlf4jLogging();
    }

    public SqlSessionFactory createFactory(String driver, String url, String username, String password) {
        DataSource dataSource = new PooledDataSource(driver, url, username, password);
        Environment environment = new Environment("Development", new JdbcTransactionFactory(), dataSource);
        Configuration configuration = new Configuration(environment);
        SqlSessionFactoryBuilder builder = new SqlSessionFactoryBuilder();
        return builder.build(configuration);
    }

    public SqlSessionFactory createFactory() {
        try {
            DataSource dataSource = Datasource.getDatasource("KeyDatasource");
            Environment environment = new Environment("Development", new JdbcTransactionFactory(), dataSource);
            Configuration configuration = new Configuration(environment);
            SqlSessionFactoryBuilder builder = new SqlSessionFactoryBuilder();
            return builder.build(configuration);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException("Failed to get datasource", e);
        }
    }
    public <T> T createMapper(Class<T> t, SqlSession session) {
        session.getConfiguration().addMapper(t);
        return session.getMapper(t);
    }

    public <T> T createMapper(Class<T> t, SqlSessionFactory sqlSessionFactory) {
        if (session == null)
            session = sqlSessionFactory.openSession();
        session.getConfiguration().addMapper(t);
        return session.getMapper(t);
    }

    public void commitSession() {
        session.commit();
    }

    public void closeSession() {
        if (session != null)
            session.close();
        session = null;
    }
}
