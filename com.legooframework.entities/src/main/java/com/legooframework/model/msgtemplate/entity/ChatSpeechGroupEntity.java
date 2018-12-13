package com.legooframework.model.msgtemplate.entity;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.legooframework.model.core.base.entity.BaseEntity;
import com.legooframework.model.core.base.runtime.LoginContext;
import com.legooframework.model.core.jdbc.ResultSetUtil;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;

public class ChatSpeechGroupEntity extends BaseEntity<Long> {

    private String groupName;
    private int groupOrder;
    private Long storeId;

    ChatSpeechGroupEntity(Long id, String groupName, int groupOrder, LoginContext loginContext) {
        super(id, loginContext.getTenantId(), loginContext.getLoginId());
        this.groupName = groupName;
        this.groupOrder = groupOrder;
        this.storeId = null;
    }

    ChatSpeechGroupEntity(Long id, ResultSet res) {
        super(id, res);
        try {
            this.groupName = ResultSetUtil.getString(res, "groupName");
            this.groupOrder = res.getInt("groupOrder");
            this.storeId = ResultSetUtil.getOptObject(res, "storeId", Long.class).orElse(null);
        } catch (SQLException e) {
            throw new RuntimeException("Restore ChatSpeechGroupEntity has SQLException", e);
        }
    }

    Optional<ChatSpeechGroupEntity> changeName(String groupName) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(groupName), "分组名称不可以为空");
        if (StringUtils.equals(this.getGroupName(), groupName)) return Optional.empty();
        ChatSpeechGroupEntity clone = (ChatSpeechGroupEntity) super.cloneMe();
        clone.groupName = groupName;
        return Optional.of(clone);
    }

    @Override
    public Map<String, Object> toParamMap(String... excludes) {
        return super.toParamMap("speeches");
    }

    public String getGroupName() {
        return groupName;
    }

    public Long getStoreId() {
        return storeId;
    }

    public int getGroupOrder() {
        return groupOrder;
    }

    Speech buildSpeech(Map<String, Object> data) {
        return new Speech(data);
    }

    public Speech addSpeech(String content) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(content), "话术内容不可以为空....");
        return new Speech(this, content, 1, 100);
    }

    class Speech implements Cloneable {
        private int id;
        private final Long groupId;
        private String content;
        private int type;
        private int order;
        private boolean enabled;

        Speech(Map<String, Object> data) {
            this.id = MapUtils.getIntValue(data, "id");
            this.content = MapUtils.getString(data, "content");
            this.groupId = MapUtils.getLong(data, "groupId");
            this.type = MapUtils.getIntValue(data, "contentType");
            this.order = MapUtils.getIntValue(data, "speechOrder");
            this.enabled = MapUtils.getIntValue(data, "enabledFlag") == 1;
        }

        Speech(ChatSpeechGroupEntity owner, String content, int type, int order) {
            this.groupId = owner.getId();
            this.content = content;
            this.order = order;
            this.type = type;
            this.enabled = true;
        }

        public Map<String, Object> toMap() {
            Map<String, Object> params = Maps.newHashMap();
            params.put("content", content);
            params.put("id", id);
            params.put("type", type);
            params.put("order", order);
            params.put("groupId", groupId);
            params.put("enabled", enabled ? 1 : 0);
            return params;
        }

        public Optional<Speech> enabled() {
            if (this.isEnabled()) return Optional.empty();
            try {
                Speech speech = (Speech) this.clone();
                speech.enabled = true;
                return Optional.of(speech);
            } catch (CloneNotSupportedException e) {
                throw new RuntimeException("CloneNotSupportedException for Speech ", e);
            }
        }

        public Optional<Speech> disbaled() {
            if (!this.isEnabled()) return Optional.empty();
            try {
                Speech speech = (Speech) this.clone();
                speech.enabled = false;
                return Optional.of(speech);
            } catch (CloneNotSupportedException e) {
                throw new RuntimeException("CloneNotSupportedException for Speech ", e);
            }
        }

        public Optional<Speech> rename(String content) {
            Preconditions.checkArgument(!Strings.isNullOrEmpty(content), "话术内容不可以为空...");
            if (StringUtils.equals(this.getContent(), content)) return Optional.empty();
            try {
                Speech speech = (Speech) this.clone();
                speech.content = content;
                return Optional.of(speech);
            } catch (CloneNotSupportedException e) {
                throw new RuntimeException("CloneNotSupportedException for Speech ", e);
            }
        }

        public int getOrder() {
            return order;
        }

        public Long getGroupId() {
            return groupId;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public int getId() {
            return id;
        }

        public String getContent() {
            return content;
        }

        public int getType() {
            return type;
        }

        boolean equalSpeech(String content) {
            return StringUtils.equals(this.getContent(), content);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Speech)) return false;
            Speech speech = (Speech) o;
            return type == speech.type &&
                    order == speech.order &&
                    enabled == speech.enabled &&
                    Objects.equal(content, speech.content);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(content, type, enabled, order);
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("id", id)
                    .add("order", order)
                    .add("type", type)
                    .add("enabled", enabled)
                    .add("content", content)
                    .toString();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChatSpeechGroupEntity)) return false;
        if (!super.equals(o)) return false;
        ChatSpeechGroupEntity that = (ChatSpeechGroupEntity) o;
        return groupOrder == that.groupOrder &&
                Objects.equal(groupName, that.groupName);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(super.hashCode(), groupName, groupOrder);
    }
}
