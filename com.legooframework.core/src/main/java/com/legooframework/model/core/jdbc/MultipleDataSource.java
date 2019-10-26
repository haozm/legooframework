package com.legooframework.model.core.jdbc;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
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
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MultipleDataSource implements InitializingBean, ApplicationContextAware {

    private final static Splitter.MapSplitter MAP_SPLITTER = Splitter.on(';').withKeyValueSeparator('=');
    private final List<SingleDataSource> dataSourceMap;
    private List<String> config;

    public MultipleDataSource() {
        this.dataSourceMap = Lists.newArrayList();
    }

    private ApplicationContext appCtx;

    public void setConfig(List<String> config) {
        this.config = config;
    }

    public JdbcTemplate loadJdbcTemplate(Object router) {
        return loadDataSource(router).getJdbcTemplate();
    }

    public NamedParameterJdbcTemplate loadParamsTemplate(Object router) {
        return loadDataSource(router).getNamedParameterJdbcTemplate();
    }

    public DataSourceTransactionManager getTransactionManager(Object router) {
        return loadDataSource(router).getTransactionManager();
    }

    private SingleDataSource loadDataSource(Object router) {
        Preconditions.checkNotNull(router, "路标标识 router 不可以为空...");
        Optional<SingleDataSource> singleDataSource = this.dataSourceMap.stream()
                .filter(x -> x.contains(router)).findFirst();
        Preconditions.checkState(singleDataSource.isPresent(), "无法获取 %s 对应的数据源...", router);
        return singleDataSource.get();
    }

    @Override
    public void setApplicationContext(ApplicationContext appCtx) throws BeansException {
        this.appCtx = appCtx;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Preconditions.checkState(CollectionUtils.isNotEmpty(config), "至少提供一个数据配源配置...");
        List<SingleDataSource> _list = Lists.newArrayList();
        config.forEach(x -> {
            Map<String, String> map = MAP_SPLITTER.split(x);
            String name = MapUtils.getString(map, "name");
            DataSource dataSource = appCtx.getBean(MapUtils.getString(map, "dataSource"), DataSource.class);
            String _companys = MapUtils.getString(map, "companys");
            Preconditions.checkArgument(!Strings.isNullOrEmpty(_companys), "至少指定一家公司信息...");
            Set<Integer> com_set = Stream.of(StringUtils.split(_companys, ','))
                    .map(Integer::valueOf).collect(Collectors.toSet());
            _list.add(new SingleDataSource(name, dataSource, com_set));
        });
        this.dataSourceMap.addAll(_list);
    }

    class SingleDataSource {
        private final String name;
        private final DataSource dataSource;
        private final JdbcTemplate jdbcTemplate;
        private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
        private final DataSourceTransactionManager transactionManager;
        private final Set<Integer> companys;

        SingleDataSource(String name, DataSource dataSource, Set<Integer> companys) {
            this.name = name;
            this.dataSource = dataSource;
            this.transactionManager = new DataSourceTransactionManager(dataSource);
            this.jdbcTemplate = new JdbcTemplate(dataSource);
            this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(this.jdbcTemplate);
            this.companys = ImmutableSet.copyOf(companys);
        }

        boolean contains(Object router) {
            return companys.contains(router);
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
                    .add("dataSource", dataSource)
                    .add("jdbcTemplate", jdbcTemplate)
                    .add("namedParameterJdbcTemplate", namedParameterJdbcTemplate)
                    .add("transactionManager", transactionManager)
                    .toString();
        }
    }
}
