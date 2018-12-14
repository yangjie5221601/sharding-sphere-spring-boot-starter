package com.chuangxin.sharding.sphere.config;

import com.alibaba.druid.pool.DruidDataSource;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * @Description:
 * @Author: yangjie
 * @Date: 2018/12/13 下午2:54
 */
@Data
@ConfigurationProperties(prefix = "sharding.sphere")
public class ShardingSphereConfig {
	private Map<String, DruidDataSource> dataSources = new HashMap<>();
	private String shardingRuleConfigLocation;
	private String mapperLocation;
	private String typeAliasesPackage;
	private String configLocation;
}