package com.legooframework.model.takecare.entity;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.legooframework.model.core.base.runtime.LoginContextHolder;
import com.legooframework.model.core.jdbc.JdbcQuerySupport;
import com.legooframework.model.core.jdbc.PagingResult;
import com.legooframework.model.core.jdbc.sqlengine.SQLStatementFactory;
import com.legooframework.model.core.utils.DateTimeUtils;
import com.legooframework.model.covariant.entity.SendChannel;
import com.legooframework.model.covariant.service.CovariantService;
import com.legooframework.model.covariant.service.MemberAgg;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.ResourceUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
        locations = {ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/junit/spring-acp-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/core/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/covariant/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/takecare/spring-model-cfg.xml"}
)
public class BirthdayCareEntityActionTest {

    @Before
    public void setUp() throws Exception {
        LoginContextHolder.setAnonymousCtx();
    }


    @Test
    public void callDuty() {
        Map<String, Object> params = Maps.newHashMap();
        Optional<List<Map<String, Object>>> list = querySupport.queryForList("CareBirthdayEntity", "birthday_care_count", params);
        System.out.println(list.orElse(null));
    }

    @Test
    public void careMember4ThisYear() {
        MemberAgg agg = covariantService.loadMemberAgg(342);
        CareBirthdayAgg careAgg = birthdayCareEntityAction.careMember4ThisYear(null, agg,
                Lists.newArrayList(SendChannel.SMS, SendChannel.WECHAT), "你妈妈含你回家吃饭了", null);
        if (careAgg.hasSavedCare()) {
            if (!careAgg.hasCareError()) {
                takeCareRecordEntityAction.batchInsert(careAgg.getTakeCareRecords());
            }
        } else {
            birthdayCareEntityAction.singleInsertCare(careAgg.getBirthdayCare());
            if (!careAgg.hasCareError()) {
                takeCareRecordEntityAction.batchInsert(careAgg.getTakeCareRecords());
            }
        }
    }

    @Test
    public void createSql() {
        Map<String, Object> params = Maps.newHashMap();
        params.putAll(DateTimeUtils.wrappDatePeriod("2018-11-18", "2018-12-11"));
        params.put("USER_STORE_ID", 1120);
        params.put("careStatus", 2);
        params.put("condition", true);
        //  String sql = sqlStatementFactory.getExecSql("CareBirthdayEntity", "MemberBirthdayCare", param);

        PagingResult pg = querySupport.queryForPage("CareBirthdayEntity", "MemberBirthdayCare",
                1, 20, params);
        System.out.println(pg.getCount());
    }

    @Resource(name = "takeCareQueryFactory")
    private SQLStatementFactory sqlStatementFactory;

    @Resource(name = "takeCareJdbcQuerySupport")
    private JdbcQuerySupport querySupport;

    @Autowired
    private CovariantService covariantService;
    @Autowired
    private CareBirthdayEntityAction birthdayCareEntityAction;
    @Autowired
    private CareRecordEntityAction takeCareRecordEntityAction;
}