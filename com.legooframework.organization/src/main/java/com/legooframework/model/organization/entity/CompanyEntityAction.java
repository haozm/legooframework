package com.legooframework.model.organization.entity;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.legooframework.model.core.base.entity.BaseEntityAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class CompanyEntityAction extends BaseEntityAction<CompanyEntity> {

    private static final Logger logger = LoggerFactory.getLogger(CompanyEntityAction.class);

    public CompanyEntityAction() {
        super("OrganizationCache");
    }

    // true 则说明绑定成功（ 再次激活成功）
    public boolean addStore(Long companyId, StoreEntity store) {
        Preconditions.checkNotNull(companyId, "公司编码不可以为空.");
        Preconditions.checkNotNull(store, "待添加的门店实例不可以为空.");
        Optional<CompanyEntity> company = findById(companyId);
        Preconditions.checkState(company.isPresent(), "指定编码%s的公司不存在.", companyId);
        if (company.get().contains(store)) return true;
        Map<String, Object> params = Maps.newHashMap();
        params.put("companyId", company.get().getId());
        params.put("storeId", store.getId());
        int res = super.update(getStatementFactory(), getModelName(), "add_store_to_company", params);
        Preconditions.checkState(1 == res, "持久化保存公司门店对应关系 %s 失败.", params);
        evictEntity(company.get());
        return true;
    }

    public void editAction(String id, String shortName, String businessLicense, String detailAddress,
                           String legalPerson, String contactNumber, String remark) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(id), "公司实例ID不不允许为空.");
        CompanyEntity entity = loadById(id);
        Optional<CompanyEntity> update = entity.edit(shortName, businessLicense, detailAddress,
                legalPerson, contactNumber, remark);
        if (update.isPresent()) {
            int res = update(getStatementFactory(), getModelName(), "edit", update.get());
            Preconditions.checkState(1 == res, "修改公司信息写入数据库失败");
        }
    }

    /**
     * 受雇于公司
     *
     * @param company
     * @return
     */
    public void hireEmployee(CompanyEntity company, EmployeeEntity emp) {
        Objects.requireNonNull(company);
        Objects.requireNonNull(emp);
        emp.setCompanyId(company.getId());
    }

    /**
     * 分配给门店
     *
     * @param store
     * @param emp
     * @return
     */
    public void assginToStore(StoreEntity store, EmployeeEntity emp) {
        Objects.requireNonNull(store);
        Objects.requireNonNull(emp);
        emp.setStoreId(store.getId());
    }

    @Override
    protected RowMapper<CompanyEntity> getRowMapper() {
        return new RowMapperImpl();
    }

    class RowMapperImpl implements RowMapper<CompanyEntity> {
        @Override
        public CompanyEntity mapRow(ResultSet res, int rowNum) throws SQLException {
            return new CompanyEntity(res.getLong("companyId"), res);
        }
    }

    private long idGenerator;

    public void init() {
        long max_id = queryForLong("SELECT MAX(id) FROM org_base_info ", 1L);
        this.idGenerator = max_id + 1;
    }
}
