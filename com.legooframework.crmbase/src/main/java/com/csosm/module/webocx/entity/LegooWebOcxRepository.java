package com.csosm.module.webocx.entity;

import com.csosm.commons.server.FileModifiedReload;
import com.csosm.module.base.entity.OrganizationEntity;
import com.csosm.module.base.entity.StoreEntity;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.digester3.Digester;
import org.apache.commons.digester3.binder.DigesterLoader;
import org.apache.commons.digester3.binder.RulesModule;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class LegooWebOcxRepository implements FileModifiedReload {

    private static final Logger logger = LoggerFactory.getLogger(LegooWebOcxRepository.class);
    private final List<WebOcx> webOcxs;
    private final RulesModule rulesModule;
    private Collection<File> files;

    LegooWebOcxRepository(RulesModule rulesModule, Collection<File> files) {
        this.webOcxs = Lists.newArrayList();
        this.rulesModule = rulesModule;
        this.files = Lists.newArrayList(files);
    }

    public Optional<List<PageDefinedDto>> loadStorePages(String groupName, OrganizationEntity company, StoreEntity store) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(groupName), "分组类型不可以为空值...");
        Preconditions.checkNotNull(company, "入参 company = ? 不可为空...");
        Preconditions.checkNotNull(store, "入参 store = ? 不可为空...");
        List<PageDefinedDto> pageDefinedDtos = Lists.newArrayList();
        for (WebOcx ocx : webOcxs) {
            if (!Objects.equals(ocx.getGroup(), groupName)) continue;
            Optional<List<PageDefinedDto>> _pgs = ocx.loadStorePages(company, store);
            if (_pgs.isPresent()) pageDefinedDtos.addAll(_pgs.get());
        }
        if (logger.isDebugEnabled())
            logger.debug(String.format("loadStorePages(%s,%s,%s ) size is %s", company.getId(),
                    store.getId(), groupName, pageDefinedDtos.size()));
        return Optional.fromNullable(CollectionUtils.isEmpty(pageDefinedDtos) ? null : pageDefinedDtos);
    }

    public Optional<List<PageDefinedDto>> loadCompanyPages(String groupName, OrganizationEntity company) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(groupName), "分组类型不可以为空值...");
        Preconditions.checkNotNull(company, "入参 company = ? 不可为空...");
        List<PageDefinedDto> pageDefinedDtos = Lists.newArrayList();
        for (WebOcx ocx : webOcxs) {
            if (!Objects.equals(ocx.getGroup(), groupName)) continue;
            Optional<List<PageDefinedDto>> _pgs = ocx.loadCompanyPages(company);
            if (_pgs.isPresent()) pageDefinedDtos.addAll(_pgs.get());
        }
        if (logger.isDebugEnabled())
            logger.debug(String.format("loadCompanyPages(%s,%s ) size is %s", company.getId(),
                    groupName, pageDefinedDtos.size()));
        return Optional.fromNullable(CollectionUtils.isEmpty(pageDefinedDtos) ? null : pageDefinedDtos);
    }

    public Optional<WebOcx> findById(String id) {
        WebOcx webOcx = null;
        for (WebOcx $it : webOcxs) {
            if (Objects.equals($it.getId(), id)) {
                webOcx = $it;
                break;
            }
        }
        return Optional.fromNullable(webOcx);
    }

    public Optional<PageDefinedDto> findByFullName(String fullName) {
        String[] args = StringUtils.split(fullName, '.');
        WebOcx webOcx = null;
        for (WebOcx $it : webOcxs) {
            if (Objects.equals($it.getId(), args[0])) {
                webOcx = $it;
                break;
            }
        }
        if (webOcx == null) return Optional.absent();
        return args.length == 2 ? webOcx.findPageById(args[1]) : webOcx.findPageById(null);
    }

    void init() {
        if (CollectionUtils.isEmpty(files)) return;
        Digester digester = DigesterLoader.newLoader(this.rulesModule).newDigester();
        for (File file : files) {
            try {
                Map<String, WebOcx> _temp = Maps.newHashMap();
                digester.push(_temp);
                digester.parse(file);
                List<WebOcx> ocxs = Lists.newArrayList();
                for (Map.Entry<String, WebOcx> entry : _temp.entrySet()) ocxs.add(entry.getValue());
                this.webOcxs.addAll(ocxs);
                digester.clear();
            } catch (Exception e) {
                logger.error(String.format("parse file=%s has error", file), e);
            } finally {
                digester.clear();
            }
        }
    }

    @Override
    public boolean isSupportFile(File file) {
        if (file == null || !file.exists()) return false;
        String file_name = file.getName();
        return StringUtils.endsWith(file_name, "-webocx-cfg.xml");
    }

    @Override
    public void building(Collection<File> files) {
        if (CollectionUtils.isEmpty(files)) return;
        Digester digester = DigesterLoader.newLoader(this.rulesModule).newDigester();
        List<WebOcx> all = Lists.newArrayList();
        boolean error = false;
        for (File file : files) {
            try {
                Map<String, WebOcx> _temp = Maps.newHashMap();
                digester.push(_temp);
                digester.parse(file);
                List<WebOcx> ocxs = Lists.newArrayList();
                for (Map.Entry<String, WebOcx> entry : _temp.entrySet()) ocxs.add(entry.getValue());
                all.addAll(ocxs);
                digester.clear();
            } catch (Exception e) {
                logger.error(String.format("parse file=%s has error", file.getAbsolutePath()), e);
                error = true;
                break;
            } finally {
                digester.clear();
            }
        }
        if (!error) {
            this.webOcxs.clear();
            this.files = files;
            this.webOcxs.addAll(all);
        }
    }
}
