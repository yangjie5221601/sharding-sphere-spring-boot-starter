package com.chuangxin.sharding.sphere.autoconfig;

import com.chuangxin.sharding.sphere.config.ShardingDataSourceConfig;
import com.google.common.collect.Maps;
import io.shardingsphere.shardingjdbc.api.yaml.YamlShardingDataSourceFactory;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.util.ResourceUtils;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.Map;

/**
 * @Description:
 * @Author: yangjie
 * @Date: 2018/12/13 上午11:13
 */
@Configuration
@EnableConfigurationProperties({ShardingDataSourceConfig.class})
@Log4j2
@EnableTransactionManagement
public class ShardingSqlSessionFactoryAutoConfig {
	@Autowired
	private ShardingDataSourceConfig shardingDataSourceConfig;

	@Bean(name = "shardingDataSource")
	@Primary
	public DataSource dataSource() {
		try {
			Map<String, DataSource> dataSourceMap = Maps.newHashMap();
			dataSourceMap.putAll(shardingDataSourceConfig.getDataSources());
			return YamlShardingDataSourceFactory.createDataSource(dataSourceMap, ResourceUtils.getFile(shardingDataSourceConfig.getShardingRuleConfigLocation()));
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return null;
	}

	@Bean(name = "shardingSqlSessionFactory")
	public SqlSessionFactory sqlSessionFactoryBean(@Qualifier(value = "shardingDataSource") DataSource shardingDataSource) {
		SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
		bean.setDataSource(shardingDataSource);
		if (StringUtils.isNotBlank(shardingDataSourceConfig.getTypeAliasesPackage())) {
			bean.setTypeAliasesPackage(shardingDataSourceConfig.getTypeAliasesPackage());
		}
		ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
		try {
			if (StringUtils.isNotBlank(shardingDataSourceConfig.getConfigLocation())) {
				bean.setConfigLocation(resolver.getResource(shardingDataSourceConfig.getConfigLocation()));
			}
			if (StringUtils.isNotBlank(shardingDataSourceConfig.getMapperLocation())) {
				bean.setMapperLocations(resolver.getResources(shardingDataSourceConfig.getMapperLocation()));
			}
		} catch (IOException e) {
			log.error(e.getMessage(), e);
			throw new RuntimeException(e);
		}
		try {
			return bean.getObject();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}

	@Bean(name = "shardingSqlSessionTemplate")
	@Primary
	public SqlSessionTemplate sqlSessionTemplate(@Qualifier(value = "shardingSqlSessionFactory") SqlSessionFactory sqlSessionFactory) {
		return new SqlSessionTemplate(sqlSessionFactory);
	}


	@Bean(name = "shardingTransactionManager")
	@Primary
	public PlatformTransactionManager annotationDrivenTransactionManager(@Qualifier(value = "shardingDataSource") DataSource shardingDataSource) {
		return new DataSourceTransactionManager(shardingDataSource);
	}
}