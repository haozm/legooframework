package com.legooframework.model.dict.entity;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.legooframework.model.core.base.entity.BaseEntityAction;
import com.legooframework.model.core.config.MonitorFileSystem;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;

import java.io.File;
import java.io.FileReader;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class LunarIndexAction extends BaseEntityAction<LunarIndexEntity> {

    private static final Logger logger = LoggerFactory.getLogger(LunarIndexAction.class);

    public LunarIndexAction() {
        super(null);
    }

    private File csvFile;
    private LunarIndexSet lunarIndexSet;

    public LunarIndexSet getLunarIndexSet() {
        build();
        return lunarIndexSet;
    }

    private void build() {
        if (lunarIndexSet != null) return;
        synchronized (logger) {
            if (lunarIndexSet != null) return;
            try {
                CSVParser parser = CSVParser.parse(new FileReader(csvFile), CSVFormat.DEFAULT);
                List<LunarIndexEntity> list = Lists.newArrayListWithCapacity(47755);
                for (CSVRecord $it : parser) list.add(new LunarIndexEntity($it));
                if (logger.isDebugEnabled())
                    logger.debug(String.format("parser lunarIndex size is %s", list.size()));
                this.lunarIndexSet = new LunarIndexSet(list);
            } catch (Exception e) {
                logger.error(String.format("CSVParser parser = CSVParser.parse(new FileReader(%s), CSVFormat.DEFAULT)", csvFile), e);
                throw new RuntimeException(e);
            }
        }
    }

    private void init() {
        Preconditions.checkNotNull(monitorFileSystem, "需初始化Spring Bean MonitorFileSystem By IOC 容器...");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(pattern), "文件匹配路径不可以未空...");
        List<String> patterns = Lists.newArrayList();
        patterns.add(pattern);
        Optional<Collection<File>> files = monitorFileSystem.findFiles(patterns);
        Preconditions.checkState(files.isPresent(), "无法加载 %s 对应的CVS文件....", pattern);
        this.csvFile = Lists.newArrayList(files.get()).get(0);
    }

    private String pattern;
    private MonitorFileSystem monitorFileSystem;

    public void setMonitorFileSystem(MonitorFileSystem monitorFileSystem) {
        this.monitorFileSystem = monitorFileSystem;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    @Override
    protected RowMapper<LunarIndexEntity> getRowMapper() {
        return null;
    }
}
