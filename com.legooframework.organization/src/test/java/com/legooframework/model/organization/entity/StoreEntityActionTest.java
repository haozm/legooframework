package com.legooframework.model.organization.entity;

import com.google.common.base.Preconditions;
import com.legooframework.model.core.base.runtime.LoginContextHolder;
import com.legooframework.model.core.event.MessageGateWay;
import com.legooframework.model.core.osgi.Bundle;
import com.legooframework.model.dict.dto.KvDictDto;
import com.legooframework.model.dict.dto.KvTypeDictDto;
import com.legooframework.model.dict.event.DictEventFactory;
import com.legooframework.model.organization.LoginContextTest;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.ResourceUtils;

import java.util.Optional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
        locations = {ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/junit/spring-db-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/base/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/integration/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/dict/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/organization/spring-model-cfg.xml"}
)
public class StoreEntityActionTest {

    @Test
    public void insert() throws Exception {
        LoginContextHolder.setCtx(new LoginContextTest());
        String type = "0000";
        Optional<KvTypeDictDto> type_opt = messageGateWay
                .sendAndReceive(DictEventFactory.loadDictByTypeEvent(bundle, StoreEntity.TYPE_DICT), KvTypeDictDto.class);
        Preconditions.checkState(type_opt.isPresent());
        Optional<KvDictDto> type_dict = type_opt.get().valueOf(type);
        Preconditions.checkState(type_dict.isPresent());
        Optional<CompanyEntity> company = companyEntityAction.findById(100000000L);
        Preconditions.checkState(company.isPresent());
        for (int i = 15; i < 28; i++) {
            storeEntityAction.insert("STORE-GHOST-00" + i, "NCE分店-" + i,
                    "NCE店", null, "天上的NCE", "NCE奥",
                    "13708231123", "不错的商店NCE，就是消费太贵", type_dict.get());
        }

    }

    @Test
    public void findById() throws Exception {
        LoginContextHolder.setCtx(new LoginContextTest());
        Optional<StoreEntity> storeEntity = storeEntityAction.findById(100000009L);
        Assert.assertTrue(storeEntity.isPresent());
        System.out.println(storeEntity.get());
    }
    //unBindingByDevice

    @Test
    public void bindingDeviceToStore() throws Exception {
        LoginContextHolder.setCtx(new LoginContextTest());
        Optional<EquipmentEntity> equipment = equipmentEntityAction.findById("A52001C69F01A81");
        Preconditions.checkState(equipment.isPresent());
        storeEntityAction.bindingDeviceToStore(2932L, equipment.get());
    }


    @Test
    public void unBindingByDevice() throws Exception {
        LoginContextHolder.setCtx(new LoginContextTest());
        Optional<EquipmentEntity> equipment = equipmentEntityAction.findById("A52001C69F01A81");
        storeEntityAction.unBindingByDevice(equipment.get());
    }

    @Autowired
    private EquipmentEntityAction equipmentEntityAction;

    @Autowired
    private StoreEntityAction storeEntityAction;
    @Autowired
    private CompanyEntityAction companyEntityAction;
    @Autowired
    @Qualifier(value = "organization")
    private Bundle bundle;
    @Autowired
    private MessageGateWay messageGateWay;
}