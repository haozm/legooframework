package com.legooframework.model.salesrecords.entity;

import com.google.common.base.Joiner;
import com.legooframework.model.core.base.entity.BaseEntityAction;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

public class SaleAlloctResultEntityAction extends BaseEntityAction<SaleAlloctResultEntity> {

    private static final Logger logger = LoggerFactory.getLogger(SaleAlloctResultEntityAction.class);

    public SaleAlloctResultEntityAction() {
        super(null);
    }

    public void batchInsert(Collection<SaleAlloctResultEntity> instance) {
        if (CollectionUtils.isEmpty(instance)) return;
        Set<Integer> saleIds = instance.stream().map(SaleAlloctResultEntity::getSaleRecordId).collect(Collectors.toSet());
        Integer companyId = instance.iterator().next().getCompanyId();
        deleteBySaleIds(saleIds, companyId);
        super.batchInsert("batchInsert", instance);
        if (logger.isDebugEnabled())
            logger.debug(String.format("batchInsert(Collection<SaleAlloctResultEntity> instance) size is %d", instance.size()));
    }

    /**
     * 删除历史
     *
     * @param saleIds
     */
    private void deleteBySaleIds(Set<Integer> saleIds, Integer companyId) {
        if (CollectionUtils.isEmpty(saleIds)) return;
        String ids = Joiner.on(',').join(saleIds);
        String delete_sql = String.format("DELETE FROM acp.ACP_EMPLOYEE_ALLOT_RESULT WHERE sale_record_id IN (%s) AND company_id = %d",
                ids, companyId);
        super.update(delete_sql, null);
        if (logger.isDebugEnabled())
            logger.debug(String.format("deleteBySaleIds(%s,companyId=%d) finished....", ids, companyId));
    }

    @Override
    protected RowMapper<SaleAlloctResultEntity> getRowMapper() {
        return null;
    }

}
