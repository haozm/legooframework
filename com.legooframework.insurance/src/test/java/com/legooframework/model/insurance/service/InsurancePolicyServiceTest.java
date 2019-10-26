package com.legooframework.model.insurance.service;

import com.google.common.collect.Lists;
import com.legooframework.model.core.base.runtime.LoginContextHolder;
import com.legooframework.model.insurance.entity.MemberEntity;
import com.legooframework.model.insurance.entity.MemberEntityAction;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.ResourceUtils;

import java.util.Date;
import java.util.Optional;

import static org.junit.Assert.*;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
        locations = {ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/junit/spring-insurance-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/core/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/insurance/spring-model-cfg.xml"}
)
public class InsurancePolicyServiceTest {

    @Test
    public void insuredSelf() {
        LoginContextHolder.setAnonymousCtx();
        String dis = "-1925558636,-1833103885,-1825590392,-1767664305,-1754408031,-1725938636,-1713421155,-1693148103,-1652258493,-1644146668,-1638894493,-1625902961,-1619297165,-1616273195,-1590704246,-1576529837,-1575117103,-1498631588,-1489030290,-1463982617,-1459812067,-1398354462,-1362676306,-1328764839";
        String[] args = StringUtils.split(dis, ',');
        for (int i = 0; i < args.length; i++) {
            Optional<MemberEntity> member = memberEntityAction.findById(Integer.valueOf(args[i]));
            insurancePolicyService.insuredSelf(member.get(), "PX020301992301-" + i, new Date(),
                    "01", 300000.00D, "GFYH", "234856123123859-" + i,
                    Lists.newArrayList("00002,2000000.00,1",
                            "00004,20000.00,0"), "颤三", "nihao");
        }

    }

    @Test
    public void insured() {
        LoginContextHolder.setAnonymousCtx();
        String asd = "-421396164,-406951355,-388458498,-354197624,-341667733,-311459444," +
                "-301837312,-243765800,-199502748,-192037656,-130222243,-128289443,-126342609,-98453607,-55629743,-49027000," +
                "16267147,61880574,109130380,141349807,153780687,194271907,219052874,248190956,412479545,459460119,485828481," +
                "485965425,494460053,499001416,583413882,639538494,644679381,675530713,680693531,732856084,733616657,742622038," +
                "788773903,850768052,949591745,953510583,953992033,972744170,975215270,983979874,1033091257,1034122363,1040326764," +
                "1121175813,1145211631,1185473621,1189078782,1210320912,1211742893,1325916040,1333846462,1375612518,1408974047," +
                "1411295545,1474251741,1491741796,1492991955,1517764089,1633992875,1640124565,1767766387,1774344337,1907413149," +
                "1920571804,1941842719,1961797308,1995252849,2003830515,2030269313,2088077546,2098878136";
        String[] args = StringUtils.split(asd, ',');
        int size = args.length;

        for (int a = 0; a < 60; a++) {
            int i = RandomUtils.nextInt(0, size);
            Optional<MemberEntity> member = memberEntityAction.findById(args[i]);
            Optional<MemberEntity> membe2r = memberEntityAction.findById(args[i + 1]);
            insurancePolicyService.insured(member.get(), membe2r.get(), "PX99120301002-" + a, new Date(),
                    "01", 300000.00D, "GFYH", "23485612312381235-" + a, "AC",
                    Lists.newArrayList("00002,2000000.00,1",
                            "00004,20000.00,0"), "hebing ", "nihao");
        }

    }


    @Autowired
    InsurancePolicyService insurancePolicyService;
    @Autowired
    MemberEntityAction memberEntityAction;
}