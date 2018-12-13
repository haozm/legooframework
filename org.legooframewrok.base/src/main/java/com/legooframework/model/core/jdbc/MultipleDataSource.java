package com.legooframework.model.core.jdbc;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;

public class MultipleDataSource implements InitializingBean, ApplicationContextAware {

    private final static Splitter.MapSplitter MAP_SPLITTER = Splitter.on(',').withKeyValueSeparator('=');
    private final Map<String, SingleDataSource> dataSourceMap;
    private List<String> config;
    private String defName;

    public MultipleDataSource() {
        this.dataSourceMap = Maps.newConcurrentMap();
    }

    private ApplicationContext appCtx;

    public void setConfig(List<String> config) {
        this.config = config;
    }

    JdbcTemplate loadJdbcTemplate(String name) {
        final String _name = Strings.isNullOrEmpty(name) ? this.defName : name;
        Preconditions.checkState(dataSourceMap.containsKey(_name), "不存在 %s 对应的数据源配置...");
        return this.dataSourceMap.get(_name).getJdbcTemplate();
    }

    NamedParameterJdbcTemplate loadParamsJdbcTemplate(String name) {
        final String _name = Strings.isNullOrEmpty(name) ? this.defName : name;
        Preconditions.checkState(dataSourceMap.containsKey(_name), "不存在 %s 对应的数据源配置...");
        return this.dataSourceMap.get(_name).getNamedParameterJdbcTemplate();
    }

    DataSourceTransactionManager getTransactionManager(String name) {
        final String _name = Strings.isNullOrEmpty(name) ? this.defName : name;
        Preconditions.checkState(dataSourceMap.containsKey(_name), "不存在 %s 对应的数据源配置...");
        return this.dataSourceMap.get(_name).getTransactionManager();
    }


    @Override
    public void setApplicationContext(ApplicationContext appCtx) throws BeansException {
        this.appCtx = appCtx;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Preconditions.checkState(CollectionUtils.isNotEmpty(config), "至少提供一个数据配源配置...");
        config.forEach(x -> {
            Map<String, String> map = MAP_SPLITTER.split(x);
            String name = MapUtils.getString(map, "name");
            boolean def = MapUtils.getBoolean(map, "default", false);
            DataSource ds = appCtx.getBean(MapUtils.getString(map, "db"), DataSource.class);
            this.dataSourceMap.put(name, new SingleDataSource(name, ds));
            if (def) defName = name;
        });
    }


    class SingleDataSource {
        private final String name;
        private final DataSource ds;
        private final JdbcTemplate jdbcTemplate;
        private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
        private final DataSourceTransactionManager transactionManager;

        SingleDataSource(String name, DataSource ds) {
            this.ds = ds;
            this.name = name;
            this.jdbcTemplate = new JdbcTemplate(ds);
            this.transactionManager = new DataSourceTransactionManager(ds);
            this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(ds);
        }

        JdbcTemplate getJdbcTemplate() {
            return jdbcTemplate;
        }

        NamedParameterJdbcTemplate getNamedParameterJdbcTemplate() {
            return namedParameterJdbcTemplate;
        }

        DataSourceTransactionManager getTransactionManager() {
            return transactionManager;
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("name", name)
                    .add("ds", ds)
                    .add("jdbcTemplate", jdbcTemplate)
                    .add("namedParameterJdbcTemplate", namedParameterJdbcTemplate)
                    .add("transactionManager", transactionManager)
                    .toString();
        }
    }
}
