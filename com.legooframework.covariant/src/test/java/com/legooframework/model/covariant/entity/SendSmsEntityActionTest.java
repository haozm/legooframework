package com.legooframework.model.covariant.entity;

import com.google.common.collect.Lists;
import com.legooframework.model.core.base.runtime.LoginContextHolder;
import org.junit.Before;
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
        locations = {ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/junit/spring-acp-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/core/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/covariant/spring-model-cfg.xml"}
)
public class SendSmsEntityActionTest {

    @Before
    public void init() {
        LoginContextHolder.setAnonymousCtx();
    }


    @Test
    public void getSmsPrefix() {
        StoEntity store = stoEntityAction.loadById(1120);
        System.out.println(sendSmsEntityAction.getSmsPrefix(store));
        System.out.println(sendSmsEntityAction.getSmsPrefix(store));
    }

    @Test
    public void batchAdd4Send() {
        StoEntity store = stoEntityAction.loadById(1120);
        Optional<List<MemberEntity>> mmes = memberEntityAction.findByStore(store);
        if (mmes.isPresent()) {
            List<SendSmsEntity> smses = Lists.newArrayList();
            int i = 0;
            for (MemberEntity mm : mmes.get()) {
                if (i % 4 == 0) {
                    SendSmsEntity sms = SendSmsEntity.createSmsByMember("你妈妈喊你回家吃饭啦", mm,
                            "112233454", BusinessType.BIRTHDAYCARE, "手机欠费");
                    smses.add(sms);
                } else {
                    SendSmsEntity sms = SendSmsEntity.createSmsByMember("你妈妈喊你回家吃饭啦", mm, "112233454",
                            BusinessType.BIRTHDAYCARE, null);
                    smses.add(sms);
                }

                i++;
            }
            sendSmsEntityAction.batchAdd4Send(smses);
        }
    }

    @Autowired
    private StoEntityAction stoEntityAction;
    @Autowired
    MemberEntityAction memberEntityAction;
    @Autowired
    private SendSmsEntityAction sendSmsEntityAction;
}
