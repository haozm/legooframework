package com.legooframework.model.rfm.service;

import com.csosm.commons.adapter.LoginUserContext;
import com.csosm.commons.server.AbstractBaseServer;
import com.csosm.module.base.entity.OrganizationEntity;
import com.csosm.module.base.entity.OrganizationEntityAction;
import com.csosm.module.base.entity.StoreEntity;
import com.csosm.module.base.entity.StoreEntityAction;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.legooframework.model.rfm.entity.RFM4OrgEntity;
import com.legooframework.model.rfm.entity.RFM4OrgEntityAction;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class RFM4OrgService extends AbstractBaseServer {

    final static int REWRITE_OFF = 0;
    final static int REWRITE_ALL = 1;
    final static int REWRITE_NOT_EXITS = 2;

    public void savaOrUpdateCompanyRFM(LoginUserContext user, OrganizationEntity organization,
                                       int valType,
                                       int r1, int r2, int r3, int r4,
                                       int f1, int f2, int f3, int f4,
                                       int m1, int m2, int m3, int m4, int rewriteType) {
        Preconditions.checkNotNull(organization, "入参 organization 不可以为空值...");
        Preconditions.checkState(rewriteType == REWRITE_OFF || REWRITE_ALL == rewriteType
                        || rewriteType == REWRITE_NOT_EXITS, "非法的 rewriteType 取值 %s,取值范围[%s,%s,%s]",
                rewriteType, REWRITE_OFF, REWRITE_ALL, REWRITE_NOT_EXITS);
        Optional<OrganizationEntity> company = organization.isCompany() ? Optional.of(organization) :
                getBean(OrganizationEntityAction.class).findCompanyById(organization.getMyCompanyId());
        Preconditions.checkState(company.isPresent());
        getBean(RFM4OrgEntityAction.class).savaOrUpdateComOrOrgRFM(user, organization, valType,
                r1, r2, r3, r4,
                f1, f2, f3, f4,
                m1, m2, m3, m4);
        if (REWRITE_ALL == rewriteType) {
            Optional<List<StoreEntity>> stores = organization.isCompany() ?
                    getBean(StoreEntityAction.class).loadAllStoreByCompany(company.get()) :
                    getBean(StoreEntityAction.class).loadAllSubStoreByOrg(organization);
            if (stores.isPresent()) {
                getBean(RFM4OrgEntityAction.class).batchReWriteStoreRFM(user, valType,
                        r1, r2, r3, r4,
                        f1, f2, f3, f4,
                        m1, m2, m3, m4, stores.get());
            }
        } else if (REWRITE_NOT_EXITS == rewriteType) {
            Optional<List<StoreEntity>> stores = organization.isCompany() ?
                    getBean(StoreEntityAction.class).loadAllStoreByCompany(company.get()) :
                    getBean(StoreEntityAction.class).loadAllSubStoreByOrg(organization);
            if (!stores.isPresent()) return;
            java.util.Optional<List<RFM4OrgEntity>> exits = getBean(RFM4OrgEntityAction.class)
                    .loadAllStoreRFM(company.get());
            if (exits.isPresent()) {
                Set<Integer> exitIds = exits.get().stream().map(RFM4OrgEntity::getStoreId).collect(Collectors.toSet());
                List<StoreEntity> subStores = stores.get().stream().filter(x -> !exitIds.contains(x.getId()))
                        .collect(Collectors.toList());
                if (CollectionUtils.isEmpty(subStores)) return;
                getBean(RFM4OrgEntityAction.class).batchReWriteStoreRFM(user, valType,
                        r1, r2, r3, r4,
                        f1, f2, f3, f4,
                        m1, m2, m3, m4, subStores);
            } else {
                getBean(RFM4OrgEntityAction.class).batchReWriteStoreRFM(user, valType,
                        r1, r2, r3, r4,
                        f1, f2, f3, f4,
                        m1, m2, m3, m4, stores.get());
            }
        }
    }
}
