package com.legooframework.model.usergroups.entity;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.SetUtils;
import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Joiner;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.legooframework.model.core.base.entity.BaseEntity;
import com.legooframework.model.core.base.runtime.LoginContext;
import com.legooframework.model.core.base.runtime.LoginContextHolder;
import com.legooframework.model.customer.entity.Channel;
import com.legooframework.model.customer.entity.CustomerEntity;
import com.legooframework.model.core.jdbc.BatchSetter;
import com.legooframework.model.organization.entity.EmployeeEntity;
import com.legooframework.model.organization.entity.StoreEntity;
import com.legooframework.model.wechat.entity.WechatAccountEntity;

public class UserGroupsEntity extends BaseEntity<Long> implements BatchSetter {

	private String name;
	private Channel channel;
	private Type type;
	private int editable = 1;
	private String context;
	private final Long storeId;
	private Set<Author> authors = Sets.newHashSet();
	private Set<String> accountIds = Sets.newHashSet();
	private int accountSize = 0;

	private static Splitter.MapSplitter mapSplitter = Splitter.on(',').withKeyValueSeparator(':');

	UserGroupsEntity(Long id, String name, Channel channel, Type type, StoreEntity store, LoginContext loginuser) {
		super(id, loginuser.getTenantId(), loginuser.getLoginId());
		this.name = name;
		this.channel = channel;
		this.type = type;
		Preconditions.checkNotNull(store);
		this.storeId = store.getId();
	}

	UserGroupsEntity(Long id, String name, Channel channel, Type type, StoreEntity store, Set<String> accountIds,
			LoginContext loginuser) {
		super(id, loginuser.getTenantId(), loginuser.getLoginId());
		this.name = name;
		this.channel = channel;
		this.type = type;
		this.accountIds = accountIds;
		Preconditions.checkNotNull(store);
		this.storeId = store.getId();
	}

	static UserGroupsEntity createGroup(StoreEntity store, Channel channel, Type type, Long id, String name,
			EmployeeEntity employee) {
		UserGroupsEntity group = null;
		switch (type) {
		case ALLFRIEND:
			group = new UserGroupsEntity(id, "所有好友", channel, type, store, Sets.newHashSet("*"),
					LoginContextHolder.get());
			group.editable = 0;
			break;
		case EMPLOYEE:
			group = new UserGroupsEntity(id, name, channel, type, store, Sets.newHashSet("*"),
					LoginContextHolder.get());
			group.context = employee.getId().toString();
			break;
		default:
			group = new UserGroupsEntity(id, name, channel, type, store, LoginContextHolder.get());
			break;
		}
		return group;
	}

	static UserGroupsEntity createAllFriendGroup(Channel channel, Long id, StoreEntity store) {
		UserGroupsEntity group = new UserGroupsEntity(id, "所有好友", channel, Type.ALLFRIEND, store,
				Sets.newHashSet("*"), LoginContextHolder.get());
		group.editable = 0;
		return group;
	}

	static UserGroupsEntity createUserGroup(StoreEntity store, Channel channel, Long id, String name) {
		return new UserGroupsEntity(id, name, channel, Type.CUSTOM, store, LoginContextHolder.get());
	}
	
	static UserGroupsEntity createEmpGroup(Channel channel,Long id, EmployeeEntity employee, StoreEntity store) {
		UserGroupsEntity res = new UserGroupsEntity(id, String.format("%s组", employee.getUserName()),
				channel, Type.EMPLOYEE, store, LoginContextHolder.get());
		res.context = employee.getId().toString();
		return res;
	}


	@Override
	public void setValues(PreparedStatement ps) throws SQLException {
		ps.setObject(1, getId());
		ps.setObject(2, getChannel().getVal());
		ps.setObject(3, getType().getVal());
		ps.setObject(4, getName());
		String author_ids = CollectionUtils.isEmpty(authors) ? null
				: StringUtils.join(this.authors.stream().map(Author::getId).collect(Collectors.toSet()), ',');
		ps.setObject(5, author_ids);
		ps.setObject(6, getStoreId());
		ps.setObject(7, getTenantId());
		ps.setObject(8, context);
		ps.setObject(9, getCreator());
	}

	@Override
	public Map<String, Object> toParamMap(String... excludes) {
		Map<String, Object> paramMap = super.toParamMap("context", "type", "channel", "accountIds", "userAcotDtos",
				"authors");
		paramMap.put("groupChannel", channel.getVal());
		paramMap.put("groupType", type.getVal());
		paramMap.put("groupName", name);
		paramMap.put("context", context);
		paramMap.put("editable", editable);
		paramMap.put("storeId", storeId);
		paramMap.put("authors", null);
		paramMap.put("accountIds", null);
		paramMap.put("accountSize", this.accountSize);
		if (CollectionUtils.isNotEmpty(accountIds))
			paramMap.put("accountIds", Joiner.on(',').join(this.accountIds));
		if (CollectionUtils.isNotEmpty(authors)) {
			Set<Long> ids = authors.stream().map(Author::getId).collect(Collectors.toSet());
			paramMap.put("authors", StringUtils.join(ids, ','));
		}
		return paramMap;
	}

	public enum Type {

		CUSTOM(1, "自定义类型"), WEIXIN(2, "微信类型"), EMPLOYEE(3, "导购类型"), ALLFRIEND(4, "所有好友");

		private final int val;
		private final String name;

		private Type(int val, String name) {
			this.val = val;
			this.name = name;
		}

		public static Type valueOf(int val) {
			Optional<Type> opt = Arrays.stream(values()).filter(x -> x.val == val).findFirst();
			Preconditions.checkState(opt.isPresent(), "不存在类型");
			return opt.get();
		}

		public static Set<Type> getUnEditableType() {
			Set<Type> types = Sets.newHashSet();
			types.add(WEIXIN);
			types.add(ALLFRIEND);
			return types;
		}

		public int getVal() {
			return val;
		}

		public String getName() {
			return name;
		}

	}

	public UserGroupsEntity(Long id, ResultSet res) {
		super(id, res);
		try {
			this.name = res.getString("groupName");
			this.channel = Channel.valueOf(res.getInt("groupChannel"));
			this.type = Type.valueOf(res.getInt("groupType"));
			this.storeId = res.getLong("storeId");
			String authorIds = res.getString("authors");
			this.context = res.getString("context");
			if (authorIds != null)
				mapSplitter.split(authorIds).entrySet()
						.forEach(x -> this.authors.add(new Author(Long.parseLong(x.getKey()), x.getValue())));
			String accountIds = res.getString("accountIds");
			if (!Strings.isNullOrEmpty(accountIds)) {
				this.accountIds = Splitter.on(',').splitToList(accountIds).stream().map(String::valueOf)
						.collect(Collectors.toSet());
			}
			this.accountSize = res.getInt("accountSize");
			this.editable = res.getInt("editable");
		} catch (SQLException e) {
			throw new RuntimeException("Restore UserGroupsEntity has SQLException", e);
		}
	}
	
	public SimpleGroupDto createSimpleGroupDto() {
		return new SimpleGroupDto(String.valueOf(this.getId()), this.name, this.accountSize, this.type.toString());
	}

	public Optional<UserGroupsEntity> modify(String name) {
		Preconditions.checkState(this.type == Type.CUSTOM || this.type == Type.EMPLOYEE, "当前分组类型为%s ，不允许编辑.", type.val);
		Preconditions.checkArgument(!Strings.isNullOrEmpty(name), "入参name不能为空");
		if (StringUtils.equals(this.name, name))
			return Optional.empty();
		UserGroupsEntity clone = (UserGroupsEntity) this.cloneMe();
		clone.name = name;
		return Optional.of(clone);
	}

	public void addAuthors(Collection<? extends Author> authors) {
		this.authors.clear();
		this.authors.addAll(authors);
	}

	public boolean hasAuthor() {
		return CollectionUtils.isNotEmpty(this.authors);
	}

	public boolean hasAccount() {
		return CollectionUtils.isNotEmpty(this.accountIds);
	}

	public boolean canEditable() {
		return this.getEditable() == 1;
	}

	public boolean isAuthored(EmployeeEntity employee) {
		if (CollectionUtils.isEmpty(this.authors))
			return false;
		return this.authors.stream().anyMatch(x -> x.getId().equals(employee.getId()));
	}

	public int accountSize() {
		return this.accountSize;
	}

	public int getAccountSize() {
		return this.accountSize;
	}

	Optional<UserGroupsEntity> updateEmployee(Collection<EmployeeEntity> employees) {
		if (CollectionUtils.isEmpty(employees)) {
			if (CollectionUtils.isEmpty(this.authors))
				return Optional.empty();
			UserGroupsEntity clone = (UserGroupsEntity) cloneMe();
			clone.authors = null;
			return Optional.of(clone);
		}
		Set<Author> adds = employees.stream().map(Author::new).collect(Collectors.toSet());
		if (SetUtils.isEqualSet(adds, this.authors))
			return Optional.empty();
		UserGroupsEntity clone = (UserGroupsEntity) cloneMe();
		clone.authors = adds;
		return Optional.of(clone);
	}

	public boolean isWechatAllGroup() {
		return this.channel == Channel.TYPE_WEIXIN && this.type == Type.ALLFRIEND;
	}

	public boolean isMemberAllGroup() {
		return this.channel == channel.TYPE_MEMBER && this.type == Type.ALLFRIEND;
	}

	public UserGroupsEntity addCustomers(Collection<CustomerEntity> customers) {
		if (CollectionUtils.isEmpty(customers))
			return this;
		Set<String> customerIds = customers.stream().map(x -> x.getId().getId().toString()).collect(Collectors.toSet());
		UserGroupsEntity clone = (UserGroupsEntity) cloneMe();
		clone.accountIds.addAll(customerIds);
		return clone;
	}
	
	public UserGroupsEntity removeCustomers(Collection<CustomerEntity> customers) {
		if (CollectionUtils.isEmpty(customers))
			return this;
		Set<String> customerIds = customers.stream().map(x -> x.getId().getId().toString()).collect(Collectors.toSet());
		UserGroupsEntity clone = (UserGroupsEntity) cloneMe();
		clone.accountIds.removeAll(customerIds);
		return clone;
	}
	
	Optional<UserGroupsEntity> removeMember(Collection<String> memberIds) {
		Preconditions.checkState(this.channel == Channel.TYPE_MEMBER);
		UserGroupsEntity clone = (UserGroupsEntity) cloneMe();
		clone.accountIds.removeAll(memberIds);
		return Optional.of(clone);
	}

	Optional<UserGroupsEntity> removeAccount(Collection<WechatAccountEntity> wechatIds) {
		Preconditions.checkState(this.channel == Channel.TYPE_WEIXIN);
		if (CollectionUtils.isEmpty(wechatIds))
			return Optional.empty();
		Set<String> new_accountIds = Sets.newHashSet(this.accountIds);
		for (WechatAccountEntity $it : wechatIds)
			new_accountIds.remove($it.getId().toString());
		if (SetUtils.isEqualSet(this.accountIds, new_accountIds))
			return Optional.empty();
		UserGroupsEntity clone = (UserGroupsEntity) cloneMe();
		clone.accountIds = new_accountIds;
		return Optional.of(clone);
	}

	public String getName() {
		return name;
	}

	public Channel getChannel() {
		return channel;
	}

	public Type getType() {
		return type;
	}

	public Set<Author> getAuthors() {
		return ImmutableSet.copyOf(this.authors);
	}

	public boolean deleteByUser() {
		// TODO
		return false;
	}

	public Long getStoreId() {
		return storeId;
	}

	public Set<String> getAccountIds() {
		return ImmutableSet.copyOf(this.accountIds);
	}

	public int getEditable() {
		return editable;
	}

	public boolean equalsSameEmpGroup(UserGroupsEntity that) {
		if (this == that)
			return true;
		return channel == that.channel && type == that.type && StringUtils.equals(that.context, that.context)
				&& Objects.equal(storeId, that.storeId);
	}

	public boolean equalsSameUsrGroup(UserGroupsEntity that) {
		if (this == that)
			return true;
		return channel == that.channel && type == that.type && StringUtils.equals(name, that.name)
				&& Objects.equal(storeId, that.storeId);
	}

	public boolean equalsSameAllFriendGroup(UserGroupsEntity that) {
		if (this == that)
			return true;
		return channel == that.channel && type == that.type && Objects.equal(storeId, that.storeId);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		if (!super.equals(o))
			return false;
		UserGroupsEntity that = (UserGroupsEntity) o;
		return editable == that.editable && Objects.equal(name, that.name) && channel == that.channel
				&& type == that.type && Objects.equal(storeId, that.storeId) && Objects.equal(authors, that.authors)
				&& Objects.equal(accountIds, that.accountIds);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(super.hashCode(), name, channel, type, editable, storeId, authors, accountIds);
	}

	public static class Author {

		private final Long id;
		private final String name;

		Author(Long id, String name) {
			this.id = id;
			this.name = name;
		}

		Author(EmployeeEntity employee) {
			this.id = employee.getId();
			this.name = employee.getUserName();
		}

		public Long getId() {
			return id;
		}

		public String getName() {
			return name;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o)
				return true;
			if (o == null || getClass() != o.getClass())
				return false;
			Author author = (Author) o;
			return Objects.equal(id, author.id) && Objects.equal(name, author.name);
		}

		@Override
		public int hashCode() {
			return Objects.hashCode(id, name);
		}

		@Override
		public String toString() {
			return MoreObjects.toStringHelper(this).add("id", id).add("name", name).toString();
		}
	}

	@Override
	public String toString() {
		return "UserGroupsEntity [name=" + name + ", channel=" + channel + ", type=" + type + ", editable=" + editable
				+ ", context=" + context + ", storeId=" + storeId + ", authors=" + authors + ", accountIds="
				+ accountIds + "]";
	}

}
