package com.legooframework.model.wechatcircle.entity;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.collect.*;
import com.legooframework.model.core.base.entity.BaseEntity;
import com.legooframework.model.core.jdbc.BatchSetter;
import com.legooframework.model.core.jdbc.ResultSetUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CircleCommentEntity extends BaseEntity<Integer> implements BatchSetter {

    private long circleId;
    private String weixinId, message;
    private final CircleCommentType commentType;
    private final Long commentTime;
    private Integer commentRefId;
    private final String commentWxId, commentWxName, commentRefWxId;
    private final boolean readingMark;
    // 数据导入来源信息
    private Set<DataSourcesFrom> sourcesFrom;
    private List<CircleReadStatus> readStatus;
    // 临时变量
    private String commentRefMsg;

    public boolean isOwner(String weixinId) {
        return CollectionUtils.isNotEmpty(sourcesFrom) && sourcesFrom.stream()
                .anyMatch(x -> StringUtils.equals(weixinId, x.getWeixinId()));
    }

    void setCommentRefId(Integer commentRefId) {
        this.commentRefId = commentRefId;
    }

    public void setCommentRef(String commentRefMsg) {
        this.commentRefMsg = commentRefMsg;
    }

    boolean iscommentRef() {
        return StringUtils.isNotEmpty(commentRefWxId) || commentRefId != null;
    }

    @Override
    public Map<String, Object> toParamMap(String... excludes) {
        Map<String, Object> params = super.toParamMap(excludes);
        params.put("id", getId());
        params.put("circleId", this.circleId);
        params.put("weixinId", this.weixinId);
        params.put("commentType", this.commentType.getType());
        params.put("commentRefWxId", commentRefWxId);
        params.put("commentWxId", commentWxId);
        params.put("message", message);
        params.put("commentRefMsg", commentRefMsg);
        return params;
    }

    Optional<List<CircleReadStatus>> getReadStatus() {
        return Optional.ofNullable(CollectionUtils.isEmpty(readStatus) ? null : readStatus);
    }

    public Optional<CircleCommentEntity> setReaded(String ownerId) {
        Optional<CircleReadStatus> readStatus = this.readStatus.stream().filter(x -> x.isWeixin(ownerId)).findFirst();
        if (readStatus.isPresent() && !readStatus.get().isRead()) {
            CircleCommentEntity clone = (CircleCommentEntity) cloneMe();
            clone.readStatus = Lists.newArrayList(this.readStatus);
            Optional<CircleReadStatus> opt = clone.readStatus.stream().filter(x -> x.isWeixin(ownerId)).findFirst();
            opt.ifPresent(CircleReadStatus::setReaded);
            return Optional.of(clone);
        }
        return Optional.empty();
    }

    void buildCircle(WechatCircleEntity wechatCircle) {
        this.weixinId = wechatCircle.getWeixinId();
        this.circleId = wechatCircle.getId();
    }

    public Collection<String> getWeixinIds() {
        Set<String> _ids = Sets.newHashSet();
        _ids.add(this.weixinId);
        _ids.add(this.commentWxId);
        if (StringUtils.isNotEmpty(this.commentRefWxId))
            _ids.add(this.commentRefWxId);
        return ImmutableSet.copyOf(_ids);
    }

    public String getCommentWxId() {
        return commentWxId;
    }

    public String getCommentWxName() {
        return commentWxName;
    }

    public boolean isLiked() {
        return CircleCommentType.Liked == this.commentType;
    }

    @Override
    public Map<String, Object> toViewMap() {
        Map<String, Object> params = Maps.newHashMap();
        params.put("id", getId());
        params.put("commentType", commentType.getType());
        params.put("commentWxId", commentWxId);
        params.put("commentWxName", commentWxName);
        params.put("commentTime", commentTime);
        params.put("message", message);
        if (StringUtils.isNotEmpty(commentRefWxId)) {
            params.put("commentRefId", commentRefId);
            params.put("commentRefWxId", commentRefWxId);
        }
        return params;
    }

    Set<DataSourcesFrom> getSourcesFrom() {
        return sourcesFrom;
    }

    public long getCircleId() {
        return circleId;
    }

    String getWeixinId() {
        return weixinId;
    }

    public boolean isOwnerCircle(WechatCircleEntity wechatCircle) {
        return StringUtils.equals(this.weixinId, wechatCircle.getWeixinId())
                && this.circleId == wechatCircle.getId();
    }

    Map<String, Object> getPk() {
        Map<String, Object> params = Maps.newHashMap();
        params.put("id", getId());
        params.put("weixinId", weixinId);
        params.put("circleId", circleId);
        params.put("commentType", commentType.getType());
        params.put("commentWxId", commentWxId);
        return params;
    }

    @Override
    public void setValues(PreparedStatement ps) throws SQLException {
        // id, circle_id, weixin_id, comment_type, comment_wx_id, comment_wx_name, comment_ref_id,
        // comment_ref_wx_id, message, comment_time, sources_from, source_wx_ids, source_com_ids, source_sto_ids
        ps.setObject(1, getId());
        ps.setObject(2, this.circleId);
        ps.setObject(3, this.weixinId);
        ps.setObject(4, this.commentType.getType());
        ps.setObject(5, this.commentWxId);
        ps.setObject(6, this.commentWxName);
        ps.setObject(7, this.commentRefId);
        ps.setObject(8, this.commentRefWxId);
        ps.setObject(9, this.message);
        ps.setObject(10, this.commentTime);
        Map<String, Object> params = WechatCircleEntityAction.circleSourcesParams(sourcesFrom);
        ps.setObject(11, MapUtils.getString(params, "sourcesFrom"));
        ps.setObject(12, MapUtils.getString(params, "sourceWxIds"));
        ps.setObject(13, MapUtils.getString(params, "sourceComIds"));
        ps.setObject(14, MapUtils.getString(params, "sourceStoIds"));
        ps.setObject(15, readingMark ? 1 : 0);
        ps.setObject(16, CircleReadStatus.join(this.readStatus));
    }

    // 点赞
    public CircleCommentEntity(Integer id, String weixinId, long circleId, Long commentTime, String commentWxId, String commentWxName,
                               boolean isReading, DataSourcesFrom sourceFrom) {
        super(id);
        this.circleId = circleId;
        this.weixinId = weixinId;
        this.readingMark = isReading;
        this.commentType = CircleCommentType.Liked;
        this.commentTime = commentTime;
        this.message = null;
        this.commentRefId = null;
        this.commentWxId = commentWxId;
        this.commentWxName = commentWxName;
        this.commentRefWxId = null;
        this.sourcesFrom = Sets.newHashSet(sourceFrom);
        this.readStatus = Lists.newArrayList(CircleReadStatus.create(sourceFrom.getWeixinId()));
    }

    // 评论
    public CircleCommentEntity(Integer id, String weixinId, long circleId, Long commentTime, String commentWxId,
                               String commentWxName, String message, Integer commentRefId, String commentRefWxId,
                               boolean isReading, DataSourcesFrom sourceFrom) {
        super(id);
        this.circleId = circleId;
        this.weixinId = weixinId;
        this.commentType = CircleCommentType.Comment;
        this.commentTime = commentTime;
        this.message = message;
        this.readingMark = isReading;
        this.commentWxId = commentWxId;
        this.commentWxName = commentWxName;
        this.commentRefId = commentRefId;
        this.commentRefWxId = commentRefWxId;
        this.sourcesFrom = Sets.newHashSet(sourceFrom);
        this.readStatus = Lists.newArrayList(CircleReadStatus.create(sourceFrom.getWeixinId()));
    }

    CircleCommentEntity(Integer id, ResultSet res) {
        super(id);
        try {
            Integer _commentType = ResultSetUtil.getObject(res, "commentType", Integer.class);
            this.commentType = CircleCommentType.parse(_commentType);
            this.commentWxId = ResultSetUtil.getString(res, "commentWxId");
            this.commentWxName = res.getString("commentWxName");
            this.commentTime = res.getLong("commentTime");
            this.weixinId = ResultSetUtil.getString(res, "weixinId");
            this.circleId = res.getLong("circleId");
            this.readingMark = ResultSetUtil.getBooleanByInt(res, "readingMark");
            if (CircleCommentType.Comment == this.commentType) {
                this.message = res.getString("message");
                this.commentRefId = res.getObject("commentRefId") == null ? null :
                        res.getInt("commentRefId");
                this.commentRefWxId = res.getObject("commentRefWxId") == null ? null :
                        res.getString("commentRefWxId");
            } else {
                this.message = null;
                this.commentRefId = null;
                this.commentRefWxId = null;
            }
            String[] sourcesFrom_args = StringUtils.splitByWholeSeparator(ResultSetUtil.getString(res, "sourcesFrom"), "||");
            this.sourcesFrom = Sets.newHashSet();
            Stream.of(sourcesFrom_args).forEach(arg -> this.sourcesFrom.add(new DataSourcesFrom(arg)));
            // weixin_id:0;weixin_id:1
            String read_status = res.getString("readStatus");
            this.readStatus = CircleReadStatus.split(read_status);
        } catch (SQLException e) {
            throw new RuntimeException("Restore CircleCommentEntity has SQLException", e);
        }
    }

    boolean exitsById(CircleCommentEntity that) {
        return Objects.equal(this.weixinId, that.weixinId) && Objects.equal(this.getId(), that.getId())
                && Objects.equal(this.commentWxId, that.commentWxId)
                && Objects.equal(this.circleId, that.circleId) && this.commentType == that.commentType;
    }

    /**
     * 合并数据阿笠原
     *
     * @param source 数据来源之
     * @return Opt
     */
    Optional<CircleCommentEntity> mergeSource(DataSourcesFrom source) {
        if (this.sourcesFrom.contains(source)) return Optional.empty();
        CircleCommentEntity clone = (CircleCommentEntity) cloneMe();
        Set<DataSourcesFrom> _temp = this.sourcesFrom.stream().filter(x -> !StringUtils.equals(x.getWeixinId(), source.getWeixinId()))
                .collect(Collectors.toSet());
        _temp.add(source);
        clone.sourcesFrom = _temp;
        clone.readStatus = CollectionUtils.isEmpty(this.readStatus) ? Lists.newArrayList() : Lists.newArrayList(this.readStatus);
        if (CollectionUtils.isNotEmpty(clone.readStatus)) {
            Optional<?> exits = clone.readStatus.stream().filter(x -> StringUtils.equals(x.getWeixinId(), source.getWeixinId()))
                    .findAny();
            if (!exits.isPresent()) {
                clone.readStatus.add(CircleReadStatus.create(source.getWeixinId()));
            }
        }
        return Optional.of(clone);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CircleCommentEntity that = (CircleCommentEntity) o;
        return Objects.equal(getId(), that.getId()) &&
                Objects.equal(circleId, that.circleId) &&
                Objects.equal(weixinId, that.weixinId) &&
                Objects.equal(message, that.message) &&
                commentType == that.commentType &&
                readingMark == that.readingMark &&
                Objects.equal(commentTime, that.commentTime) &&
                Objects.equal(commentRefId, that.commentRefId) &&
                Objects.equal(commentWxId, that.commentWxId) &&
                Objects.equal(commentWxName, that.commentWxName) &&
                Objects.equal(commentRefWxId, that.commentRefWxId) &&
                Objects.equal(sourcesFrom, that.sourcesFrom);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId(), circleId, weixinId, message, commentType, commentTime, commentRefId,
                commentWxId, commentWxName, commentRefWxId, readingMark, sourcesFrom);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", getId())
                .add("circleId", circleId)
                .add("weixinId", weixinId)
                .add("message", message)
                .add("commentType", commentType)
                .add("commentTime", commentTime)
                .add("commentRefId", commentRefId)
                .add("commentWxId", commentWxId)
                .add("commentWxName", commentWxName)
                .add("commentRefWxId", commentRefWxId)
                .add("readingMark", readingMark)
                .add("sourcesFrom", sourcesFrom)
                .toString();
    }
}
