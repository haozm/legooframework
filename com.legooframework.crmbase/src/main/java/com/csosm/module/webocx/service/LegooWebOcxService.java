package com.csosm.module.webocx.service;

import com.csosm.commons.adapter.LoginUserContext;

import com.csosm.commons.jdbc.sqlcfg.ColumnMeta;
import com.csosm.commons.jdbc.sqlcfg.SqlMetaEntity;
import com.csosm.commons.jdbc.sqlcfg.SqlMetaEntityFactory;
import com.csosm.commons.server.AbstractBaseServer;
import com.csosm.module.base.entity.KvDictEntity;
import com.csosm.module.base.entity.KvDictEntityAction;
import com.csosm.module.base.entity.OrganizationEntity;
import com.csosm.module.base.entity.StoreEntity;
import com.csosm.module.query.QueryEngineService;
import com.csosm.module.webocx.entity.*;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

public class LegooWebOcxService extends AbstractBaseServer {

    private final static Logger logger = LoggerFactory.getLogger(LegooWebOcxService.class);

    private static Ordering<MemberGroupInfo> ordering = Ordering.natural()
            .onResultOf((Function<MemberGroupInfo, Integer>) MemberGroupInfo::getIndex);

    public Optional<List<MemberGroupInfo>> statisticalByGroup(String groupName, OrganizationEntity company,
                                                              StoreEntity store, LoginUserContext loginUser) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(groupName), "分组类型不可以为空值...");
        Preconditions.checkNotNull(company, "入参 OrganizationEntity company = ? 不可为空...");
        Preconditions.checkNotNull(store, "入参  StoreEntity store = ? 不可为空...");
        Optional<List<PageDefinedDto>> pgs = getBean(LegooWebOcxRepository.class)
                .loadStorePages(groupName, company, store);
        if (!pgs.isPresent()) return Optional.absent();

        // 权限过滤
        List<PageDefinedDto> list = getBean(GroupAuthorEntityAction.class).filterByStore(store, pgs.get());
        if (CollectionUtils.isEmpty(list)) return Optional.absent();

        // 统计数量
        List<ListenableFuture<MemberGroupInfo>> lbfs = Lists.newArrayList();
        for (PageDefinedDto pg : pgs.get()) {
            ListenableFuture<MemberGroupInfo> lbf = getListeningExecutor().submit(new C0(pg, loginUser));
            lbfs.add(lbf);
        }
        Futures.allAsList(lbfs);

        List<MemberGroupInfo> res = Lists.newArrayList();
        try {
            for (ListenableFuture<MemberGroupInfo> lbf : lbfs) res.add(lbf.get());
        } catch (Exception e) {
            logger.error(String.format("ListenableFuture get count for %s has error", groupName), e);
            throw new RuntimeException(e);
        }
        Collections.sort(res, ordering);
        return Optional.of(res);
    }

    public Optional<PageDefinedDto> loadByGroupId(String groupId, LoginUserContext loginUser) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(groupId), "分组Id不可以为空值...");
        Optional<PageDefinedDto> pgd = getBean(LegooWebOcxRepository.class).findByFullName(groupId);
        if (!pgd.isPresent()) return Optional.absent();
        boolean diabled = getBean(GroupAuthorEntityAction.class).isActived(loginUser, pgd.get());
        if (diabled) return Optional.absent();
        return pgd;
    }

    /**
     * 内部类 用于并行技术统计数据SQL
     */
    class C0 implements Callable<MemberGroupInfo> {

        private PageDefinedDto pd;
        private LoginUserContext u;

        C0(PageDefinedDto pd, LoginUserContext u) {
            this.pd = pd;
            this.u = u;
        }

        @Override
        public MemberGroupInfo call() throws Exception {
            MemberGroupInfo res = null;
            if (pd.isShowCount()) {
                Map<String, Object> source = u.toMap();
                pd.getPageDefined().holdParam(source);
                long count = getJdbcQuery().queryForCount(pd.getSqlModel(), pd.getSqlStmtId(), source);
                res = new MemberGroupInfo(pd.getFullName(), pd.getIndex(), pd.getTitle(), count, pd.getQueryParams().orNull());
                if (logger.isDebugEnabled())
                    logger.debug(String.format("MemberGroupInfo[%s]:%s", pd.getTitle(), res));
            } else {
                res = new MemberGroupInfo(pd.getFullName(), pd.getIndex(), pd.getTitle(), 0L, pd.getQueryParams().orNull());
            }
            return res;
        }
    }

    public Optional<Map<String, Object>> loadOcxById(String ocxId, String subPage, LoginUserContext user) {
        Preconditions.checkState(user.getCompany().isPresent(), "当前登陆用户所属公司不可以空...");
//        Preconditions.checkState(user.getStore().isPresent(), "当前登陆用户所属门店不可以空...");
        Optional<WebOcx> webOcx = getBean(LegooWebOcxRepository.class).findById(ocxId);
        if (!webOcx.isPresent()) return Optional.absent();
        Optional<List<PageDefinedDto>> pds = Optional.absent();
        if(user.getStore().isPresent()) {
        	pds = webOcx.get().loadStorePages(user.getCompany().get(), user.getStore().get());
        }else {
        	pds = webOcx.get().loadCompanyPages(user.getCompany().get());
        }
        Preconditions.checkState(pds.isPresent());
        Preconditions.checkState(pds.get().size() == 1);
        PageDefined pageDefined = null;
        if (Strings.isNullOrEmpty(subPage)) {
            pageDefined = pds.get().get(0).getPageDefined();
        } else {
            String fullName = String.format("%s.%s", ocxId, subPage);
            for (PageDefinedDto $it : pds.get()) {
                if ($it.getFullName().equals(fullName)) {
                    pageDefined = $it.getPageDefined();
                    break;
                }
            }
        }
        Map<String, Object> config = Maps.newHashMap();
        Map<String, Object> tableConfig = webOcx.get().toViewMap();

        if (webOcx.get().getFullName().isPresent()) {
            if (pageDefined.getMetas().isPresent()) {
                tableConfig.put("meta", buildMetas(pageDefined));
            } else {
                tableConfig.put("meta", buildMetas(webOcx.get().getSqlModel(), webOcx.get().getSqlStmtId()));
            }
        }

        if (pageDefined.getOperates().isPresent()) {
            List<Map<String, Object>> list = Lists.newArrayList();
            for (Operate $it : pageDefined.getOperates().get()) {
            	if($it.hasRole(user)) list.add($it.toMap());
            }
            tableConfig.put("operate", list);
        }

        if (pageDefined.getButtons().isPresent()) {
            List<Map<String, Object>> list = Lists.newArrayList();
            for (Operate $it : pageDefined.getButtons().get()) {
            	if($it.hasRole(user)) list.add($it.toMap());
            }
            tableConfig.put("batchOperate", list);
        }

        if (pageDefined.getSubPageDefineds().isPresent()) {
            tableConfig.put("childrenActive", pageDefined.getActive());
            List<Map<String, Object>> children = Lists.newArrayList();
            for (SubPageDefined $it : pageDefined.getSubPageDefineds().get()) {
                Map<String, Object> params = $it.toMap();
                params.put("meta", buildMetas($it.getSqlModel(), $it.getSqlStmtId()));
                children.add(params);
            }
            tableConfig.put("children", children);
        }

        config.put("tableConfig", tableConfig);

        Map<String, Object> user_params = user.toMap();
        if (pageDefined.getCdnItems().isPresent()) {
            for (CdnItem $it : pageDefined.getCdnItems().get()) {
                $it.holdParam(user_params);
            }
        }
        config.put("fixeCdn", user_params);

        List<Map<String, Object>> searchConfig = Lists.newArrayList();
        for (OcxItem $it : pageDefined.getOcxItems()) {
            Map<String, Object> item = $it.toViewMap();
            if ($it.getDataSource().isPresent()) {
            	Map<String, Object> buildByDs = buildByDs($it.getDataSource().get(), $it.isAll(), user);
            	if(null != item.get("data")) {
            		Map<String,Object> value = (Map<String, Object>) item.get("data");
            		value.putAll(buildByDs);
            		item.put("data", value);
            	}else {
            		item.put("data", buildByDs);
            	}
            }
            searchConfig.add(item);
        }
        if (CollectionUtils.isNotEmpty(searchConfig)) config.put("searchConfig", searchConfig);
        return Optional.of(config);
    }

    /**
     * 加载 字典类数据信息
     *
     * @param dataSource
     * @param user
     * @return
     */
    private Map<String, Object> buildByDs(DataSource dataSource, boolean isAll, LoginUserContext user) {
        Preconditions.checkNotNull(user);
        Preconditions.checkState(user.getCompany().isPresent());
        Map<String, Object> map = Maps.newHashMap();
        map.put("isall", isAll);
        final List<Map<String, Object>> list = Lists.newArrayList();
        if (dataSource.isEnum()) {
            list.addAll(dataSource.toViewMap());
        } else if (dataSource.isDict()) {
            Optional<List<KvDictEntity>> kvs = getBean(KvDictEntityAction.class).findByType(user.getCompany().get(),
                    dataSource.getContext());
            if (kvs.isPresent()) {
                for (KvDictEntity $it : kvs.get()) {
                    Map<String, Object> _map = Maps.newHashMap();
                    _map.put("value", $it.getKey());
                    _map.put("label", $it.getValue());
                    list.add(_map);
                }
            }
        } else if (dataSource.isSql()) {
            Optional<List<Map<String, Object>>> query_list = getJdbcQuery().queryForList(dataSource.getContext(), user.toMap());
            if (query_list.isPresent()) list.addAll(query_list.get());
        } else if (dataSource.isQuery()) {
            String[] arges = StringUtils.split(dataSource.getContext(), '.');
            Optional<List<Map<String, Object>>> query_list = getJdbcQuery().queryForList(arges[0], arges[1], user.toMap());
            if (query_list.isPresent()) list.addAll(query_list.get());
        }
        map.put("items", list);
        return map;
    }

    private List<Map<String, Object>> buildMetas(String model, String stmtId) {
    	SqlMetaEntity sqlStatement = getSQLStatementFactory().getSqlMetaEntity(model, getQueryStmtId(stmtId));
        Preconditions.checkState(sqlStatement.getColumnMetas().isPresent(), "modle=  %s ,stmtid=%s 没有配置查询结果Meta",
                model, stmtId);
        List<Map<String, Object>> list = Lists.newArrayList();
        for (ColumnMeta $it : sqlStatement.getColumnMetas().get()) {
        	Optional<String> stmtIdOpt = getQueryType(stmtId);
//        	if(getQueryType(stmtId).isPresent()) {
        		$it = $it.get(stmtIdOpt.isPresent()?stmtIdOpt.get():null);
        		if(null == $it) continue;
//        	}
            Map<String, Object> meta = Maps.newHashMap();
            meta.put("id", $it.getId());
            meta.put("name", $it.getName());
            meta.put("fixed", $it.isFixed());
            meta.put("type", $it.getShowType().or($it.getType()));
            list.add(meta);
        }
        return list;
    }
    
    private Optional<String> getQueryType(String stmtId){
    	if(Strings.isNullOrEmpty(stmtId) || stmtId.indexOf(":") == -1) return Optional.absent();
    	return Optional.fromNullable(stmtId.split(":")[1]);
    }
    
    private String getQueryStmtId(String stmtId) {
    	if(stmtId.indexOf(":") != -1) return stmtId.split(":")[0];
    	return stmtId;
    }

    private List<Map<String, Object>> buildMetas(PageDefined pageDefined) {
        Preconditions.checkState(pageDefined.getMetas().isPresent());
        List<Map<String, Object>> list = Lists.newArrayList();
        for (ColumMeta $it : pageDefined.getMetas().get()) {
            Map<String, Object> meta = Maps.newHashMap();
            meta.put("id", $it.getId());
            meta.put("name", $it.getName());
            meta.put("fixed", $it.isFixed());
            meta.put("type", $it.getType());
            list.add(meta);
        }
        return list;
    }

    private synchronized ListeningExecutorService getListeningExecutor() {
        if (listeningExecutorService != null) return listeningExecutorService;
        ThreadPoolTaskExecutor executor = getBean("csosm-executor", ThreadPoolTaskExecutor.class);
        listeningExecutorService = MoreExecutors.listeningDecorator(executor.getThreadPoolExecutor());
        return listeningExecutorService;
    }

    private ListeningExecutorService listeningExecutorService = null;

    private QueryEngineService getJdbcQuery() {
        return getBean("webOcxQueryEngineService", QueryEngineService.class);
    }

    private SqlMetaEntityFactory getSQLStatementFactory() {
        return getBean("sqlMetaEntityFactory", SqlMetaEntityFactory.class);
    }
    
}
