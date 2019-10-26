package com.legooframework.model.takecare.entity;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.legooframework.model.core.base.entity.BaseEntityAction;
import com.legooframework.model.covariant.entity.EmpEntity;
import com.legooframework.model.covariant.entity.SendChannel;
import com.legooframework.model.covariant.service.MemberAgg;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class CareBirthdayEntityAction extends BaseEntityAction<CareBirthdayEntity> {

    private static final Logger logger = LoggerFactory.getLogger(CareBirthdayEntityAction.class);

    public CareBirthdayEntityAction() {
        super(null);
    }

    public CareBirthdayAgg careMember4ThisYear(EmpEntity employee, MemberAgg agg, Collection<SendChannel> channels,
                                               String followUpContent, String[] imgUrls) {
        Preconditions.checkArgument(CollectionUtils.isNotEmpty(channels), "发送渠道不可以为空值");
        Map<String, Object> params = agg.getMember().toParamMap();
        params.put("sql", "findCareByMemberThisYear");
        Optional<List<CareBirthdayEntity>> care_list = findByParams(params);
        CareBirthdayEntity care = care_list.isPresent() ? care_list.get().get(0) :
                new CareBirthdayEntity(agg.getMember(), followUpContent);
        List<CareRecordEntity> careLogs = Lists.newArrayList();
        List<CareHisRecordEntity> hisCareLogs = Lists.newArrayList();
        for (SendChannel channel : channels) {
            if (SendChannel.SMS == channel) {
                careLogs.add(CareRecordEntity.smsBirthdayCare4Member(care, employee, agg, followUpContent));
                hisCareLogs.add(CareHisRecordEntity.smsBirthdayCare4Member(care, employee, agg, followUpContent));
            } else if (SendChannel.WECHAT == channel) {
                careLogs.add(CareRecordEntity.wxBirthdayCare4Member(care, employee, agg, followUpContent, imgUrls));
                hisCareLogs.add(CareHisRecordEntity.wxBirthdayCare4Member(care, employee, agg, followUpContent));
            } else if (SendChannel.CALLPHONE == channel || SendChannel.OFFLINE == channel) {
                careLogs.add(CareRecordEntity.manualBirthdayCare4Member(care, employee, agg.getMember()));
                hisCareLogs.add(CareHisRecordEntity.offlineBirthdayCare4Member(care, employee, agg.getMember()));
            } else if (SendChannel.CANCEL == channel) {
                careLogs.add(CareRecordEntity.cancelBirthdayCare4Member(care, employee, agg.getMember()));
                hisCareLogs.add(CareHisRecordEntity.cancelBirthdayCare4Member(care, employee, agg.getMember()));
            }
        }
        return new CareBirthdayAgg(care, careLogs, hisCareLogs, agg);
    }

    public void batchInsertCare(Collection<CareBirthdayEntity> birthdayCares) {
        if (CollectionUtils.isEmpty(birthdayCares)) return;
        List<CareBirthdayEntity> un_save_list = birthdayCares.stream().filter(x -> !x.hasSaved()).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(un_save_list)) {
            super.batchInsert("batchInsert", un_save_list);
            if (logger.isDebugEnabled())
                logger.debug(String.format("batchInsertCare(...) size is %d finisded..", birthdayCares.size()));
        }
    }

    public int singleInsertCare(CareBirthdayEntity birthdayCare) {
        if (birthdayCare.hasSaved()) return birthdayCare.getCareId();
        int careId = (Integer) super.insertSingleWithGeneratedKey("batchInsert", birthdayCare, null, GENERATEDKEY_INT);
        birthdayCare.setCareId(careId);
        if (logger.isDebugEnabled())
            logger.debug(String.format("insertBirthdayCare(%s) return id = %d", birthdayCare, careId));
        return careId;
    }

    Optional<List<CareBirthdayEntity>> findByParams(Map<String, Object> params) {
        return super.queryForEntities("query4list", params, getRowMapper());
    }

    @Override
    protected RowMapper<CareBirthdayEntity> getRowMapper() {
        return new RowMapperImpl();
    }

    private static class RowMapperImpl implements RowMapper<CareBirthdayEntity> {
        @Override
        public CareBirthdayEntity mapRow(ResultSet resultSet, int i) throws SQLException {
            return new CareBirthdayEntity(resultSet.getInt("id"), resultSet);
        }
    }
}
