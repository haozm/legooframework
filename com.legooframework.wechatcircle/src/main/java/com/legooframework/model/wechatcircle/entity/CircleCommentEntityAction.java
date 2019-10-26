package com.legooframework.model.wechatcircle.entity;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.legooframework.model.core.base.entity.BaseEntityAction;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class CircleCommentEntityAction extends BaseEntityAction<CircleCommentEntity> {

    private static final Logger logger = LoggerFactory.getLogger(CircleCommentEntityAction.class);

    public CircleCommentEntityAction() {
        super(null);
    }

    /**
     * @param wechatCircle 朋友圈
     * @param un_save_cmts 评论数量
     * @param sourcesFrom  数据来源
     */
    public void saveOrUpdate(WechatCircleEntity wechatCircle, List<CircleCommentEntity> un_save_cmts, DataSourcesFrom sourcesFrom) {
        if (CollectionUtils.isEmpty(un_save_cmts)) return;
        Preconditions.checkNotNull(wechatCircle);
        Optional<List<CircleCommentEntity>> exits_comments_opt = findByWechatCircle(wechatCircle);
        un_save_cmts.forEach(x -> x.buildCircle(wechatCircle));
        List<CircleCommentEntity> insert_list = Lists.newArrayList();
        List<CircleCommentEntity> upodate_list = Lists.newArrayList();

        if (exits_comments_opt.isPresent()) {
            if (logger.isDebugEnabled()) {
                logger.debug(String.format("[WAKKA]wechatCircle:%s", wechatCircle));
                logger.debug(String.format("[WAKKA]un_save_cmts:%s", un_save_cmts));
                logger.debug(String.format("[WAKKA]exits_comments_opt:%s", exits_comments_opt.get()));
            }
            un_save_cmts.forEach(cmt -> {
                Optional<CircleCommentEntity> exit_cmt = exits_comments_opt.get().stream().filter(x -> x.exitsById(cmt)).findFirst();
                if (exit_cmt.isPresent()) {
                    exit_cmt.get().mergeSource(sourcesFrom).ifPresent(upodate_list::add);
                } else {
                    insert_list.add(cmt);
                }
            });
        } else {
            insert_list.addAll(un_save_cmts);
        }
        if (CollectionUtils.isNotEmpty(insert_list)) super.batchInsert("batchInsert", insert_list);
        if (CollectionUtils.isNotEmpty(upodate_list)) updateSourcesFrom(upodate_list);
    }

    /**
     * Unread xinxin
     *
     * @param ownerWxId 微信
     * @return 你的推图
     */
    public Optional<List<CircleCommentEntity>> loadUnReadComments(String ownerWxId) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("readStatus", String.format("%s:0", ownerWxId));
        Optional<List<CircleCommentEntity>> cmts = super.queryForEntities("loadUnReadCmts", params, getRowMapper());
        if (logger.isDebugEnabled())
            logger.debug(String.format("loadUnReadComments(%s) return %s ", ownerWxId, cmts.map(List::size).orElse(0)));
        return cmts;
    }

    /**
     * @param comments 窃读的评论
     */
    public void addUnReadComments(List<CircleCommentEntity> comments) {
        if (CollectionUtils.isEmpty(comments)) return;
        List<Map<String, Object>> pks = comments.stream().map(CircleCommentEntity::getPk).collect(Collectors.toList());
        Optional<List<CircleCommentEntity>> exits_list_opt = findByIds(pks);
        List<CircleCommentEntity> add_list = Lists.newArrayList();
        if (exits_list_opt.isPresent()) {
            comments.forEach(cmt -> {
                Optional<CircleCommentEntity> exits = exits_list_opt.get().stream().filter(x -> x.exitsById(cmt)).findFirst();
                if (!exits.isPresent()) add_list.add(cmt);
            });
        } else {
            add_list.addAll(comments);
        }
        if (CollectionUtils.isNotEmpty(add_list)) {
            add_list.forEach(x -> {
                if (x.iscommentRef()) {
                    Optional<CircleCommentEntity> ref_opt = super.queryForEntity("find4Ref", x.toParamMap(), getRowMapper());
                    ref_opt.ifPresent(c -> x.setCommentRefId(c.getId()));
                }
            });
            super.batchInsert("batchInsert", add_list);
        }
    }

    private void updateSourcesFrom(Collection<CircleCommentEntity> circleComments) {
        super.batchUpdate("updateSourcesFrom", (ps, cmt) -> {
            Map<String, Object> params = WechatCircleEntityAction.circleSourcesParams(cmt.getSourcesFrom());
            ps.setObject(1, MapUtils.getString(params, "sourcesFrom"));
            ps.setObject(2, MapUtils.getString(params, "sourceWxIds"));
            ps.setObject(3, MapUtils.getString(params, "sourceComIds"));
            ps.setObject(4, MapUtils.getString(params, "sourceStoIds"));
            ps.setObject(5, cmt.getReadStatus().isPresent() ? StringUtils.join(cmt.getReadStatus().get().stream()
                    .map(CircleReadStatus::toString).collect(Collectors.toList()), ';') : null);
            ps.setObject(6, cmt.getId());
            ps.setObject(7, cmt.getWeixinId());
            ps.setObject(8, cmt.getCircleId());
        }, circleComments);
    }

    public void updateReadStatus(Collection<CircleCommentEntity> comments) {
        if (CollectionUtils.isEmpty(comments)) return;
        super.batchUpdate("updatReadStatus", (ps, cmt) -> {
            ps.setObject(1, cmt.getReadStatus().isPresent() ? Joiner.on(';').join(cmt.getReadStatus().get()) : null);
            ps.setObject(2, cmt.getId());
            ps.setObject(3, cmt.getWeixinId());
            ps.setObject(4, cmt.getCircleId());
        }, comments);
    }

    /**
     * 获取指定朋友圈的评论
     *
     * @param wechatCircle 谁的朋友圈
     * @return Optional
     */
    public Optional<List<CircleCommentEntity>> findByWechatCircle(WechatCircleEntity wechatCircle) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("weixinId", wechatCircle.getWeixinId());
        params.put("circleId", wechatCircle.getId());
        params.put("sql", "single");
        Optional<List<CircleCommentEntity>> comments = super.queryForEntities("findByCircle", params, getRowMapper());
        if (logger.isDebugEnabled())
            logger.debug(String.format("findByWechatCircle(weixinId:%s,circleId:%s) return size %s", wechatCircle.getWeixinId(),
                    wechatCircle.getId(),
                    comments.map(List::size).orElse(0)));
        return comments;
    }

    public static Optional<List<CircleCommentEntity>> splitByWechatCircle(WechatCircleEntity wechatCircle,
                                                                          Collection<CircleCommentEntity> comments) {
        if (CollectionUtils.isEmpty(comments)) return Optional.empty();
        List<CircleCommentEntity> sub_list = comments.stream().filter(x -> x.isOwnerCircle(wechatCircle))
                .collect(Collectors.toList());
        return Optional.ofNullable(CollectionUtils.isEmpty(sub_list) ? null : sub_list);
    }

    /**
     * 获取一批朋友的朋友圈 评论
     *
     * @param wechatCircles 谁的彭会有全
     * @param sourceWxId    我的温馨
     * @return Optional
     */
    public Optional<List<CircleCommentEntity>> findByWechatCircles(Collection<WechatCircleEntity> wechatCircles, String sourceWxId) {
        if (CollectionUtils.isEmpty(wechatCircles)) return Optional.empty();
        List<Map<String, Object>> circlesIds = wechatCircles.stream().map(WechatCircleEntity::toPkParams).collect(Collectors.toList());
        Map<String, Object> params = Maps.newHashMap();
        params.put("circlesIds", circlesIds);
        params.put("sourceWxId", sourceWxId);
        params.put("sql", "all");
        Optional<List<CircleCommentEntity>> comments = super.queryForEntities("findByCircle", params, getRowMapper());
        if (logger.isDebugEnabled())
            logger.debug(String.format("findByWechatCircles(params:%s) return size %s", circlesIds,
                    comments.map(List::size).orElse(0)));
        return comments;
    }

    Optional<List<CircleCommentEntity>> findByIds(Collection<Map<String, Object>> pks) {
        if (CollectionUtils.isEmpty(pks)) return Optional.empty();
        Map<String, Object> params = Maps.newHashMap();
        params.put("pks", pks);
        params.put("sql", "findByIds");
        return super.queryForEntities("findByIds", params, getRowMapper());
    }

    @Override
    protected RowMapper<CircleCommentEntity> getRowMapper() {
        return new RowMapperImpl();
    }

    class RowMapperImpl implements RowMapper<CircleCommentEntity> {
        @Override
        public CircleCommentEntity mapRow(ResultSet res, int rowNum) throws SQLException {
            return new CircleCommentEntity(res.getInt("id"), res);
        }
    }
}
