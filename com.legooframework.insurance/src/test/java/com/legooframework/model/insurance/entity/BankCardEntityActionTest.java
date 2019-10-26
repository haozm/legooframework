package com.legooframework.model.insurance.entity;

import com.legooframework.model.core.base.runtime.LoginContextHolder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.ResourceUtils;

import java.util.List;
import java.util.Optional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
        locations = {ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/junit/spring-insurance-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/core/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/insurance/spring-model-cfg.xml"}
)
public class BankCardEntityActionTest {

    @Test
    public void insert() {
        LoginContextHolder.setAnonymousCtx();
        Optional<MemberEntity> res = memberEntityAction.findById(1034122363);
        bankCardEntityAction.insert(res.get(), "RMYH", "123123123123123123123");
    }

    @Test
    public void findByMember() {
        LoginContextHolder.setAnonymousCtx();
        Optional<MemberEntity> res = memberEntityAction.findById(1034122363);
        Optional<List<BankCardEntity>> aa = bankCardEntityAction.findByMember(res.get());
        System.out.println(aa.get());
    }

    @Autowired
    BankCardEntityAction bankCardEntityAction;
    @Autowired
    MemberEntityAction memberEntityAction;
}