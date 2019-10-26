package com.csosm.module.base.entity;

import com.csosm.commons.entity.BaseEntityAction;
import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.ResultSetExtractor;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

public class InitCompanySqlEntityAction extends BaseEntityAction<InitCompanySqlEntity> {

    private static final Logger logger = LoggerFactory.getLogger(InitCompanySqlEntityAction.class);

    public InitCompanySqlEntityAction() {
        super(null, null);
    }

    public void initCompany(Map<String,Object> datas) {
        List<String> exec_sqls = parseFile(datas);
        if (CollectionUtils.isEmpty(exec_sqls)) return;
        exec_sqls.forEach(sql -> {
            Objects.requireNonNull(super.getJdbcTemplate()).execute(sql);
            if (logger.isDebugEnabled())
                logger.debug(String.format("execute(%s) is ok.", sql));
        });
    }

    private List<String> parseFile(Map<String,Object> datas) {
        Preconditions.checkState(resource.exists(), "待加载的SQL文件不存在...初始化失败。");
        try {
            File file = resource.getFile();
            List<String> read_lines = FileUtils.readLines(file, Charsets.UTF_8);
            List<String> exec_sqls = Lists.newArrayList();
            StringBuffer buffer = new StringBuffer(1024);
            for (String line : read_lines) {
                String _ln = StringUtils.trimToEmpty(line);
                if (StringUtils.isEmpty(_ln) || StringUtils.startsWith(line, "--")) continue;
                buffer.append(_ln).append(" ");
                if (StringUtils.endsWith(_ln, ";")) {
                	_ln = replace(buffer.toString(), datas);
                    exec_sqls.add(_ln);
                    buffer = new StringBuffer();
                }
            }
            return exec_sqls;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String replace(String content,Map<String,Object> datas) {
    	for(Entry<String, Object> entry : datas.entrySet()) {
    		String key = String.format(":%s", entry.getKey());
    		String value = entry.getValue().toString();
    		content = StringUtils.replaceIgnoreCase(content,key , value);
    	}
    	return content;
    }
    
    @Override
    protected ResultSetExtractor<InitCompanySqlEntity> getResultSetExtractor() {
        return null;
    }

    private Resource resource;

    public void setResource(Resource resource) {
        this.resource = resource;
    }
    
}
