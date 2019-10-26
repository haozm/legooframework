package com.csosm.module.menu.entity;

import com.csosm.commons.server.FileModifiedReload;
import com.csosm.commons.vfs.MonitorFileSystem;
import com.google.common.base.Preconditions;
import com.google.common.collect.*;
import org.apache.commons.digester3.Digester;
import org.apache.commons.digester3.binder.DigesterLoader;
import org.apache.commons.digester3.binder.RulesModule;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ResourcesFactory implements FileModifiedReload {

    private static final Logger logger = LoggerFactory.getLogger(ResourcesFactory.class);

    private final RulesModule rulesModule;
    private MonitorFileSystem monitorFileSystem;
    private final Pattern pattern;
    private final String endWith;
    private List<ConfigByFileMeta> configByFileMetas;

    ResourcesFactory(RulesModule rulesModule, MonitorFileSystem monitorFileSystem, String pattern) {
        this.rulesModule = rulesModule;
        this.pattern = Pattern.compile(pattern);
        this.endWith = pattern;
        this.monitorFileSystem = monitorFileSystem;
        this.configByFileMetas = Lists.newArrayList();
    }

    private Optional<ConfigByFileMeta> loadByTenant(Long tenantId) {
        Preconditions.checkState(!CollectionUtils.isEmpty(configByFileMetas), "配置信息为空，请初始化配置文件..");
        Optional<ConfigByFileMeta> fileMeta = configByFileMetas.stream()
                .filter(x -> x.exitsTenant(tenantId)).findFirst();
        if (!fileMeta.isPresent())
            logger.warn(String.format("租户 %s 未配置对应的系统功能资源....", tenantId));
        return fileMeta;
    }

    public Optional<ResEntity> getMenu(Long tenantId) {
        Optional<ConfigByFileMeta> fileMeta = loadByTenant(tenantId);
        return fileMeta.isPresent() ? Optional.ofNullable(fileMeta.get().getMenu(tenantId))
                : Optional.empty();
    }

    public Optional<List<ResEntity>> getPages(Long tenantId) {
        Optional<ConfigByFileMeta> fileMeta = loadByTenant(tenantId);
        return fileMeta.isPresent() ? Optional.ofNullable(fileMeta.get().getPage(tenantId)) :
                Optional.empty();
    }

    public Optional<ResourceDto> getAllReource(Long tenantId) {
        Optional<ConfigByFileMeta> fileMeta = loadByTenant(tenantId);
        if (!fileMeta.isPresent()) return Optional.empty();
        // 菜单根目录
        ResEntity root = fileMeta.get().getMenu(tenantId);
        Set<String> paths = Sets.newHashSet();
        List<ResEntity> pages = getPageList(tenantId);
        pages.forEach(p -> paths.addAll(p.getPaths()));
        ResourceDto root_dto = root.createDto();
        root_dto.filter(paths);
        return Optional.of(root_dto);
    }

    public Optional<ResourceDto> getSubReource(Long tenantId, Collection<String> resIds) {
        Optional<ConfigByFileMeta> fileMeta = loadByTenant(tenantId);
        if (CollectionUtils.isEmpty(resIds) || !fileMeta.isPresent()) return Optional.empty();
        // 菜单根目录
        ResEntity root = fileMeta.get().getMenu(tenantId);
        List<ResEntity> pageList = getPageList(tenantId);

        Set<String> paths = Sets.newHashSet();

        List<ResEntity> pages = pageList.stream()
                .filter(p -> resIds.contains(p.getId())).collect(Collectors.toList());
        pages.forEach(p -> paths.addAll(p.getPaths()));

        ResourceDto root_dto = root.createDto();
        root_dto.filter(paths);

        return Optional.of(root_dto);
    }

    public Optional<List<ResEntity>> getPageRes(Long tenantId, String... ids) {
        Optional<ConfigByFileMeta> fileMeta = loadByTenant(tenantId);
        if (!fileMeta.isPresent()) return Optional.empty();
        List<ResEntity> pageLists = fileMeta.get().getPage(tenantId);
        List<ResEntity> sub_pages = pageLists.stream()
                .filter(x -> ArrayUtils.contains(ids, x.getId())).collect(Collectors.toList());
        return Optional.ofNullable(CollectionUtils.isEmpty(sub_pages) ? null : sub_pages);
    }

    public List<ResEntity> getPageList(Long tenantId) {
        Optional<ConfigByFileMeta> fileMeta = loadByTenant(tenantId);
        return fileMeta.map(configByFileMeta -> configByFileMeta.getPage(tenantId)).orElse(null);
    }

    @Override
    public void building(Collection<File> empty) {
        List<ConfigByFileMeta> list = Lists.newArrayList();
        boolean error = false;
        com.google.common.base.Optional<List<File>> files = monitorFileSystem.findFiles(endWith);
        if (!files.isPresent()) return;
        for (File file : files.get()) {
            if (!isSupportFile(file)) return;
            Digester digester = DigesterLoader.newLoader(this.rulesModule).newDigester();
            Map<Long, ResEntity> menu_map = Maps.newHashMap();
            ListMultimap<Long, ResEntity> page_list = ArrayListMultimap.create();
            try {
                digester.push(menu_map);
                digester.push("page_list", page_list);
                digester.parse(file);
                if (logger.isDebugEnabled()) logger.debug(String.format("finish parse sql-cfg: %s", file));
                ConfigByFileMeta fileMeta = new ConfigByFileMeta(file, menu_map, page_list);
                list.add(fileMeta);
            } catch (Exception e) {
                logger.error(String.format("parse file=%s has error", file), e);
                error = true;
                break;
            } finally {
                digester.clear();
            }
        }
        if (!error) {
            this.configByFileMetas.clear();
            this.configByFileMetas.addAll(list);
        }
    }

    @Override
    public boolean isSupportFile(File fileName) {
        boolean res = pattern.matcher(fileName.getAbsolutePath()).matches();
        if (logger.isDebugEnabled())
            logger.debug(String.format("%s match res %s is %s", fileName.getAbsolutePath(), pattern, res));
        return res;
    }

}
