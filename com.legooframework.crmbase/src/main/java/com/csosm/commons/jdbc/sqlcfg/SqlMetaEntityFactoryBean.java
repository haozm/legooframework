package com.csosm.commons.jdbc.sqlcfg;

import com.csosm.commons.jdbc.sqlcfg.rules.SqlConfigRulesModule;
import com.csosm.commons.vfs.MonitorFileSystem;
import com.google.common.base.Optional;
import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.AbstractFactoryBean;

import java.io.File;
import java.util.List;

/**
 * Created by Smart on 2017/3/31.
 *
 * @author hxj
 */
public class SqlMetaEntityFactoryBean extends AbstractFactoryBean<SqlMetaEntityFactory> {

    private static final Logger logger = LoggerFactory.getLogger(SqlMetaEntityFactoryBean.class);

    public SqlMetaEntityFactoryBean() {
    }

    @Override
    public Class<SqlMetaEntityFactory> getObjectType() {
        return SqlMetaEntityFactory.class;
    }

    @Override
    protected SqlMetaEntityFactory createInstance() throws Exception {
        SqlConfigRulesModule rulesModule = new SqlConfigRulesModule();
        MonitorFileSystem monitorFileSystem = getBeanFactory().getBean(MonitorFileSystem.class);
        SqlMetaEntityFactory factory = new SqlMetaEntityFactory(initConfiguration(), rulesModule, nameMatch,
                monitorFileSystem);
        Optional<List<File>> files = monitorFileSystem.findFiles(nameMatch);
        if (files.isPresent()) factory.building(files.get());
        return factory;
    }

    private Configuration initConfiguration() {
        Configuration _configuration = new Configuration(Configuration.VERSION_2_3_22);
        StringTemplateLoader stringTemplateLoader = new StringTemplateLoader();
        _configuration.setTemplateLoader(stringTemplateLoader);
        _configuration.setNumberFormat("#");
        _configuration.setClassicCompatible(true);
        return _configuration;
    }

    private String nameMatch;

    public void setNameMatch(String nameMatch) {
        this.nameMatch = nameMatch;
    }
}
