package com.legooframework.model.wechatcircle.entity;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
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

public class WechatCircleEntity extends BaseEntity<Long> implements BatchSetter {
    private final String weixinId;
    // from client
    private final String circleId;
    private final CircleType circleType;
    private final int imgNums;
    private final Long sendTime;
    private final String title, message, url, subUrl, description;
    private final List<WechatCircleImage> images;
    // 数据导入来源信息
    private Set<DataSourcesFrom> sourcesFrom;

    private List<CircleReadStatus> readStatus;

    List<CircleReadStatus> getReadStatus() {
        return readStatus;
    }

    public boolean isOwner(String weixinId) {
        return CollectionUtils.isNotEmpty(sourcesFrom) && sourcesFrom.stream()
                .anyMatch(x -> StringUtils.equals(weixinId, x.getWeixinId()));
    }

    @Override
    public Map<String, Object> toViewMap() {
        Map<String, Object> params = Maps.newHashMap();
        params.put("id", getId());
        params.put("strId", String.valueOf(getId()));
        params.put("weixinId", this.weixinId);
        params.put("circleType", circleType.getType());
        params.put("message", message);
        params.put("sendTime", sendTime);
        params.put("circleId", circleId);
        switch (circleType) {
            case TextCircle:
                break;
            case AudioCircle:
                params.put("title", title);
                params.put("desc", description);
                params.put("url", url);
                params.put("imgUrl", this.subUrl);
                break;
            case MixCircle:
                params.put("imgNums", imgNums);
                if (CollectionUtils.isNotEmpty(images)) {
                    params.put("imgs", images.stream().map(WechatCircleImage::toViewMap).collect(Collectors.toList()));
                }
                break;
            case VideoCircle:
                params.put("url", url);
                params.put("imgUrl", this.subUrl);
                break;
            case SoftArticleCircle:
                params.put("title", title);
                params.put("desc", description);
                params.put("url", url);
                params.put("imgUrl", this.subUrl);
                break;
            case UNKNOWN:
                break;
            default:
                break;
        }
        return params;
    }

    public boolean hasImages() {
        return CollectionUtils.isNotEmpty(images);
    }

    public boolean isMixCircle() {
        return CircleType.MixCircle == this.circleType;
    }

    Long getSendTime() {
        return sendTime;
    }

    @Override
    public Map<String, Object> toParamMap(String... excludes) {
        Map<String, Object> params = super.toParamMap("images", "circleType");
        params.put("weixinId", weixinId);
        params.put("circleId", circleId);
        params.put("circleType", circleType.getType());
        params.put("imgNums", imgNums);
        params.put("sendTime", sendTime);
        params.put("title", title);
        params.put("message", message);
        params.put("url", url);
        params.put("subUrl", subUrl);
        params.put("description", description);
        params.putAll(WechatCircleEntityAction.circleSourcesParams(this.sourcesFrom));
        if (CircleType.MixCircle == this.circleType && CollectionUtils.isEmpty(images)) {
            List<Map<String, Object>> img_params = images.stream().map(x -> x.toParamMap()).collect(Collectors.toList());
            params.put("images", img_params);
        }
        params.put("readStatus", CircleReadStatus.join(this.readStatus));
        return params;
    }

    @Override
    public void setValues(PreparedStatement ps) throws SQLException {
        //id, weixin_id, circle_id, circle_type, image_num, title, url,  sub_url, send_time,
        // message, sources_from, source_wx_ids, source_com_ids, source_sto_ids
        ps.setObject(1, this.getId());
        ps.setObject(2, this.weixinId);
        ps.setObject(3, this.circleId);
        ps.setObject(4, this.circleType.getType());
        ps.setObject(5, this.imgNums);
        ps.setObject(6, this.title);
        ps.setObject(7, this.url);
        ps.setObject(8, this.subUrl);
        ps.setObject(9, this.sendTime);
        ps.setObject(10, this.message);
        Map<String, Object> params = WechatCircleEntityAction.circleSourcesParams(this.sourcesFrom);
        ps.setObject(11, MapUtils.getString(params, "sourcesFrom"));
        ps.setObject(12, MapUtils.getString(params, "sourceWxIds"));
        ps.setObject(13, MapUtils.getString(params, "sourceComIds"));
        ps.setObject(14, MapUtils.getString(params, "sourceStoIds"));
        ps.setObject(15, this.description);
        ps.setObject(16, CircleReadStatus.join(this.readStatus));
    }

    Set<DataSourcesFrom> getSourcesFrom() {
        return sourcesFrom;
    }

    Map<String, Object> toPkParams() {
        Map<String, Object> params = Maps.newHashMap();
        params.put("id", getId());
        params.put("weixinId", weixinId);
        return params;
    }

    private WechatCircleEntity(Long id, String weixinId, CircleType circleType, String circleId,
                               String title, String message, String url, String subUrl, Long sendTime,
                               String description, DataSourcesFrom sourcesFrom) {
        super(id);
        Preconditions.checkNotNull(sendTime, "发送时间不可以为空值...");
        Preconditions.checkNotNull(sourcesFrom, "信息来源不可以为空值...");
        this.weixinId = weixinId;
        this.circleType = circleType;
        this.imgNums = 0;
        this.circleId = circleId;
        this.title = title;
        this.message = message;
        this.url = url;
        this.subUrl = subUrl;
        this.sendTime = sendTime;
        this.images = null;
        this.description = description;
        this.sourcesFrom = Sets.newHashSet(sourcesFrom);
        this.readStatus = Lists.newArrayList(CircleReadStatus.create(sourcesFrom.getWeixinId()));
    }

    private WechatCircleEntity(Long id, String weixinId, String circleId, String message, int imgNums,
                               List<WechatCircleImage> images, Long sendTime, DataSourcesFrom sourcesFrom) {
        super(id);
        Preconditions.checkNotNull(sendTime, "发送时间不可以为空值...");
        this.weixinId = weixinId;
        this.circleType = CircleType.MixCircle;
        this.imgNums = imgNums;
        this.circleId = circleId;
        this.title = null;
        this.message = message;
        this.sendTime = sendTime;
        this.url = null;
        this.subUrl = null;
        this.images = images;
        this.description = null;
        this.sourcesFrom = Sets.newHashSet(sourcesFrom);
        this.readStatus = Lists.newArrayList(CircleReadStatus.create(sourcesFrom.getWeixinId()));
    }

    WechatCircleEntity(Long id, ResultSet res) {
        super(id);
        try {
            Integer _circleType = ResultSetUtil.getObject(res, "circleType", Integer.class);
            this.circleType = CircleType.parse(_circleType);
            this.title = ResultSetUtil.getOptString(res, "title", null);
            this.subUrl = ResultSetUtil.getOptString(res, "subUrl", null);
            this.url = ResultSetUtil.getOptString(res, "url", null);
            this.message = ResultSetUtil.getOptString(res, "message", null);
            this.circleId = ResultSetUtil.getString(res, "circleId");
            this.imgNums = res.getInt("imageNum");
            this.sendTime = res.getLong("sendTime");
            this.description = res.getString("description");
            this.weixinId = ResultSetUtil.getString(res, "weixinId");
            String[] sourcesFrom_args = StringUtils.splitByWholeSeparator(ResultSetUtil.getString(res, "sourcesFrom"), "||");
            this.sourcesFrom = Sets.newHashSet();
            Stream.of(sourcesFrom_args).forEach(arg -> this.sourcesFrom.add(new DataSourcesFrom(arg)));
            String img_data = res.getString("imageData");
            if (CircleType.MixCircle == this.circleType && !Strings.isNullOrEmpty(img_data)) {
                String[] img_data_args = StringUtils.splitByWholeSeparator(img_data, "||||");
                this.images = Lists.newArrayList();
                int index = 0;
                for (String arg : img_data_args) {
                    String[] _temp = StringUtils.splitByWholeSeparator(arg, "||");
                    WechatCircleImage _img = new WechatCircleImage(_temp[0], _temp[1], _temp[2], index);
                    _img.setCircle(id, circleId);
                    this.images.add(_img);
                    index++;
                }
            } else {
                this.images = null;
            }
            // weixin_id:0;weixin_id:1
            String read_status = res.getString("readStatus");
            this.readStatus = CircleReadStatus.split(read_status);
        } catch (SQLException e) {
            throw new RuntimeException("Restore WechatCircleEntity has SQLException", e);
        }
    }

    CircleType getCircleType() {
        return circleType;
    }

    public String getCircleId() {
        return circleId;
    }

    public static WechatCircleEntity mixContent(String weixinId, Long id, String circleId, Long sendTime, int imgNums,
                                                String message, List<WechatCircleImage> images,
                                                DataSourcesFrom circleSource) {
        if (CollectionUtils.isNotEmpty(images)) {
            images.forEach(x -> x.setCircle(id, circleId));
        }
        return new WechatCircleEntity(id, weixinId, circleId, message, imgNums, images, sendTime, circleSource);
    }

    public static WechatCircleEntity textContent(String weixinId, Long id, String circleId, Long sendTime,
                                                 String message, DataSourcesFrom circleSource) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(message), "朋友圈文本内容不可以为空值...");
        return new WechatCircleEntity(id, weixinId, CircleType.TextCircle, circleId, null, message, null,
                null, sendTime, null, circleSource);
    }

    Optional<List<WechatCircleImage>> getImagesIfExits() {
        if (CircleType.MixCircle != this.circleType) return Optional.empty();
        return Optional.ofNullable(CollectionUtils.isEmpty(images) ? null : images);
    }

    public List<WechatCircleImage> getImages() {
        return CollectionUtils.isEmpty(images) ? null : ImmutableList.copyOf(images);
    }

    Optional<List<WechatCircleImage>> mergeImages(Collection<WechatCircleImage> images) {
        if (CollectionUtils.isEmpty(images)) return Optional.empty();
        images.forEach(img -> img.setCircle(this.getId(), this.circleId));
        if (CollectionUtils.isEmpty(this.images)) return Optional.of(Lists.newArrayList(images));
        List<WechatCircleImage> sub_list = Lists.newArrayList();
        images.forEach(img -> {
            Optional<WechatCircleImage> exits = this.images.stream().filter(x -> x.equals(img)).findFirst();
            if (exits.isPresent()) {
                if (exits.get().isMissing()) {
                    exits.get().setImgUrl(img.getImgUrl());
                    exits.get().setImgUrl(img.getImgUrl());
                    sub_list.add(exits.get());
                }
            } else {
                sub_list.add(img);
            }
        });
        return Optional.ofNullable(CollectionUtils.isEmpty(sub_list) ? null : sub_list);
    }

    public static WechatCircleEntity softArticleContent(String weixinId, Long id, String circleId, Long sendTime,
                                                        String title, String message, String softArticleUrl, String imgUrl,
                                                        String description, DataSourcesFrom circleSource) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(softArticleUrl), "软文链接地址不可以为空...");
        return new WechatCircleEntity(id, weixinId, CircleType.SoftArticleCircle, circleId, title, message,
                softArticleUrl, imgUrl, sendTime, description, circleSource);
    }


    public static WechatCircleEntity audioContent(String weixinId, Long id, String circleId, Long sendTime, String message, String title,
                                                  String audioDesc, String audioUrl, String imgUrl,
                                                  DataSourcesFrom dataSourcesFrom) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(audioUrl), "音频地址不可以空值");
        return new WechatCircleEntity(id, weixinId, CircleType.AudioCircle, circleId, title, message,
                audioUrl, imgUrl, sendTime, audioDesc, dataSourcesFrom);
    }

    public static WechatCircleEntity videoContent(String weixinId, Long id, String circleId, Long sendTime, String message, String videoUrl,
                                                  String imgUrl, DataSourcesFrom circleSource) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(videoUrl), "视频地址不可以空值");
        return new WechatCircleEntity(id, weixinId, CircleType.VideoCircle, circleId, null, message,
                videoUrl, imgUrl, sendTime, null, circleSource);
    }

    boolean equalsById(WechatCircleEntity that) {
        return this.getId().equals(that.getId()) && StringUtils.equals(this.weixinId, that.weixinId);
    }

    /**
     * 合并数据阿笠原
     *
     * @param source 数据来源之
     * @return Opt
     */
    Optional<WechatCircleEntity> mergeSource(DataSourcesFrom source) {
        if (this.sourcesFrom.contains(source)) return Optional.empty();
        WechatCircleEntity clone = (WechatCircleEntity) cloneMe();
        clone.sourcesFrom = this.sourcesFrom.stream().filter(x -> !StringUtils.equals(x.getWeixinId(), source.getWeixinId()))
                .collect(Collectors.toSet());
        clone.sourcesFrom.add(source);
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

    public String getWeixinId() {
        return weixinId;
    }

    public Optional<WechatCircleEntity> setReaded(String ownerId) {
        Optional<CircleReadStatus> readStatus = this.readStatus.stream().filter(x -> x.isWeixin(ownerId)).findFirst();
        if (readStatus.isPresent() && !readStatus.get().isRead()) {
            WechatCircleEntity clone = (WechatCircleEntity) cloneMe();
            clone.readStatus = Lists.newArrayList(this.readStatus);
            Optional<CircleReadStatus> opt = clone.readStatus.stream().filter(x -> x.isWeixin(ownerId)).findFirst();
            opt.ifPresent(CircleReadStatus::setReaded);
            return Optional.of(clone);
        }
        return Optional.empty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WechatCircleEntity that = (WechatCircleEntity) o;
        return imgNums == that.imgNums &&
                Objects.equal(getId(), that.getId()) &&
                Objects.equal(weixinId, that.weixinId) &&
                Objects.equal(circleId, that.circleId) &&
                circleType == that.circleType &&
                Objects.equal(sendTime, that.sendTime) &&
                Objects.equal(title, that.title) &&
                Objects.equal(message, that.message) &&
                Objects.equal(url, that.url) &&
                Objects.equal(subUrl, that.subUrl);
    }

    public Map<String, Object> toResultMap01() {
        Map<String, Object> param = Maps.newHashMap();
        param.put("id", this.getId());
        param.put("weixinId", this.weixinId);
        param.put("circleType", this.circleType.getType());
        param.put("imagesSize", CollectionUtils.isEmpty(images) ? 0 : images.size());
        param.put("imagesIds", CollectionUtils.isEmpty(images) ? null :
                StringUtils.join(images.stream().filter(WechatCircleImage::isComplete).map(BaseEntity::getId)
                        .collect(Collectors.toList()), ','));
        return param;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId(), weixinId, circleId, circleType, imgNums, sendTime,
                title, message, url, subUrl);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", getId())
                .add("weixinId", weixinId)
                .add("circleId", circleId)
                .add("circleType", circleType)
                .add("imgNums", imgNums)
                .add("sendTime", sendTime)
                .add("title", title)
                .add("message", message)
                .add("url", url)
                .add("subUrl", subUrl)
                .add("images", images)
                .add("description", description)
                .toString();
    }
}
