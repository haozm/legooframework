package com.legooframework.model.crmadapter.entity;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.ResourceUtils;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
        locations = {ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/junit/spring-membercare-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/core/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/crmadapter/spring-model-cfg.xml"}
)
public class MsgReplaceParamActionTest {

    @Test
    public void formatMemberTemplate() {
       // replaceParamAction.formatMemberTemplate(1, 2, "1,2,3,4,5,6,7,8,9,10", "你好，今天是你的生日，{会员姓名}回家吃饭{店长}回复TD退订", false);
    }

    @Autowired
    private MsgTemplateProxyAction replaceParamAction;

}