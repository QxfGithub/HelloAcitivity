package com.qxf.config;

import com.alibaba.druid.pool.DruidDataSource;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
@ConditionalOnProperty("spring.datasource.fund.url")
public class DataSourceConfiguration {

    @Bean(name = "fundDataSource", destroyMethod = "close", initMethod = "init")
    @ConfigurationProperties("spring.datasource.fund")
    public DataSource fundDataSource() {
        return new DruidDataSource();
    }

    @Bean
    public ProcessEngineConfiguration config(){
        ProcessEngineConfiguration configuration = ProcessEngineConfiguration.createStandaloneProcessEngineConfiguration();
        //2. 设置数据库连接信息
        // 设置数据库地址
        configuration.setJdbcUrl("jdbc:mysql://localhost:3306/activiti_test?createDatabaseIfNotExist=true");
        // 数据库驱动
        configuration.setJdbcDriver("com.mysql.jdbc.Driver");
        // 用户名
        configuration.setJdbcUsername("root");
        // 密码
        configuration.setJdbcPassword("123456");
        // 设置数据库建表策略(默认不会建表)
        configuration.setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE);
        //3. 使用配置对象创建流程引擎实例（检查数据库连接等环境信息是否正确）
        ProcessEngine processEngine = configuration.buildProcessEngine();

        return  configuration;
    }


}
