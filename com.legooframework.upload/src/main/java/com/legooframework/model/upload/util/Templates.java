package com.legooframework.model.upload.util;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;
import com.google.common.base.Throwables;
import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Templates {

    private static final Logger logger = LoggerFactory.getLogger(Templates.class);

    private final static String PATH_TEMPLATE_NAME = "path";

    private final static Configuration configuration = initConfiguration();

    /**
     * 获取freemarker模板
     *
     * @param value
     * @return
     */
    public static Template newTemplate(String value) {
        Template pathTemplate = null;
        try {
            pathTemplate = new Template(PATH_TEMPLATE_NAME, new StringReader(value), configuration);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        return pathTemplate;
    }

    /**
     * 解析数据
     *
     * @param template
     * @param dataMap
     * @return
     */
    public static String execute(Template template, Map<String, Object> dataMap) {
        StringWriter sw = new StringWriter();
        try {
            template.process(dataMap, sw);
            return sw.toString();
        } catch (Exception e) {
            logger.error("template parse data cause exception", e);
            throw Throwables.propagate(e);
        }

    }

    /**
     * 初始化freemarker设置
     *
     * @return
     */
    private static Configuration initConfiguration() {
        Configuration _configuration = new Configuration(Configuration.VERSION_2_3_22);
        StringTemplateLoader stringTemplateLoader = new StringTemplateLoader();
        _configuration.setTemplateLoader(stringTemplateLoader);
        _configuration.setNumberFormat("#");
        _configuration.setClassicCompatible(true);
        return _configuration;
    }

}
