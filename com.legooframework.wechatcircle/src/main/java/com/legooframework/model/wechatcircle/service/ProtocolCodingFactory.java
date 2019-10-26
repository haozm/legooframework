package com.legooframework.model.wechatcircle.service;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.legooframework.model.wechatcircle.entity.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public abstract class ProtocolCodingFactory {

    private static final Logger logger = LoggerFactory.getLogger(ProtocolCodingFactory.class);

    /**
     * @param weixinId   属主微信
     * @param companyId  公司ID
     * @param storeId    门店ID
     * @param jsonString 朋友圈数据- 天啊 他是一个数组
     * @return List_ASZXD
     */
    public static List<WechatCircleTranDto> deCodingCircle(String weixinId, Integer companyId, Integer storeId, String jsonString) {
        List<WechatCircleTranDto> list = Lists.newArrayList();
        final DataSourcesFrom sourcesFrom = new DataSourcesFrom(weixinId, companyId, storeId);
        JsonParser parser = new JsonParser();
        JsonArray json_all_array = parser.parse(jsonString).getAsJsonArray();

        json_all_array.forEach(json_cricle -> {
            JsonObject json_cricle_obj = json_cricle.getAsJsonObject();
            WechatCircleEntity circleEntity = buildCircleEntity(json_cricle_obj, sourcesFrom);
            JsonArray comment_json = json_cricle_obj.get("snsVerbEntryList") == null || json_cricle_obj.get("snsVerbEntryList")
                    .isJsonNull() ? null : json_cricle_obj.get("snsVerbEntryList").getAsJsonArray();
            List<CircleCommentEntity> comments = Lists.newArrayList();
            if (comment_json != null) {
                comment_json.forEach(json -> comments.add(buildComment(json.getAsJsonObject(), circleEntity, sourcesFrom)));
            }
            WechatCircleTranDto _c = new WechatCircleTranDto(circleEntity, CollectionUtils.isEmpty(comments) ? null : comments,
                    sourcesFrom);
            if (logger.isDebugEnabled())
                logger.debug(String.format("builder WechatCircleTranDto content:%s", _c.toString()));
            list.add(_c);
        });
        if (logger.isDebugEnabled())
            logger.debug(String.format("本次共解析朋友圈%s 条", list.size()));
        return list;
    }

    /**
     * 外部解析朋友圈 评论  独立模式
     *
     * @param comments_json 母体
     * @param sourcesFrom   来源
     * @return 物种起源
     */
    static List<CircleCommentEntity> decodingUnReadComment(String comments_json, DataSourcesFrom sourcesFrom) {
        JsonParser parser = new JsonParser();
        JsonArray json_all_array = parser.parse(comments_json).getAsJsonArray();
        if (logger.isDebugEnabled())
            logger.debug(String.format("[WAKAKA]decodingComment(comments_json:%s)", json_all_array.toString()));
        List<CircleCommentEntity> comments = Lists.newArrayList();
        json_all_array.forEach(json -> {
            JsonObject comment_json = json.getAsJsonObject();
            int commentType_val = comment_json.get("ackType").getAsInt();
            long circleId = comment_json.get("field_snsId").getAsLong();
            int id = comment_json.get("oprId").getAsInt();
            String weixinId = comment_json.get("snsUserName").getAsString();
            String commentWxId = comment_json.get("oprUserName").getAsString();
            String commentWxName = comment_json.get("oprNikeName") == null || comment_json.get("oprNikeName").isJsonNull() ? null :
                    comment_json.get("oprNikeName").getAsString();
            long commentTime = comment_json.get("time").getAsInt();
            String message = comment_json.get("content") == null || comment_json.get("content").isJsonNull() ? null :
                    comment_json.get("content").getAsString();
            JsonObject snsRefEntity = comment_json.get("tSnsRefEntry") == null || comment_json.get("tSnsRefEntry").isJsonNull() ? null :
                    comment_json.get("tSnsRefEntry").getAsJsonObject();
            Integer commentRefId = null;
            String commentRefWxId = null;
            String commentRefMsg = null;
            if (snsRefEntity != null) {
                commentRefWxId = snsRefEntity.get("refUserName") == null || snsRefEntity.get("refUserName").isJsonNull() ? null :
                        snsRefEntity.get("refUserName").getAsString();
                commentRefMsg = snsRefEntity.get("refContent") == null || snsRefEntity.get("refContent").isJsonNull() ? null :
                        snsRefEntity.get("refContent").getAsString();
            }
            if (1 == commentType_val) {
                comments.add(new CircleCommentEntity(id, weixinId, circleId,
                        commentTime, commentWxId, commentWxName, false, sourcesFrom));
            } else if (2 == commentType_val) {
                CircleCommentEntity cmt = new CircleCommentEntity(id, weixinId, circleId,
                        commentTime, commentWxId, commentWxName, message, commentRefId, commentRefWxId, false, sourcesFrom);
                cmt.setCommentRef(commentRefMsg);
                comments.add(cmt);
            }
        });
        return comments;
    }

    /**
     * 整体解析朋友圈 OOXX
     *
     * @param comment_json 容器
     * @param circleEntity 来源
     * @param sourcesFrom  索命
     * @return 哈利波特
     */
    private static CircleCommentEntity buildComment(JsonObject comment_json, WechatCircleEntity circleEntity, DataSourcesFrom sourcesFrom) {
        int commentType_val = comment_json.get("ackType").getAsInt();
        if (logger.isDebugEnabled())
            logger.debug(String.format("[WAKAKA]decodingComment(comments_json:%s)", comment_json.toString()));
        int id = comment_json.get("oprId").getAsInt();
        String commentWxId = comment_json.get("oprUserName").getAsString();
        String commentWxName = comment_json.get("oprNikeName") == null || comment_json.get("oprNikeName").isJsonNull() ? null :
                comment_json.get("oprNikeName").getAsString();
        long commentTime = comment_json.get("time").getAsInt();
        String message = comment_json.get("content") == null || comment_json.get("content").isJsonNull() ? null :
                comment_json.get("content").getAsString();
        Integer commentRefId = comment_json.get("bAckId") == null || comment_json.get("bAckId").isJsonNull() ? null :
                comment_json.get("bAckId").getAsInt();
        String commentRefWxId = comment_json.get("bAckUserName") == null || comment_json.get("bAckUserName").isJsonNull() ? null :
                comment_json.get("bAckUserName").getAsString();
        if (1 == commentType_val) {
            return new CircleCommentEntity(id, circleEntity.getWeixinId(), circleEntity.getId(),
                    commentTime, commentWxId, commentWxName, true, sourcesFrom);
        } else if (2 == commentType_val) {
            return new CircleCommentEntity(id, circleEntity.getWeixinId(), circleEntity.getId(),
                    commentTime, commentWxId, commentWxName, message, commentRefId, commentRefWxId, true, sourcesFrom);
        } else {
            throw new IllegalArgumentException(String.format("尚未支持的评论类型：%s", commentType_val));
        }
    }

    private static WechatCircleEntity buildCircleEntity(JsonObject cricle_json, DataSourcesFrom sourcesFrom) {
        int circleType_val = cricle_json.get("snsType").getAsInt();
        CircleType circleType = CircleType.parse(circleType_val);
        String circleId = cricle_json.get("snsId").getAsString();
        Long id = cricle_json.get("field_snsId").getAsLong();
        Long sendTime = cricle_json.get("snsTime").getAsLong();
        String wxId = cricle_json.get("snsUserName").getAsString();
        String message = cricle_json.get("snsMessage") == null || cricle_json.get("snsMessage").isJsonNull() ? null :
                cricle_json.get("snsMessage").getAsString();
        WechatCircleEntity circleEntity;
        switch (circleType) {
            case TextCircle:
                circleEntity = textContent(wxId, id, circleId, sendTime, message, sourcesFrom);
                break;
            case MixCircle:
                circleEntity = mixContent(wxId, id, circleId, sendTime, message, cricle_json, sourcesFrom);
                break;
            case AudioCircle:
                circleEntity = audioContent(wxId, id, circleId, sendTime, message, cricle_json, sourcesFrom);
                break;
            case VideoCircle:
                circleEntity = videoContent(wxId, id, circleId, sendTime, message, cricle_json, sourcesFrom);
                break;
            case SoftArticleCircle:
                circleEntity = softArticleContent(wxId, id, circleId, sendTime, message, cricle_json, sourcesFrom);
                break;
            default:
                throw new IllegalArgumentException("不支持的朋友圈类型....");
        }
        return circleEntity;
    }

    private static WechatCircleEntity textContent(String wxId, Long id, String circleId, Long sendTime,
                                                  String message, DataSourcesFrom sourcesFrom) {
        return WechatCircleEntity.textContent(wxId, id, circleId, sendTime, message, sourcesFrom);
    }

    private static WechatCircleEntity mixContent(String wxId, Long id, String circleId, Long sendTime,
                                                 String message, JsonObject cricle_json, DataSourcesFrom sourcesFrom) {
        int imageCount = (cricle_json.get("imageCount") == null || cricle_json.get("imageCount").isJsonNull()) ?
                0 : cricle_json.get("imageCount").getAsInt();
        JsonArray images_json = (cricle_json.get("imageEntryList") == null || cricle_json.get("imageEntryList").isJsonNull()) ?
                null : cricle_json.get("imageEntryList").getAsJsonArray();
        List<WechatCircleImage> images = Lists.newArrayListWithCapacity(6);
        if (images_json != null) {
            for (JsonElement jsonElement : images_json) {
                deCodingImage(jsonElement.getAsJsonObject()).ifPresent(images::add);
            }
        }
        return WechatCircleEntity.mixContent(wxId, id, circleId, sendTime, imageCount, message, images, sourcesFrom);
    }

    public static Optional<WechatCircleImage> deCodingImage(JsonObject img_json) {
        String img_id = img_json.get("Id").getAsString();
        String img_url = img_json.get("url") == null || img_json.get("url").isJsonNull() ? null :
                img_json.get("url").getAsString();
        String img_smallUrl = img_json.get("smallUrl") == null || img_json.get("smallUrl").isJsonNull() ? null :
                img_json.get("smallUrl").getAsString();
        if (StringUtils.isEmpty(img_url) && StringUtils.isEmpty(img_smallUrl)) return Optional.empty();
        return Optional.of(new WechatCircleImage(img_id, img_url, img_smallUrl, 0));
    }

    private static WechatCircleEntity softArticleContent(String wxId, Long id, String circleId, Long sendTime,
                                                         String message, JsonObject cricle_json,
                                                         DataSourcesFrom sourcesFrom) {
        JsonObject softArticle_json = cricle_json.getAsJsonObject("linksEntryList");
        Preconditions.checkState(softArticle_json != null && !softArticle_json.isJsonNull(), "上传数据缺失软文信息....");
        String softArticle_id = softArticle_json.get("Id").getAsString();
        String title = softArticle_json.get("title").getAsString();
        String desc = (softArticle_json.get("desc") == null || softArticle_json.get("desc").isJsonNull()) ?
                null : softArticle_json.get("desc").getAsString();
        String url = softArticle_json.get("url").getAsString();
        String imgUrl = (softArticle_json.get("imgUrl") == null || softArticle_json.get("imgUrl").isJsonNull()) ?
                null : softArticle_json.get("imgUrl").getAsString();
        return WechatCircleEntity.softArticleContent(wxId, id, circleId, sendTime, title, message, url, imgUrl,
                desc, sourcesFrom);
    }

    private static WechatCircleEntity audioContent(String wxId, Long id, String circleId, Long sendTime, String message,
                                                   JsonObject cricle_json, DataSourcesFrom sourcesFrom) {
        JsonObject softArticle_json = cricle_json.getAsJsonObject("snsAndioEntry");
        Preconditions.checkState(softArticle_json != null && !softArticle_json.isJsonNull(), "上传数据缺失音频信息....");
        String softArticle_id = softArticle_json.get("Id").getAsString();
        String title = softArticle_json.get("title").getAsString();
        String desc = (softArticle_json.get("desc") == null || softArticle_json.get("desc").isJsonNull()) ?
                null : softArticle_json.get("desc").getAsString();
        String url = softArticle_json.get("url").getAsString();
        String imgUrl = (softArticle_json.get("imgUrl") == null || softArticle_json.get("imgUrl").isJsonNull()) ?
                null : softArticle_json.get("imgUrl").getAsString();
        return WechatCircleEntity.audioContent(wxId, id, circleId, sendTime, title, message, url, imgUrl,
                desc, sourcesFrom);
    }

    private static WechatCircleEntity videoContent(String wxId, Long id, String circleId, Long sendTime, String message,
                                                   JsonObject cricle_json, DataSourcesFrom sourcesFrom) {
        JsonObject softArticle_json = cricle_json.getAsJsonObject("videoEntryList");
        Preconditions.checkState(softArticle_json != null && !softArticle_json.isJsonNull(), "上传数据缺失视频信息....");
        String softArticle_id = softArticle_json.get("Id").getAsString();
        String url = softArticle_json.get("url").getAsString();
        String imgUrl = (softArticle_json.get("imgUrl") == null || softArticle_json.get("imgUrl").isJsonNull()) ?
                null : softArticle_json.get("imgUrl").getAsString();
        return WechatCircleEntity.videoContent(wxId, id, circleId, sendTime, message, url, imgUrl, sourcesFrom);
    }
}
