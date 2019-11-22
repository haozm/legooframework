package com.legooframework.model.covariant.entity;

import com.google.common.base.Joiner;
import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.legooframework.model.core.base.entity.BaseEntity;
import com.legooframework.model.core.jdbc.BatchSetter;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.util.CollectionUtils;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SendWechatEntity extends BaseEntity<String> implements BatchSetter {

    private String msgTxt;
    private String[] imageInfo;
    private int weixinNum;
    private final BusinessType businessType;
    private int msgNum = 0;
    private Long msgTempId;
    private Collection<String> weixinIds;
    private int msgType = 0;
    private Integer storeId, companyId;
    private String deviceId;

    @Override
    public void setValues(PreparedStatement ps) throws SQLException {
        // id, msg_type, business_type, msg_num, image_url, weixin_ids,
        ps.setObject(1, getId());
        ps.setObject(2, msgType);
        ps.setObject(3, businessType.getValue());
        ps.setObject(4, msgNum);
        if (ArrayUtils.isEmpty(imageInfo)) {
            ps.setObject(5, null);
        } else {
            ps.setObject(5, Joiner.on(',').join(imageInfo));
        }
        ps.setObject(6, Joiner.on(',').join(weixinIds));
        // msg_text, store_id, weixin_num, company_id, msg_temp_id, createUserId,
        ps.setObject(7, msgTxt);
        ps.setObject(8, storeId);
        ps.setObject(9, weixinNum);
        ps.setObject(10, companyId);
        ps.setObject(11, msgTempId);
        ps.setObject(12, 0);
    }

    public SendWechatEntity(String msgTxt, String[] imageInfo, String weixinId, Integer storeId, Integer companyId, String deviceId,
                            BusinessType businessType) {
        super(UUID.randomUUID().toString());
        this.storeId = storeId;
        this.msgTempId = -1L;
        this.companyId = companyId;
        this.businessType = businessType == null ? BusinessType.CUSTOM_CARE : businessType;
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
        this.weixinNum = 1;
        this.weixinIds = Lists.newArrayList(weixinId);
        this.deviceId = deviceId;
    }

    SendWechatEntity(String msgTxt, String[] imageInfo, Collection<String> weixinIds, StoEntity store, BusinessType businessType) {
        super(UUID.randomUUID().toString());
        this.storeId = store.getId();
        this.msgTempId = -1L;
        this.companyId = store.getCompanyId();
        this.businessType = businessType == null ? BusinessType.CUSTOM_CARE : businessType;
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
        this.deviceId = store.getDeviceId().orElse(null);
    }

    public List<SendWechatDetailEntity> toMessageDetail() {
        List<SendWechatDetailEntity> details = Lists.newArrayList();
        for (String $it : weixinIds) {
            String batchno = UUID.randomUUID().toString();
            int order = 0;
            if (ArrayUtils.isNotEmpty(imageInfo)) {
                for (String image : imageInfo) {
                    details.add(SendWechatDetailEntity.createWechatMsg4Img(image, $it, getId(),
                            deviceId, storeId, companyId, batchno, order++));
                }
            }
            if (!Strings.isNullOrEmpty(msgTxt)) {
                if (this.msgType != 49) {
                    details.add(SendWechatDetailEntity.createWechatMsg4Txt(msgTxt, $it, getId(),
                            deviceId, storeId, companyId, batchno, order++));
                } else {
                    details.add(SendWechatDetailEntity.createWechatMsg4SoftTxt(msgTxt, $it, getId(),
                            deviceId, storeId, companyId, batchno, order++));
                }
            }
        }
        return details;
    }

    @Override
    public Map<String, Object> toParamMap(String... excludes) {
        Map<String, Object> param = Maps.newHashMap();
        param.put("id", getId());
        param.put("msgType", msgType);
        param.put("businessType", businessType == null ? BusinessType.CUSTOM_CARE.getValue() : businessType.getValue());
        param.put("msgNum", msgNum);
        if (ArrayUtils.isEmpty(imageInfo)) {
            param.put("imageUrl", null);
        } else {
            param.put("imageUrl", Joiner.on(',').join(imageInfo));
        }
        param.put("weixinIds", Joiner.on(',').join(weixinIds));
        param.put("msgTxt", msgTxt);
        param.put("storeId", storeId);
        param.put("weixinNum", weixinNum);
        param.put("msgTempId", msgTempId);
        param.put("companyId", companyId);
        param.put("createUserId", 0);
        return param;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("msgTxt", msgTxt)
                .add("imageInfo", imageInfo != null)
                .add("weixinNum", weixinNum)
                .add("businessType", businessType)
                .add("msgNum", msgNum)
                .add("msgTempId", msgTempId)
                .add("weixinIds", weixinIds.size())
                .add("msgType", msgType)
                .add("storeId", storeId)
                .add("companyId", companyId)
                .toString();
    }
}
