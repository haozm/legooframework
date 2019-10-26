package com.legooframework.model.crmjob.entity;

import com.legooframework.model.core.base.entity.BaseEntityAction;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class DeviceWithWeixinEntityAction extends BaseEntityAction<DeviceWithWeixinEntity> {

    public DeviceWithWeixinEntityAction() {
        super(null);
    }

    public Optional<List<DeviceWithWeixinEntity>> loadAll() {
        return super.queryForEntities("loadAll", null, getRowMapper());
    }

    public Optional<DeviceWithWeixinEntity> loadByStoreId(Integer companyId, Integer storeId) {
        Optional<List<DeviceWithWeixinEntity>> exits_list = loadAll();
        return exits_list.flatMap(list -> list.stream().filter(x -> x.isStore(companyId, storeId))
                .findFirst());
    }

    public Optional<List<DeviceWithWeixinEntity>> loadByCompanyId(Integer companyId) {
        Optional<List<DeviceWithWeixinEntity>> exits_list = loadAll();
        if (!exits_list.isPresent()) return Optional.empty();
        List<DeviceWithWeixinEntity> sub_list = exits_list.get().stream().filter(x -> x.isCompany(companyId))
                .collect(Collectors.toList());
        return Optional.ofNullable(CollectionUtils.isEmpty(sub_list) ? null : sub_list);
    }

    @Override
    protected RowMapper<DeviceWithWeixinEntity> getRowMapper() {
        return new RowMapperImpl();
    }

    class RowMapperImpl implements RowMapper<DeviceWithWeixinEntity> {
        @Override
        public DeviceWithWeixinEntity mapRow(ResultSet res, int rowNum) throws SQLException {
            return new DeviceWithWeixinEntity(res);
        }
    }
}
