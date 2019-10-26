package com.legooframework.junit.ewei;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ResourceUtils;

import javax.annotation.Resource;
import java.io.File;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
        locations = {ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/junit/spring-ewei-cfg.xml"}
)
public class DataChangeListener {
    @Test
    public void change1() throws Exception {
        System.out.println(Long.MAX_VALUE);
    }

    @Test
    public void change() throws Exception {
        String prefix = "demo_qiweido.";
        File table_file = ResourceUtils.getFile(ResourceUtils.CLASSPATH_URL_PREFIX + "com\\legooframework\\junit\\ewei\\tables.txt");
        List<String> tables = Files.readLines(table_file, Charsets.UTF_8);
        List<Object[]> args = Lists.newArrayListWithCapacity(tables.size());
        if (!CollectionUtils.isEmpty(tables)) {
            tables.forEach(x -> {
                String sql = String.format("SELECT COUNT(*) FROM demo_qiweido.%s ", x);
                long count = eweiJdbcTemplate.queryForObject(sql, Long.class);
                args.add(new Object[]{x, count});
            });
        }
        String insert_sql = "INSERT INTO CSOSM_CRM_DB.EWEI_COUNT_CHANGE (id, count_01, count_02) VALUES(?, ?, 0) ";
        localJdbcTemplate.batchUpdate(insert_sql, args, args.size(), (ps, t) -> {
            ps.setObject(1, t[0]);
            ps.setObject(2, t[1]);
        });
    }

    @Test
    public void update() throws Exception {
        String prefix = "demo_qiweido.";
        File table_file = ResourceUtils.getFile(ResourceUtils.CLASSPATH_URL_PREFIX + "com\\legooframework\\junit\\ewei\\tables.txt");
        List<String> tables = Files.readLines(table_file, Charsets.UTF_8);
        List<Object[]> args = Lists.newArrayListWithCapacity(tables.size());
        if (!CollectionUtils.isEmpty(tables)) {
            tables.forEach(x -> {
                if (StringUtils.startsWith(x, "ims_ewei_")) {
                    String sql = String.format("SELECT COUNT(*) FROM demo_qiweido.%s ", x);
                    System.out.println(sql);
                    long count = eweiJdbcTemplate.queryForObject(sql, Long.class);
                    args.add(new Object[]{x, count});
                }
            });
        }
        String insert_sql = "UPDATE CSOSM_CRM_DB.EWEI_COUNT_CHANGE SET  count_01 = ? WHERE id = ?";
        localJdbcTemplate.batchUpdate(insert_sql, args, args.size(), (ps, t) -> {
            ps.setObject(1, t[1]);
            ps.setObject(2, t[0]);
        });
    }

    @Resource(name = "eweiJdbcTemplate")
    private JdbcTemplate eweiJdbcTemplate;

    @Resource(name = "localJdbcTemplate")
    private JdbcTemplate localJdbcTemplate;

}
