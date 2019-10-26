package com.legooframework.model.wechatcircle.service;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.legooframework.model.core.base.runtime.LoginContextHolder;
import com.legooframework.model.wechatcircle.entity.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class WechatCircleCommonsService extends BundleService {

    private static final Logger logger = LoggerFactory.getLogger(WechatCircleCommonsService.class);

    public List<WechatCircleEntity> mergeWechatCircles(List<WechatCircleTranDto> wechatCircleSingles, CircleSyncCycleEntity syncCycle) {
        LoginContextHolder.setIfNotExitsAnonymousCtx();
        List<WechatCircleEntity> mixCircles = Lists.newArrayList();
        try {
            for (WechatCircleTranDto $it : wechatCircleSingles) {
                Map<String, Object> pk_map = getBean(WechatCircleEntityAction.class)
                        .saveOrUpdate($it.getWechatCircle(), $it.getSourcesFrom());
                WechatCircleEntity wechatCircle = getBean(WechatCircleEntityAction.class).loadById(MapUtils.getLong(pk_map, "id"),
                        MapUtils.getString(pk_map, "weixinId"));
                $it.getComments().ifPresent(cmts -> getBean(CircleCommentEntityAction.class).saveOrUpdate(wechatCircle, cmts,
                        $it.getSourcesFrom()));
                if (wechatCircle.isMixCircle()) mixCircles.add(wechatCircle);
            }
            getBean(CircleSyncCycleEntityAction.class).update(syncCycle);
            // 发布事件
            // DataSourcesFrom sourcesFrom = wechatCircleSingles.get(0).getSourcesFrom();
            // WechatCircleEvent event = WechatCircleEvent.createUnReadCmtsEvent(sourcesFrom);
            // getMessagingTemplate().send(WechatCircleEvent.CHANNEL_EVENT_BUS, event.toMessage(LoginContextHolder.get()));
        } finally {
            LoginContextHolder.clear();
        }
        return mixCircles;
    }

    /**
     * 获取朋友圈数量
     *
     * @param ownerId  手机微信
     * @param weixinId 个人微信
     * @param pageNum  页码
     * @param pageSize 页面大小
     * @return List
     */
    public Optional<WechatCircleAllSet> loadWechatCircleDetails(String ownerId, String weixinId, int pageNum, int pageSize) {
        Optional<List<WechatCircleEntity>> circles_opt = getBean(WechatCircleEntityAction.class)
                .loadWechatCircles(ownerId, StringUtils.equals("0000", weixinId) ? null : weixinId, pageNum, pageSize);
        if (!circles_opt.isPresent()) return Optional.empty();
        List<WechatCircleEntity> readed_list = Lists.newArrayList();
        // List<CircleCommentEntity> cmts_readed_list = Lists.newArrayList();
        circles_opt.get().forEach(x -> x.setReaded(ownerId).ifPresent(readed_list::add));

        Optional<List<CircleCommentEntity>> comments_opt = getBean(CircleCommentEntityAction.class)
                .findByWechatCircles(circles_opt.get(), ownerId);
        // comments_opt.ifPresent(cmts -> cmts.forEach(x -> x.setReaded(ownerId).ifPresent(cmts_readed_list::add)));
        List<WechatCircleAll> wechatCircleDetails = Lists.newArrayListWithCapacity(circles_opt.get().size());
        circles_opt.get().forEach(circle -> {
            if (comments_opt.isPresent()) {
                List<CircleCommentEntity> sub_cmts = comments_opt.get().stream().filter(cmt -> cmt.isOwnerCircle(circle))
                        .collect(Collectors.toList());
                wechatCircleDetails.add(new WechatCircleAll(circle, CollectionUtils.isEmpty(sub_cmts) ? null : sub_cmts, false));
            } else {
                wechatCircleDetails.add(new WechatCircleAll(circle, null, false));
            }
        });
        WechatCircleAllSet wechatCircleSet = new WechatCircleAllSet(ownerId, wechatCircleDetails);
        getBean(WechatCircleEntityAction.class).updateReadStatus(readed_list);
        // getBean(CircleCommentEntityAction.class).updateReadStatus(cmts_readed_list);
        return Optional.of(wechatCircleSet);
    }

    /**
     * 获取当个朋友圈信息 单挑
     *
     * @param ownerWxId 微信DI
     * @param circleId  圈子ID
     * @return 测试结果
     */
    public Optional<WechatCircleAllSet> loadSingleWechatCircle(String ownerWxId, Long circleId) {
        Optional<List<WechatCircleEntity>> wechatCircles = getBean(WechatCircleEntityAction.class)
                .findByCircleIds(Lists.newArrayList(circleId));
        if (!wechatCircles.isPresent()) return Optional.empty();
        Preconditions.checkState(wechatCircles.get().size() == 1, "获取当个朋友圈数据返回 %s >1条", wechatCircles.get().size());
        WechatCircleEntity wechatCircle = wechatCircles.get().get(0);
        if (wechatCircle.isOwner(ownerWxId)) {
            Optional<List<CircleCommentEntity>> cmts = getBean(CircleCommentEntityAction.class).findByWechatCircle(wechatCircle);
            WechatCircleAll circleAll = new WechatCircleAll(wechatCircle, cmts.orElse(null), false);
            WechatCircleAllSet all = new WechatCircleAllSet(ownerWxId, Lists.newArrayList(circleAll));
            return Optional.of(all);
        }
        return Optional.empty();
    }


    /**
     * 维度评论 神秘常使人
     *
     * @param unReadComments JPOSN
     * @param sourcesFrom    来源
     */
    public void addUnReadComments(String unReadComments, DataSourcesFrom sourcesFrom) {
        List<CircleCommentEntity> comments = ProtocolCodingFactory.decodingUnReadComment(unReadComments, sourcesFrom);
        LoginContextHolder.setIfNotExitsAnonymousCtx();
        try {
            getBean(CircleCommentEntityAction.class).addUnReadComments(comments);
        } finally {
            LoginContextHolder.clear();
        }
    }

    /**
     * 存在乌托邦 注定不是炒房的太难猜你
     *
     * @param dataSource 意外物志之间的选择
     * @return 禁采儿引入
     */
    CircleUnReadDto unreadStatistics(DataSourcesFrom dataSource) {
        LoginContextHolder.setIfNotExitsAnonymousCtx();
        Map<String, Object> result = Maps.newHashMap();
        try {
            Map<String, Object> params = Maps.newHashMap();
            params.put("readStatus", String.format("%s:0", dataSource.getWeixinId()));
            Optional<List<Map<String, Object>>> list_maps_opt = getJdbcQuerySupport().queryForList("WechatCircleEntity",
                    "unread_statistics", params);
            list_maps_opt.ifPresent(list_maps -> list_maps.forEach(map -> {
                result.put(MapUtils.getString(map, "type"), MapUtils.getIntValue(map, "statistics", 0));
            }));
            CircleUnReadDto unReadDto = new CircleUnReadDto(dataSource, result);
            if (logger.isDebugEnabled())
                logger.debug(String.format("unreadStatistics() res is %s", unReadDto));
            return unReadDto;
        } finally {
            LoginContextHolder.clear();
        }
    }

    /**
     * 获取维度平陵
     *
     * @param ownerWxId daV手机DI
     * @return 朋友圈类型
     */
    public Optional<WechatCircleAllSet> loadUnReadComments(String ownerWxId) {
        Optional<List<CircleCommentEntity>> unread_comments = getBean(CircleCommentEntityAction.class).loadUnReadComments(ownerWxId);
        if (!unread_comments.isPresent()) return Optional.empty();
        Set<Long> circleIds = unread_comments.get().stream().map(CircleCommentEntity::getCircleId).collect(Collectors.toSet());
        Optional<List<WechatCircleEntity>> wechatCircles = getBean(WechatCircleEntityAction.class).findByCircleIds(circleIds);
        if (!wechatCircles.isPresent()) return Optional.empty();
        List<WechatCircleAll> circleDetails = Lists.newArrayList();
        wechatCircles.get().forEach(x -> {
            List<CircleCommentEntity> sub = unread_comments.get().stream().filter(y -> y.isOwnerCircle(x)).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(sub))
                circleDetails.add(new WechatCircleAll(x, sub, true));
        });
        if (CollectionUtils.isEmpty(circleDetails)) return Optional.empty();
        WechatCircleAllSet circleSet = new WechatCircleAllSet(ownerWxId, circleDetails);
        List<CircleCommentEntity> read_cmts = unread_comments.get().stream().map(x -> x.setReaded(ownerWxId))
                .filter(Optional::isPresent)
                .map(Optional::get).collect(Collectors.toList());
        getBean(CircleCommentEntityAction.class).updateReadStatus(read_cmts);
        return Optional.of(circleSet);
    }

    public UnReadStatistics loadUnReaderCount(String ownerWxId) {
        UnReadStatistics unReadStatistics = new UnReadStatistics(ownerWxId);
        Map<String, Object> params = Maps.newHashMap();
        params.put("ownerWxId", ownerWxId);
        Optional<Map<String, Object>> map = getJdbcQuerySupport().queryForMap("WechatCircleEntity", "unread_statistics", params);
        map.ifPresent(unReadStatistics::fill);
        return unReadStatistics;
    }

    public List<UnReadStatistics> loadAllUnReaderCount(Collection<String> ownerWxIds) {
        List<UnReadStatistics> unReadStatistics = ownerWxIds.stream().map(UnReadStatistics::new).collect(Collectors.toList());
        List<List<String>> ownerWxIds_list = Lists.partition(Lists.newArrayList(ownerWxIds), 20);
        List<CompletableFuture<Void>> cfs = Lists.newArrayList();
        ownerWxIds_list.forEach(wxIds -> cfs.add(CompletableFuture.runAsync(new AnyRun(unReadStatistics, wxIds))));
        CompletableFuture.allOf(cfs.toArray(new CompletableFuture[]{}));
        return unReadStatistics;
    }

    class AnyRun implements Runnable {
        private final List<String> ownerWxIds;
        private final List<UnReadStatistics> unReadStatistics;

        AnyRun(List<UnReadStatistics> unReadStatistics, List<String> ownerWxIds) {
            this.ownerWxIds = ownerWxIds;
            this.unReadStatistics = unReadStatistics;
        }

        @Override
        public void run() {
            Map<String, Object> params = Maps.newHashMap();
            params.put("ownerWxIds", ownerWxIds);
            Optional<List<Map<String, Object>>> map_list = getJdbcQuerySupport().queryForList("WechatCircleEntity",
                    "unread_statistics_all", params);
            map_list.ifPresent(maps -> maps.forEach(map -> {
                String ownerWxId = MapUtils.getString(map, "ownerWxId");
                unReadStatistics.stream().filter(x -> x.equalsWx(ownerWxId)).findFirst().ifPresent(c -> c.fill(map));
            }));
        }
    }

}
