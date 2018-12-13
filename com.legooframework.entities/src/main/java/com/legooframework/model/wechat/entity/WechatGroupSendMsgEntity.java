package com.legooframework.model.wechat.entity;

import com.google.common.base.Joiner;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.legooframework.model.core.base.entity.BaseEntity;
import com.legooframework.model.core.base.entity.Sorting;
import com.legooframework.model.core.base.runtime.LoginContext;
import com.legooframework.model.core.jdbc.BatchSetter;
import com.legooframework.model.organization.entity.StoreEntity;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class WechatGroupSendMsgEntity extends BaseEntity<Long> {

	private Set<String> userNames;
	private int numOfUsername;
	private int numOfContent;
	private List<ChatContent> chatContents;
	private Long storeId;

	WechatGroupSendMsgEntity(Long id, List<Map<String, Object>> chatContents, Set<String> userNames, StoreEntity store,
			LoginContext loginUser) {
		super(id, loginUser.getTenantId(), loginUser.getLoginId());
		Preconditions.checkArgument(CollectionUtils.isNotEmpty(chatContents), "群发消息内容不可以为空。");
		Preconditions.checkArgument(CollectionUtils.isNotEmpty(userNames), "群发目标微信用户不可以为空。");
		Preconditions.checkArgument(CollectionUtils.isNotEmpty(userNames), "群发目标微信用户不可以为空。");
		this.numOfContent = chatContents.size();
		this.numOfUsername = userNames.size();
		this.userNames = Sets.newHashSet(userNames);
		this.chatContents = Lists.newArrayList();
		chatContents.forEach(m -> this.chatContents.add(new ChatContent(id, m)));
		this.storeId = store.getId();
	}

	@Override
	public Map<String, Object> toParamMap(String... excludes) {
		Map<String, Object> data = super.toParamMap("userNames", "chatContents", "storeId");
		data.put("usernames", Joiner.on(',').join(this.userNames));
		data.put("storeId", storeId);
		return data;
	}

	WechatGroupSendMsgEntity(Long id, ResultSet res) {
		super(id, res);
	}

	public Set<String> getUserNames() {
		return userNames;
	}

	public int getNumOfUsername() {
		return numOfUsername;
	}

	public int getNumOfContent() {
		return numOfContent;
	}

	List<ChatContent> getChatContents() {
		return chatContents;
	}

	public Optional<Long> getStoreId() {
		return Optional.ofNullable(storeId);
	}

	class ChatContent implements Sorting, BatchSetter {

		private final Long batchNo;
		private final int type;
		private final String content;
		private final int order;

		@Override
		public void setValues(PreparedStatement ps) throws SQLException {
			ps.setLong(1, this.batchNo);
			ps.setInt(2, this.type);
			ps.setInt(3, this.order);
			ps.setString(4, this.content);
		}

		ChatContent(Long batchNo, int type, String content, int order) {
			this.type = type;
			this.content = content;
			this.order = order;
			this.batchNo = batchNo;
		}

		ChatContent(Long batchNo, Map<String, Object> data) {
			this.batchNo = batchNo;
			this.type = MapUtils.getIntValue(data, "type");
			this.content = MapUtils.getString(data, "content");
			this.order = MapUtils.getIntValue(data, "order");
		}

		@Override
		public int getIndex() {
			return order;
		}

		public Long getBatchNo() {
			return batchNo;
		}

		public int getType() {
			return type;
		}

		public String getContent() {
			return content;
		}

		public int getOrder() {
			return order;
		}

		Map<String, Object> toMap() {
			Map<String, Object> data = Maps.newHashMap();
			data.put("batchNo", this.batchNo);
			data.put("contentType", this.type);
			data.put("content", content);
			data.put("order", order);
			return data;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o)
				return true;
			if (!(o instanceof ChatContent))
				return false;
			ChatContent that = (ChatContent) o;
			return type == that.type && Objects.equal(batchNo, that.batchNo) && Objects.equal(content, that.content);
		}

		@Override
		public int hashCode() {
			return Objects.hashCode(batchNo, type, content);
		}

		@Override
		public String toString() {
			return MoreObjects.toStringHelper(this).add("batchNo", batchNo).add("type", type).add("content", content)
					.add("order", order).toString();
		}
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this).add("storeId", storeId).add("numOfUsername", numOfUsername)
				.add("numOfContent", numOfContent).add("userNames", userNames).add("chatContents", chatContents)
				.toString();
	}
}
