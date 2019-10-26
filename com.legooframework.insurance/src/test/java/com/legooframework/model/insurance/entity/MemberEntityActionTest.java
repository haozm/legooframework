package com.legooframework.model.insurance.entity;

import com.legooframework.model.core.base.runtime.LoginContextHolder;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDate;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.ResourceUtils;

import java.util.Optional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
        locations = {ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/junit/spring-insurance-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/core/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/insurance/spring-model-cfg.xml"}
)
public class MemberEntityActionTest {

    @Test
    public void insert() {
        LoginContextHolder.setAnonymousCtx();
        String asd = "祥羽,萧扬,益晨,辰宏,俊钰,佳义";
        String[] stir = StringUtils.split(asd, ',');
        for (int i = 0; i < stir.length; i++) {
            MemberEntity mm = memberEntityAction.insert(stir[i], "14781122323189403" + i, null, "1823423123" + i, LocalDate.now(), 1,
                    4, 170, 74, stir[i], stir[i]);
        }
    }

    @Test
    public void change() {
        LoginContextHolder.setAnonymousCtx();
        String ids = "-2146786797,-2094836954,-2028538269,-1925558636,-1833103885,-1825590392,-1767664305,-1754408031,-1725938636,-1713421155,-1693148103,-1652258493,-1644146668,-1638894493,-1625902961,-1619297165,-1616273195,-1590704246,-1576529837,-1575117103,-1498631588,-1489030290,-1463982617,-1459812067,-1398354462,-1362676306,-1328764839,-1326089376,-1296594121,-1289756391,-1286164944,-1277302559,-1265571672,-1247572292,-1176603446,-1175250427,-1148139288,-1122774777,-1117213486,-1092665338,-1090606102,-1049907810,-1014997115,-1001158391,-1000372342,-971816623,-945931723,-935536510,-916837125,-899300817,-895250198,-809644258,-783135090,-759560509,-754629620,-753584612,-748888611,-741353214,-678935563,-638098858,-543371699,-513420956,-494889463,-456682227,-421396164,-406951355,-388458498,-354197624,-341667733,-311459444,-301837312,-243765800,-199502748,-192037656,-130222243,-128289443,-126342609,-98453607,-55629743,-49027000,16267147,61880574,109130380,141349807,153780687,194271907,219052874,248190956,412479545,459460119,485828481,485965425,494460053,499001416,583413882,639538494,644679381,675530713,680693531,732856084,733616657,742622038,788773903,850768052,949591745,953510583,953992033,972744170,975215270,983979874,1033091257,1034122363,1040326764,1121175813,1145211631,1185473621,1189078782,1210320912,1211742893,1325916040,1333846462,1375612518,1408974047,1411295545,1474251741,1491741796,1492991955,1517764089,1633992875,1640124565,1767766387,1774344337,1907413149,1920571804,1941842719,1961797308,1995252849,2003830515,2030269313,2088077546,2098878136,2108408948,2114626908,2131299021";
        String[] idd = StringUtils.split(ids, ',');
        for (int i = 0; i < idd.length; i++) {
            Optional<MemberEntity> mm = memberEntityAction.findById(Integer.valueOf(idd[i]));
            LocalDate date = new LocalDate(1981, 1, 1);
            mm.ifPresent(m -> {
                LocalDate date1 = date.plusDays(RandomUtils.nextInt(10, 10000));
                memberEntityAction.change(m.getId(), m.getName(), "1422101" + RandomUtils.nextInt(100000, 999999) + "102410",
                        m.getPhone(), "1868" + RandomUtils.nextInt(10000, 99999) + "83", date1,
                        RandomUtils.nextInt(0, 10) % 3, m.getEducationType(), m.getHeight(), m.getWeight(), m.getFamilyAddr(), m.getWorkAddr());
            });
        }
    }

    @Test
    public void findById() {
        LoginContextHolder.setAnonymousCtx();
        Optional<MemberEntity> res = memberEntityAction.findById(1034122363);
        System.out.println(res.get());
    }

    @Autowired
    MemberEntityAction memberEntityAction;

}