package com.csosm.module.webchat.entity;

import com.csosm.commons.adapter.LoginUserContext;
import com.csosm.commons.entity.BaseEntity;
import com.csosm.commons.util.MyWebUtil;
import com.csosm.commons.util.ReplaceWordsUtil;
import com.csosm.module.base.entity.StoreEntity;
import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.base.*;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.ArrayUtils;
import org.joda.time.DateTime;
import org.springframework.util.CollectionUtils;

import java.util.*;


public class SendMsgEntity extends BaseEntity<String> {

    private String msgTxt;
    private String[] imageInfo;
    private int weixinNum;
    private int msgNum = 0;
    private Long msgTempId;
    private Collection<String> weixinIds;
    private int msgType = 0;
    private Integer storeId, companyId;
    private List<String> deviceIds = Lists.newArrayList();

    SendMsgEntity(String id, String msgTxt, String[] imageInfo, int msgNum, int weixinNum, int msgType,
                  Collection<String> weixinIds, Integer storeId, Integer companyId,
                  Integer sendUserId, Date sendTime, Long msgTempId, String deviceIds) {
        super(id, sendUserId, sendTime);
        this.msgTxt = msgTxt;
        this.imageInfo = imageInfo;
        this.weixinNum = weixinNum;
        this.storeId = storeId;
        this.companyId = companyId;
        this.msgNum = msgNum;
        this.msgType = msgType;
        this.msgTempId = msgTempId == null ? -1L : msgTempId;
        this.weixinIds = weixinIds;
        this.deviceIds = Strings.isNullOrEmpty(deviceIds) ? null : Lists.newArrayList(deviceIds.split(","));
    }

    SendMsgEntity(Integer userId, String msgTxt, String[] imageInfo, Collection<String> weixinIds, StoreEntity store,
                  Long msgTempId) {
        super(UUID.randomUUID().toString(), userId, DateTime.now().toDate());
        Preconditions.checkNotNull(store);
        Preconditions.checkArgument(store.getCompanyId().isPresent());
        this.storeId = store.getId();
        this.msgTempId = msgTempId == null ? -1L : msgTempId;
        this.companyId = store.getCompanyId().get();
        if (Strings.isNullOrEmpty(msgTxt) && ArrayUtils.isEmpty(imageInfo))
            throw new IllegalArgumentException("发送消息不可以为空.");
        if (ArrayUtils.isNotEmpty(imageInfo)) {
            this.msgNum += imageInfo.length;
            this.imageInfo = imageInfo;
            this.msgType += 2;
        }

        if (!Strings.isNullOrEmpty(msgTxt)) {
            this.msgNum += 1;
            this.msgTxt = msgTxt;
            this.msgType += 1;
            Preconditions.checkArgument(this.msgTxt.length() < 2048, "微信文字信息长度最大为2048个字符。");
        }

        Preconditions.checkArgument(!CollectionUtils.isEmpty(weixinIds), "待发送的微信好友ID不可以为空.");
        this.weixinNum = weixinIds.size();
        this.weixinIds = weixinIds;
        this.deviceIds = store.getDeviceIds().orNull();
    }


    SendMsgEntity(LoginUserContext userContext, String msgTxt,
                  String[] imageInfo, Collection<String> weixinIds, StoreEntity store, Long msgTempId) {
        super(UUID.randomUUID().toString(), userContext.getUserId(), DateTime.now().toDate());
        Preconditions.checkNotNull(userContext);
        Preconditions.checkNotNull(store);
        Preconditions.checkArgument(userContext.getCompany().isPresent());
        this.storeId = store.getId();
        this.msgTempId = msgTempId == null ? -1L : msgTempId;
        this.companyId = userContext.getCompany().get().getId();
        if (Strings.isNullOrEmpty(msgTxt) && ArrayUtils.isEmpty(imageInfo))
            throw new IllegalArgumentException("发送消息不可以为空.");
        if (ArrayUtils.isNotEmpty(imageInfo)) {
            this.msgNum += imageInfo.length;
            this.imageInfo = imageInfo;
            this.msgType += 2;
        }
        if (SoftMsg4Weixin.isSoftInfo(msgTxt)) {
            this.msgNum += 1;
            this.msgType = 49;
            this.msgTxt = MyWebUtil.toJson(new SoftMsg4Weixin(msgTxt));
        } else if (!Strings.isNullOrEmpty(msgTxt)) {
            this.msgNum += 1;
            this.msgTxt = msgTxt;
            this.msgType += 1;
            Preconditions.checkArgument(this.msgTxt.length() < 2048, "微信文字信息长度最大为2048个字符。");
        }
        Preconditions.checkArgument(!CollectionUtils.isEmpty(weixinIds), "待发送的微信好友ID不可以为空.");
        this.weixinNum = weixinIds.size();
        this.weixinIds = weixinIds;
    }

    private Optional<WebChatUserEntity> getWeixin(Collection<WebChatUserEntity> weixins, String weixinId) {
        for (WebChatUserEntity weixin : weixins)
            if (weixin.getUserName().equals(weixinId)) return Optional.of(weixin);
        return Optional.absent();
    }

    public List<SendMsgDetailEntity> toMessageDetail(DevicesEntity device, LoginUserContext userContext,
                                                     Collection<WebChatUserEntity> weixins) {
        List<SendMsgDetailEntity> msgs = Lists.newArrayList();
        for (String weixinId : weixinIds) {
            Optional<WebChatUserEntity> weixinOpt = getWeixin(weixins, weixinId);
            if (!weixinOpt.isPresent()) continue;
            String uuId = UUID.randomUUID().toString();
            int order = 0;
            if (ArrayUtils.isNotEmpty(imageInfo)) {
                for (String image : imageInfo) {
                    msgs.add(SendMsgDetailEntity.createSendMsgDetailEntity4Img(image, weixinId, getId(),
                            device.getId(), storeId, companyId,
                            userContext, uuId, order++));
                }
            }
            if (!Strings.isNullOrEmpty(msgTxt)) {
                if (this.msgType != 49) {
                    String content = ReplaceWordsUtil.replace(msgTxt, userContext, weixinOpt.get());
                    msgs.add(SendMsgDetailEntity.createSendMsgDetailEntity4Txt(content, weixinId, getId(),
                            device.getId(), storeId, companyId,
                            userContext, uuId, order++));
                } else {
                    msgs.add(SendMsgDetailEntity.createSendMsgDetailEntity4SoftTxt(msgTxt, weixinId, getId(),
                            device.getId(), storeId, companyId,
                            userContext, uuId, order++));
                }
            }
        }
        return msgs;
    }

    public boolean canReplace() {
        if (Strings.isNullOrEmpty(msgTxt)) return false;
        if (this.isSoftTxt()) return false;
        return ReplaceWordsUtil.hasReplaceWord(msgTxt);
    }

    public long getMsgTempId() {
        return msgTempId;
    }

    public boolean isTxtOnly() {
        return 1 == this.msgType;
    }

    public boolean isImgOnly() {
        return 2 == this.msgType;
    }

    public boolean isMixOnly() {
        return 3 == this.msgType;
    }

    public boolean isSoftTxt() {
        return 49 == this.msgType;
    }

    public Optional<String> getMsgTxt() {
        return Optional.fromNullable(msgTxt);
    }

    public Optional<String[]> getImageUrl() {
        if (ArrayUtils.isEmpty(imageInfo)) return Optional.absent();
        return Optional.of(imageInfo);
    }

    public int getWeixinNum() {
        return weixinNum;
    }

    public Collection<String> getWeixinIds() {
        return weixinIds;
    }

    public Integer getStoreId() {
        return storeId;
    }

    public Integer getCompanyId() {
        return companyId;
    }

    @Override
    protected Map<String, Object> toMap() {
        Map<String, Object> param = super.toMap();
        param.put("weixinNum", weixinNum);
        param.put("msgType", msgType);
        param.put("msgTxt", msgTxt);
        param.put("msgNum", msgNum);
        param.put("msgTempId", msgTempId);
        if (ArrayUtils.isEmpty(imageInfo)) {
            param.put("imageUrl", null);
        } else {
            param.put("imageUrl", Joiner.on(',').join(imageInfo));
        }
        param.put("storeId", storeId);
        param.put("companyId", companyId);
        param.put("weixinIds", Joiner.on(',').join(weixinIds));
        return param;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SendMsgEntity)) return false;
        if (!super.equals(o)) return false;
        SendMsgEntity that = (SendMsgEntity) o;
        return weixinNum == that.weixinNum &&
                msgType == that.msgType &&
                Objects.equal(msgTxt, that.msgTxt) &&
                Objects.equal(companyId, that.companyId) &&
                Objects.equal(storeId, that.storeId) &&
                Objects.equal(weixinIds, that.weixinIds);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(super.hashCode(), msgTxt, companyId, storeId, weixinNum, weixinIds, msgType);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("weixinNum", weixinNum)
                .add("msgType", msgType)
                .add("msgTempId", msgTempId)
                .add("msgTxt", msgTxt)
                .add("msgNum", msgNum)
                .add("imageInfo", imageInfo)
                .add("storeId", storeId)
                .add("companyId", companyId)
                .add("weixinIds", weixinIds)
                .toString();
    }

}
