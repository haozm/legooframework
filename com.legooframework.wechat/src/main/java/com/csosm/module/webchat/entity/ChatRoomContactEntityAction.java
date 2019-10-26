package com.csosm.module.webchat.entity;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import com.csosm.commons.entity.BaseEntityAction;
import com.csosm.module.base.entity.OrganizationEntity;
import com.csosm.module.base.entity.StoreEntity;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ParameterizedPreparedStatementSetter;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;

public class ChatRoomContactEntityAction extends BaseEntityAction<ChatRoomContactEntity> {

    private static final Logger logger = LoggerFactory.getLogger(ChatRoomContactEntityAction.class);

    public ChatRoomContactEntityAction() {
        super("ChatRoomContactEntity", null);
    }

    public Optional<List<ChatRoomContactEntity>> findAllByStores(Collection<StoreEntity> stores) {
        Preconditions.checkArgument(CollectionUtils.isNotEmpty(stores), "请指定所要查询的门店...");
        Map<String, Object> params = Maps.newHashMap();
        List<Integer> storeIds = Lists.newArrayList();
        for (StoreEntity $it : stores) storeIds.add($it.getId());
        params.put("storeIds", storeIds);
        params.put("companyId", stores.iterator().next().getCompanyId().get());
        List<ChatRoomContactEntity> rooms = getNamedParameterJdbcTemplate()
                .query(getExecSql("findAllByStores", params), params, new RowMapperImpl());
        if (logger.isDebugEnabled())
            logger.debug(String.format("findAllByStores(%s) return -> devices size is %s", storeIds,
                    CollectionUtils.isEmpty(rooms) ? 0 : rooms.size()));
        return Optional.fromNullable(CollectionUtils.isEmpty(rooms) ? null : rooms);
    }

    public Optional<List<ChatRoomContactEntity>> findAllByNames(Collection<String> roomnames, OrganizationEntity company) {
        Preconditions.checkArgument(CollectionUtils.isNotEmpty(roomnames), "请指定所要查询的门店...");
        Preconditions.checkNotNull(company, "请指定所要查询的company...");
        Map<String, Object> params = Maps.newHashMap();
        //  LoginUserContext user = lo
        List<Integer> names = Lists.newArrayList();
        params.put("roomNames", roomnames);
        params.put("companyId", company.getId());
        List<ChatRoomContactEntity> rooms = getNamedParameterJdbcTemplate()
                .query(getExecSql("findAllByNames", params), params, new RowMapperImpl());
        if (logger.isDebugEnabled())
            logger.debug(String.format("findAllByNames(%s) return -> devices size is %s", roomnames,
                    CollectionUtils.isEmpty(rooms) ? 0 : rooms.size()));
        return Optional.fromNullable(CollectionUtils.isEmpty(rooms) ? null : rooms);
    }

    public void totalGroupActivity() {
        List<Map<String, Object>> groupActivities = getNamedParameterJdbcTemplate()
                .queryForList(getExecSql("totalGroupActivity", null), (Map<String, ?>) null);
        if (CollectionUtils.isNotEmpty(groupActivities)) {
            List<ChatroomActivityAmountEntity> list = Lists.newArrayList();
            for (Map<String, Object> $it : groupActivities) {
                String addOrDelSize = MapUtils.getString($it, "addOrDelSize");
                int addsize = 0;
                int delsize = 0;
                if (!Strings.isNullOrEmpty(addOrDelSize)) {
                    String[] args = StringUtils.split(addOrDelSize, ',');
                    addsize = Integer.valueOf(args[0]);
                    delsize = Integer.valueOf(args[1]);
                }
                ChatroomActivityAmountEntity instance = new ChatroomActivityAmountEntity(
                        MapUtils.getString($it, "chatroomname"), MapUtils.getInteger($it, "totalSize", 0),
                        addsize, delsize, MapUtils.getInteger($it, "mssageSize", 0),
                        MapUtils.getInteger($it, "talkSize", 0), MapUtils.getString($it, "amountDate"));
                list.add(instance);
            } // end_for
            getJdbcTemplate().batchUpdate(getExecSql("batchInsertGroupActivity", null), list, 2000,
                    new ParameterizedPreparedStatementSetter<ChatroomActivityAmountEntity>() {
                        //   chatroomname, total_number, add_number, remove_number, talk_number, msg_number, amount_date,
                        @Override
                        public void setValues(PreparedStatement ps, ChatroomActivityAmountEntity entity) throws SQLException {
                            ps.setObject(1, entity.getId());
                            ps.setObject(2, entity.getTotalSize());
                            ps.setObject(3, entity.getAddSize());
                            ps.setObject(4, entity.getDelSize());
                            ps.setObject(5, entity.getTalkSize());
                            ps.setObject(6, entity.getMsgSize());
                            ps.setObject(7, entity.getAmountDate());
                        }
                    });
            if (logger.isDebugEnabled())
                logger.debug(String.format("本次统计群活跃度基础数据共计%s 条....", list.size()));
        }
    }


    @Override
    protected ResultSetExtractor<ChatRoomContactEntity> getResultSetExtractor() {
        return new ResultSetExtractorImpl();
    }

    class RowMapperImpl implements RowMapper<ChatRoomContactEntity> {
        @Override
        public ChatRoomContactEntity mapRow(ResultSet resultSet, int i) throws SQLException {
            return buildByResultSet(resultSet);
        }
    }

    class ResultSetExtractorImpl implements ResultSetExtractor<ChatRoomContactEntity> {

        @Override
        public ChatRoomContactEntity extractData(ResultSet resultSet)
                throws SQLException, DataAccessException {
            if (resultSet.next()) {
                return buildByResultSet(resultSet);
            }
            return null;
        }
    }

    private ChatRoomContactEntity buildByResultSet(ResultSet resultSet) throws SQLException {
        // Long id, String name, String nikename, String owner, String[] weixinIds,
        // String[] weixinNames
        String weixinIds = resultSet.getString("weixinIds");
        String weixinNames = resultSet.getString("weixinNames");
        String owners = resultSet.getString("owners");
        String storeIds = resultSet.getString("storeIds");
        return new ChatRoomContactEntity(
                resultSet.getString("name"),
                resultSet.getString("nickname"), owners,
                StringUtils.isNoneBlank(weixinIds) ? StringUtils.split(weixinIds, ';') : null,
                StringUtils.isNoneBlank(weixinNames) ? StringUtils.split(weixinNames, '、') : null,
                storeIds, resultSet.getInt("companyId"));
    }

}
