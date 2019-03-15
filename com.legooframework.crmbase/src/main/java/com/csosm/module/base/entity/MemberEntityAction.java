package com.csosm.module.base.entity;

import com.csosm.commons.adapter.LoginUserContext;
import com.csosm.commons.entity.BaseEntityAction;
import com.csosm.commons.entity.ResultSetUtil;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ParameterizedPreparedStatementSetter;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class MemberEntityAction extends BaseEntityAction<MemberEntity> {

    private static final Logger logger = LoggerFactory.getLogger(MemberEntityAction.class);

    public MemberEntityAction() {
        super("MemberEntity", "adapterCache");
    }

    @Deprecated
    @Override
    public MemberEntity loadById(Object id) {
        throw new UnsupportedOperationException("MemberEntityAction##loadById[id] 已废弃");
    }

    public Map<String, Object> storeCheckAndToMap(StoreEntity store) {
        Objects.requireNonNull(store);
        Preconditions.checkState(store.getCompanyId().isPresent(), String.format("门店[%s]无公司信息", store.getId()));
        Map<String, Object> params = Maps.newHashMap();
        params.put("storeId", store.getId());
        params.put("companyId", store.getCompanyId().get());
        params.put("tableName", store.getContactTableName());
        return params;
    }

    @Override
    public Optional<MemberEntity> findById(Object id) {
        return findById(null, id);
    }

    public Optional<MemberEntity> findById(StoreEntity store, Object id) {
        Objects.requireNonNull(id);
        Map<String, Object> params = Maps.newHashMap();
        if (store == null) {
            params.put("tableName", "CONTACT_MOULD");
        } else {
            params.putAll(storeCheckAndToMap(store));
        }
        params.put("id", id);
        String sql = getExecSql("findById", params);
        MemberEntity entity = getNamedParameterJdbcTemplate().query(sql, params, getResultSetExtractor());
        if (entity == null) return Optional.absent();
        return Optional.of(entity);
    }

    public MemberEntity loadById(StoreEntity store, Object id) {
        Optional<MemberEntity> memberOpt = findById(store, id);
        Preconditions.checkState(memberOpt.isPresent(), "会员信息不存在");
        return memberOpt.get();
    }

    public Optional<MemberEntity> findByPhone(StoreEntity store, String phoneNo) {
        if (Strings.isNullOrEmpty(phoneNo)) return Optional.absent();
        Map<String, Object> params = storeCheckAndToMap(store);
        params.put("phoneNo", phoneNo);
        String sql = getExecSql("findByPhone", params);
        MemberEntity member = getNamedParameterJdbcTemplate().query(sql, params, getResultSetExtractor());
        if (member == null) return Optional.absent();
        return Optional.of(member);
    }

//	public Optional<MemberEntity> findByPhone(String phoneNo) {
//        if (Strings.isNullOrEmpty(phoneNo)) return Optional.absent();
//        Map<String, Object> params = Maps.newHashMap();
//        params.put("phoneNo", phoneNo);
//        String sql = getExecSql("findByPhone", params);
//        MemberEntity member = getNamedParameterJdbcTemplate().query(sql, params, getResultSetExtractor());
//        if (member == null) return Optional.absent();
//        return Optional.of(member);
//    }

    public void addShoppingGuide(EmployeeEntity employee, List<MemberEntity> members) {
        if (CollectionUtils.isEmpty(members)) return;
        Preconditions.checkNotNull(employee);
        List<MemberEntity> build_list = Lists.newArrayList();
        for (MemberEntity mm : members) {
            Optional<MemberEntity> res = mm.addShoppingGuide(employee);
            if (res.isPresent()) build_list.add(res.get());
        }
        if (CollectionUtils.isNotEmpty(build_list)) {
            getJdbcTemplate().batchUpdate(getExecSql("updateBuildFlag", null), build_list, 200,
                    new ParameterizedPreparedStatementSetter<MemberEntity>() {
                        @Override
                        public void setValues(PreparedStatement preparedStatement, MemberEntity memberEntity) throws SQLException {
                            preparedStatement.setObject(1, memberEntity.getAssignState());
                            preparedStatement.setObject(2, memberEntity.getId());
                        }
                    });
            if (getCache().isPresent())
                getCache().get().invalidateAll();
        }
    }

    public void removeShoppingGuide(EmployeeEntity employee, List<MemberEntity> members) {
        if (CollectionUtils.isEmpty(members)) return;
        List<MemberEntity> build_list = Lists.newArrayList();
        if (employee == null) {
            for (MemberEntity mm : members) {
                Optional<MemberEntity> res = mm.removeAllShoppingGuide();
                if (res.isPresent()) build_list.add(res.get());
            }
        } else {
            for (MemberEntity mm : members) {
                Optional<MemberEntity> res = mm.removeShoppingGuide(employee);
                if (res.isPresent()) build_list.add(res.get());
            }
        }
        if (CollectionUtils.isNotEmpty(build_list)) {
            getJdbcTemplate().batchUpdate(getExecSql("updateBuildFlag", null), build_list, 200,
                    new ParameterizedPreparedStatementSetter<MemberEntity>() {
                        @Override
                        public void setValues(PreparedStatement preparedStatement, MemberEntity memberEntity) throws SQLException {
                            preparedStatement.setObject(1, memberEntity.getAssignState());
                            preparedStatement.setObject(2, memberEntity.getId());
                        }
                    });
            if (getCache().isPresent())
                getCache().get().invalidateAll();
        }
    }

    /**
     * 批量设置一批 会员无效
     *
     * @param memberIds
     * @param user
     */
    public void makeMembersUneffective(Collection<Integer> memberIds, LoginUserContext user) {
        if (CollectionUtils.isEmpty(memberIds)) return;
        Preconditions.checkNotNull(user);
        Preconditions.checkState(user.getStore().isPresent(), "当前登陆用户无门店信息，无法执行该操作...");
        StoreEntity store = user.getStore().get();
        Optional<List<MemberEntity>> members = findByIds(store, memberIds, false, false);
        if (!members.isPresent()) return;
        List<Integer> mms = Lists.newArrayList();
        for (MemberEntity m : members.get()) {
            Optional<MemberEntity> change = m.makedUneffective();
            if (change.isPresent()) mms.add(change.get().getId());
        }
        if (CollectionUtils.isEmpty(mms)) return;
        Map<String, Object> params = Maps.newHashMap();
        params.put("memberIds", mms);
        getJdbc().update(getExecSql("makeMembersUneffective", params), params);
    }

    /**
     * @param needPhone boolean 是否需要过滤拥有合法的手机号码
     * @param effective boolean 是否需要过滤无效会员
     * @return Optional_Member_Entity
     */
//    public Optional<List<MemberEntity>> findByIds(Collection<Integer> ids, boolean needPhone, boolean effective) {
//        if (CollectionUtils.isEmpty(ids))
//            return Optional.absent();
//        Map<String, Object> params = Maps.newHashMap();
//        params.put("memberIds", ids);
//        params.put("needPhone", needPhone);
//        params.put("effective", effective);
//        List<MemberEntity> members = getNamedParameterJdbcTemplate().query(getExecSql("findMemberByIds", params),
//                params, new RowMapperImpl());
//        return Optional.fromNullable(CollectionUtils.isEmpty(members) ? null : members);
//    }

//    @Override
//    public Optional<MemberEntity> findById(Object id) {
//        return Optional.fromNullable(super.selectById(id));
//    }

    /**
     * @param needPhone boolean 是否需要过滤拥有合法的手机号码
     * @param effective boolean 是否需要过滤无效会员
     * @return Optional_MemberEntity
     */
    public Optional<List<MemberEntity>> findByIds(StoreEntity store, Collection<Integer> ids, boolean needPhone,
                                                  boolean effective) {
        if (CollectionUtils.isEmpty(ids))
            return Optional.absent();
        Map<String, Object> params = storeCheckAndToMap(store);
        params.put("memberIds", ids);
        params.put("needPhone", needPhone);
        params.put("effective", effective);
        List<MemberEntity> members = getNamedParameterJdbcTemplate().query(getExecSql("findMemberByIds", params),
                params, new RowMapperImpl());
        return Optional.fromNullable(CollectionUtils.isEmpty(members) ? null : members);
    }

//    /**
//     * @param needPhone boolean 是否需要过滤拥有合法的手机号码
//     * @param effective boolean 是否需要过滤无效会员
//     * @return Optional_MemberEntity
//     */
//    public Optional<List<MemberEntity>> findByIds(StoreEntity store, Collection<Integer> ids, boolean needPhone,
//                                                  boolean effective) {
//        if (CollectionUtils.isEmpty(ids))
//            return Optional.absent();
//        Preconditions.checkNotNull(store);
//        Map<String, Object> params = Maps.newHashMap();
//        params.put("memberIds", ids);
//        params.put("storeId", store.getId());
//        params.put("needPhone", needPhone);
//        params.put("effective", effective);
//        List<MemberEntity> members = getNamedParameterJdbcTemplate().query(getExecSql("findMemberByIds", params),
//                params, new RowMapperImpl());
//        return Optional.fromNullable(CollectionUtils.isEmpty(members) ? null : members);
//    }

    // 获取指定门店 与手机号码 匹配的会员
    public Optional<MemberEntity> findByStoreWithMobile(StoreEntity store, String mobileNum) {
        Preconditions.checkNotNull(store);
        Preconditions.checkArgument(!Strings.isNullOrEmpty(mobileNum), "手机号码不可以为空");
        Map<String, Object> params = storeCheckAndToMap(store);
        params.put("mobileNum", mobileNum);
        MemberEntity members = getNamedParameterJdbcTemplate().query(getExecSql("find_bystore_withmobilenum", params),
                params, getResultSetExtractor());
        return Optional.fromNullable(members);
    }

//    // 获取指定门店 与手机号码 匹配的会员
//    public Optional<MemberEntity> findByStoreWithMobile(StoreEntity store, String mobileNum) {
//        Preconditions.checkNotNull(store);
//        Preconditions.checkArgument(!Strings.isNullOrEmpty(mobileNum), "手机号码不可以为空");
//        Map<String, Object> params = Maps.newHashMap();
//        params.put("storeId", store.getId());
//        params.put("mobileNum", mobileNum);
//        MemberEntity members = getNamedParameterJdbcTemplate().query(getExecSql("find_bystore_withmobilenum", params),
//                params, getResultSetExtractor());
//        return Optional.fromNullable(members);
//    }

    // 获取指定导购的会员清单
    public Optional<MemberEntity> findByShoppingGuide(StoreEntity store, EmployeeEntity shoppingGuide) {
        Preconditions.checkNotNull(shoppingGuide);
        Map<String, Object> params = storeCheckAndToMap(store);
        params.put("shoppingguideId", shoppingGuide.getId());
        MemberEntity members = getNamedParameterJdbcTemplate().query(getExecSql("findmember_by_shoppingguide", params),
                params, getResultSetExtractor());
        return Optional.fromNullable(members);
    }

    // 获取指定导购的会员清单
//    public Optional<MemberEntity> findByShoppingGuide(EmployeeEntity shoppingGuide) {
//        Preconditions.checkNotNull(shoppingGuide);
//        Map<String, Object> params = Maps.newHashMap();
//        params.put("shoppingguideId", shoppingGuide.getId());
//        MemberEntity members = getNamedParameterJdbcTemplate().query(getExecSql("findmember_by_shoppingguide", params),
//                params, getResultSetExtractor());
//        return Optional.fromNullable(members);
//    }

    /**
     * @param needPhone boolean 是否需要过滤拥有合法的手机号码
     * @param effective boolean 是否需要过滤无效会员
     * @return Optional_MemberEntity
     */
    public Optional<List<MemberEntity>> findByStore(StoreEntity store, boolean needPhone, boolean effective) {
        Map<String, Object> params = storeCheckAndToMap(store);
        params.put("needPhone", needPhone);
        params.put("effective", effective);
        List<MemberEntity> members = getNamedParameterJdbcTemplate().query(getExecSql("findMemberByStore", params),
                params, new RowMapperImpl());
        return Optional.fromNullable(CollectionUtils.isEmpty(members) ? null : members);
    }

//    /**
//     * @param needPhone boolean 是否需要过滤拥有合法的手机号码
//     * @param effective boolean 是否需要过滤无效会员
//     * @return Optional_MemberEntity
//     */
//    public Optional<List<MemberEntity>> findByStore(StoreEntity store, boolean needPhone, boolean effective) {
//        Preconditions.checkNotNull(store);
//        Map<String, Object> params = Maps.newHashMap();
//        params.put("storeId", store.getId());
//        params.put("needPhone", needPhone);
//        params.put("effective", effective);
//        List<MemberEntity> members = getNamedParameterJdbcTemplate().query(getExecSql("findMemberByStore", params),
//                params, new RowMapperImpl());
//        return Optional.fromNullable(CollectionUtils.isEmpty(members) ? null : members);
//    }

    /**
     * 改变会员详情数据
     *
     * @param name
     * @param memberType
     * @param sex
     * @param serviceLevel
     * @param phone
     * @param memberCardType
     * @param memberCardNum
     * @param createCardTime
     * @param qqNum
     * @param weixinId
     * @param weiboNum
     * @param email
     * @param marryStatus
     * @param detailAddress
     * @param idols
     * @param carePeople
     * @param zodiac
     * @param characterType
     * @param jobType
     * @param faithType
     * @param likeContact
     * @param gregorianBirthday
     * @param lunarBirthday
     */
    public MemberEntity modifyMember(MemberEntity member, String name, Integer memberType, Integer sex, Integer serviceLevel,
                                     String phone, Integer memberCardType, String memberCardNum, Date createCardTime, String qqNum,
                                     String weixinId, String weiboNum, String email, Integer marryStatus, String detailAddress, String idols,
                                     String carePeople, Integer zodiac, Integer characterType, String jobType, Integer faithType,
                                     Integer likeContact, Date gregorianBirthday, Date lunarBirthday, String iconUrl, Integer calendarType) {
        Objects.requireNonNull(member);
        MemberEntity clone = member.modify(name, memberType, sex, serviceLevel, phone, memberCardType, memberCardNum,
                createCardTime, qqNum, weixinId, weiboNum, email, marryStatus, detailAddress, idols, carePeople, zodiac,
                characterType, jobType, faithType, likeContact, gregorianBirthday, lunarBirthday, iconUrl,
                calendarType);
        if (!member.equalsModifyInfo(clone)) {
            Map<String, Object> map = clone.toMap();
            String sql = getExecSql("updateMember", null);
            getNamedParameterJdbcTemplate().update(sql, map);
            if (getCache().isPresent()) getCache().get().cleanUp();
        }
        return clone;
    }

    public void saveMember(String name, Integer memberType, Integer sex, Integer serviceLevel, String phone,
                           Integer memberCardType, String memberCardNum, Date createCardTime, Integer createCardStoreId,
                           String createCardStoreName, String weixinId, String qqNum, String weiboNum, String email,
                           Date gregorianBirthday, Date lunarBirthday, Integer calendarType, String iconUrl, Integer marryStatus,
                           String detailAddress, String idols, String carePeople, Integer zodiac, Integer characterType,
                           String jobType, Integer faithType, Integer likeContact, StoreEntity store) {
        Preconditions.checkArgument(store.getCompanyId().isPresent(), "公司ID不能为空");
        MemberEntity member = new MemberEntity(name, memberType, sex, serviceLevel, phone, memberCardType,
                memberCardNum, createCardTime, createCardStoreId, createCardStoreName, weixinId, qqNum, weiboNum, email,
                gregorianBirthday, lunarBirthday, calendarType, iconUrl, marryStatus, detailAddress, idols, carePeople,
                zodiac, characterType, jobType, faithType, likeContact, store);
        Map<String, Object> params = member.toMap();
        String saveSql = getExecSql("saveMember", params);
        getNamedParameterJdbcTemplate().update(saveSql, params);
        Optional<MemberEntity> optional = findByPhone(store, phone);
        Preconditions.checkState(optional.isPresent(), "新增失败");
        bindStoreMember(store, optional.get());
        if (getCache().isPresent()) getCache().get().cleanUp();
    }

    private void bindStoreMember(StoreEntity store, MemberEntity member) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("storeId", store.getId());
        params.put("memberId", member.getId());
        String sql = getExecSql("saveStoreMember", params);
        int res = getNamedParameterJdbcTemplate().update(sql, params);
        Preconditions.checkState(res == 1, "绑定门店会员发生异常");
    }

//    /**
//     * @param needPhone boolean 是否需要过滤拥有合法的手机号码
//     * @param effective boolean 是否需要过滤无效会员
//     * @return Optional_MemberEntity
//     */
//    public Optional<List<MemberEntity>> findByStores(Collection<StoreEntity> stores, boolean needPhone,
//                                                     boolean effective) {
//        Preconditions.checkArgument(CollectionUtils.isNotEmpty(stores));
//        List<Integer> dis = Lists.newArrayListWithCapacity(stores.size());
//        for (StoreEntity $it : stores)
//            dis.add($it.getId());
//        Map<String, Object> params = Maps.newHashMap();
//        params.put("storeIds", dis);
//        params.put("needPhone", needPhone);
//        params.put("effective", effective);
//        List<MemberEntity> members = getNamedParameterJdbcTemplate().query(getExecSql("findMemberByStores", params),
//                params, new RowMapperImpl());
//        return Optional.fromNullable(CollectionUtils.isEmpty(members) ? null : members);
//    }


    @Override
    protected ResultSetExtractor<MemberEntity> getResultSetExtractor() {
        return new ResultSetExtractorImpl();
    }

    class ResultSetExtractorImpl implements ResultSetExtractor<MemberEntity> {

        @Override
        public MemberEntity extractData(ResultSet resultSet) throws SQLException, DataAccessException {
            if (resultSet.next()) {
                return buildByResultSet(resultSet);
            }
            return null;
        }
    }

    class RowMapperImpl implements RowMapper<MemberEntity> {
        @Override
        public MemberEntity mapRow(ResultSet resultSet, int i) throws SQLException {
            return buildByResultSet(resultSet);
        }
    }


    private MemberEntity buildByResultSet(ResultSet resultSet) throws SQLException {
        String weixin_id = ResultSetUtil.getOptValue(resultSet, "weixinId", String.class).orNull();
        String weixin_info = resultSet.getString("weixinInfo");
        String storeInfo = resultSet.getString("storeInfo");
        WeixinAct weixinAct = null;

        if (!Strings.isNullOrEmpty(weixin_id)) {
            if (!Strings.isNullOrEmpty(weixin_info)) {
                String[] args = StringUtils.split(weixin_info, "$$");
                weixinAct = new WeixinAct(weixin_id, args[0], args[1], args[2]);
            } else {
                weixinAct = new WeixinAct(weixin_id, null, null, null);
            }
        }

        MemberEntity.CardStore cardStore = null;
        if (!Strings.isNullOrEmpty(storeInfo)) {
            String[] storeArgs = StringUtils.split(storeInfo, "$$");
            cardStore = new MemberEntity.CardStore(storeArgs[0], storeArgs[1]);
        }

        Birthday birthday = new Birthday(ResultSetUtil.getOptValue(resultSet, "birthday", Date.class).orNull(),
                ResultSetUtil.getOptValue(resultSet, "lunarBirthDay", Date.class).orNull(),
                ResultSetUtil.getOptValue(resultSet, "calendarType", Integer.class).orNull());

        String _shoppingGuideIds = resultSet.getString("shoppingGuideIds");
        Set<Integer> shoppingGuideIds = null;
        if (!Strings.isNullOrEmpty(_shoppingGuideIds)) {
            shoppingGuideIds = Sets.newHashSet();
            for (String $it : StringUtils.split(_shoppingGuideIds, ',')) {
                shoppingGuideIds.add(Integer.valueOf($it));
            }

        }
        return new MemberEntity(ResultSetUtil.getOptValue(resultSet, "id", Integer.class).orNull(),
                ResultSetUtil.getOptValue(resultSet, "name", String.class).orNull(),
                ResultSetUtil.getOptValue(resultSet, "sex", Integer.class).orNull(),
                ResultSetUtil.getOptValue(resultSet, "phone", String.class).orNull(), birthday,
                ResultSetUtil.getOptValue(resultSet, "telephone", String.class).orNull(),
                ResultSetUtil.getOptValue(resultSet, "certificateType", Integer.class).orNull(),
                ResultSetUtil.getOptValue(resultSet, "certificate", String.class).orNull(),
                ResultSetUtil.getOptValue(resultSet, "oldMemberCode", String.class).orNull(),
                ResultSetUtil.getOptValue(resultSet, "effectiveFlag", Integer.class).orNull(),
                ResultSetUtil.getOptValue(resultSet, "serviceLevel", Integer.class).orNull(),
                ResultSetUtil.getOptValue(resultSet, "memberType", Integer.class).orNull(),
                ResultSetUtil.getOptValue(resultSet, "companyId", Integer.class).orNull(),
                ResultSetUtil.getOptValue(resultSet, "status", Integer.class).orNull(),
                ResultSetUtil.getOptValue(resultSet, "hasWeixinAccount", Integer.class).orNull(),
                ResultSetUtil.getOptValue(resultSet, "storeIds", Integer.class).orNull(),
                ResultSetUtil.getOptValue(resultSet, "weixinId", String.class).orNull(),
                ResultSetUtil.getOptValue(resultSet, "srfm", String.class).orNull(),
                ResultSetUtil.getOptValue(resultSet, "crfm", String.class).orNull(),
                ResultSetUtil.getOptValue(resultSet, "totalScore", Integer.class).orNull(), weixinAct,
                ResultSetUtil.getOptValue(resultSet, "memberCardNum", String.class).orNull(), cardStore,
                ResultSetUtil.getOptValue(resultSet, "createCardTime", Date.class).orNull(),
                ResultSetUtil.getOptValue(resultSet, "memberCardType", Integer.class).orNull(),
                ResultSetUtil.getOptValue(resultSet, "qqNum", String.class).orNull(),
                ResultSetUtil.getOptValue(resultSet, "weiboNum", String.class).orNull(),
                ResultSetUtil.getOptValue(resultSet, "email", String.class).orNull(),
                ResultSetUtil.getOptValue(resultSet, "marryStatus", Integer.class).orNull(),
                ResultSetUtil.getOptValue(resultSet, "detailAddress", String.class).orNull(),
                ResultSetUtil.getOptValue(resultSet, "idols", String.class).orNull(),
                ResultSetUtil.getOptValue(resultSet, "carePeople", String.class).orNull(),
                ResultSetUtil.getOptValue(resultSet, "zodiac", Integer.class).orNull(),
                ResultSetUtil.getOptValue(resultSet, "characterType", Integer.class).orNull(),
                ResultSetUtil.getOptValue(resultSet, "jobType", String.class).orNull(),
                ResultSetUtil.getOptValue(resultSet, "faithType", Integer.class).orNull(),
                ResultSetUtil.getOptValue(resultSet, "likeContact", Integer.class).orNull(),
                ResultSetUtil.getOptValue(resultSet, "iconUrl", String.class).orNull(),
                shoppingGuideIds);
    }
}
