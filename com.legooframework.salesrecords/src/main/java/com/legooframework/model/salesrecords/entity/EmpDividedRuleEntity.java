package com.legooframework.model.salesrecords.entity;

import com.google.common.base.Joiner;
import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.legooframework.model.core.base.entity.BaseEntity;
import com.legooframework.model.core.jdbc.BatchSetter;
import com.legooframework.model.covariant.entity.OrgEntity;
import com.legooframework.model.covariant.entity.StoEntity;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EmpDividedRuleEntity extends BaseEntity<Integer> implements BatchSetter {

    private final static Comparator<List<Divided>> COMPARABLE_LIST = Comparator.comparingInt(List::size);
    private final static Comparator<Divided> COMPARABLE_SINGLE =
            Comparator.comparingInt((ToIntFunction<Divided>) val -> val.type == 1 ? 0 : 1).thenComparingDouble(o -> o.value);

    private Integer companyId, storeId;
    private boolean autoRun;
    private List<List<Divided>> memberRule;
    private List<List<Divided>> noMemberRule;
    private List<List<Divided>> crossMemberRule;
    private List<List<Divided>> crossNoMemberRule;

    boolean isCompany() {
        return this.storeId == 0;
    }

    List<List<Divided>> getMemberRule() {
        return memberRule;
    }

    List<List<Divided>> getNoMemberRule() {
        return noMemberRule;
    }

    List<List<Divided>> getCrossMemberRule() {
        return crossMemberRule;
    }

    List<List<Divided>> getCrossNoMemberRule() {
        return crossNoMemberRule;
    }

    boolean isCompany(OrgEntity company) {
        return this.companyId.equals(company.getId());
    }

    boolean isAutoRun() {
        return autoRun;
    }

    boolean isOnlyCompany(StoEntity store) {
        return isCompany() && this.companyId.equals(store.getCompanyId());
    }

    boolean isStore(StoEntity store) {
        return this.companyId.equals(store.getCompanyId()) && this.storeId.equals(store.getId());
    }

    private EmpDividedRuleEntity(Integer companyId, Integer storeId, boolean autoRun, List<List<Divided>> memberRule,
                                 List<List<Divided>> noMemberRule, List<List<Divided>> crossMemberRule,
                                 List<List<Divided>> crossNoMemberRule) {
        super(0);
        this.companyId = companyId;
        this.storeId = storeId;
        this.autoRun = autoRun;
        this.memberRule = memberRule;
        sortAndCheck(this.memberRule, 1);
        this.noMemberRule = noMemberRule;
        sortAndCheck(this.noMemberRule, 2);
        this.crossMemberRule = crossMemberRule;
        sortAndCheck(this.crossMemberRule, 1);
        this.crossNoMemberRule = crossNoMemberRule;
        sortAndCheck(this.crossNoMemberRule, 2);
    }

    EmpDividedRuleEntity(Integer id, ResultSet resultSet) throws RuntimeException {
        super(id);
        try {
            this.companyId = resultSet.getInt("company_id");
            this.storeId = resultSet.getInt("store_id");
            this.autoRun = resultSet.getInt("auto_run") == 1;
            this.memberRule = reduction4Save(resultSet.getString("member_rule"));
            this.noMemberRule = reduction4Save(resultSet.getString("no_member_rule"));
            if (this.storeId != 0) {
                this.crossMemberRule = null;
                this.crossNoMemberRule = null;
            } else {
                this.crossMemberRule = reduction4Save(resultSet.getString("crs_member_rule"));
                this.crossNoMemberRule = reduction4Save(resultSet.getString("crs_no_member_rule"));
            }
        } catch (SQLException e) {
            throw new RuntimeException("还原对象 EmpDividedRuleEntity 发生异常", e);
        }
    }


    @Override
    public void setValues(PreparedStatement ps) throws SQLException {
        //  company_id, store_id, member_rule, no_member_rule, crs_member_rule, crs_no_member_rule, auto_run,  tenant_id
        Joiner joiner = Joiner.on('$');
        Joiner sub_joiner = Joiner.on('#');
        ps.setObject(1, companyId);
        ps.setObject(2, storeId == null ? 0 : storeId);
        ps.setObject(3, toString4Save(memberRule, sub_joiner, joiner));
        ps.setObject(4, toString4Save(noMemberRule, sub_joiner, joiner));
        ps.setObject(5, toString4Save(crossMemberRule, sub_joiner, joiner));
        ps.setObject(6, toString4Save(crossNoMemberRule, sub_joiner, joiner));
        ps.setObject(7, this.autoRun ? 1 : 0);
        ps.setObject(8, this.companyId);
    }

    private void sortAndCheck(List<List<Divided>> rules, int type) {
        if (CollectionUtils.isNotEmpty(rules)) {
            if (type == 1) {
                rules.forEach($it -> $it.forEach(x -> {
                    List<Divided> _list = $it.stream().filter(dd -> dd.type == 1).collect(Collectors.toList());
                    Preconditions.checkArgument(CollectionUtils.isNotEmpty(_list) && _list.size() == 1, "非法的规则设定，存在多个服务导购....");
                }));
            } else if (type == 2) {
                rules.forEach($it -> $it.forEach(x -> {
                    List<Divided> list = $it.stream().filter(dd -> dd.type == 1).collect(Collectors.toList());
                    Preconditions.checkArgument(CollectionUtils.isEmpty(list), "非法的规则设定，非会员单存在服务导购设定....");
                }));
            }
            rules.forEach($it -> {
                double sum = $it.stream().mapToDouble(Divided::getValue).sum();
                Preconditions.checkArgument(sum <= 1.0, "分成比例规则总和需小于等于100%");
            });
            rules.forEach(x -> x.sort(COMPARABLE_SINGLE));
            rules.sort(COMPARABLE_LIST);
        }
    }

    static EmpDividedRuleEntity createByStore(StoEntity store, boolean autoRun, List<List<Divided>> memberRule,
                                              List<List<Divided>> noMemberRule) {
        return new EmpDividedRuleEntity(store.getCompanyId(), store.getId(), autoRun, memberRule, noMemberRule, null, null);
    }

    static EmpDividedRuleEntity createByCompany(OrgEntity company, boolean autoRun, List<List<Divided>> memberRule,
                                                List<List<Divided>> noMemberRule, List<List<Divided>> crossMemberRule,
                                                List<List<Divided>> crossNoMemberRule) {
        return new EmpDividedRuleEntity(company.getId(), null, autoRun, memberRule, noMemberRule,
                crossMemberRule, crossNoMemberRule);
    }

    @Override
    public Map<String, Object> toParamMap(String... excludes) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("companyId", companyId);
        params.put("storeId", storeId == null ? 0 : storeId);
        params.put("autoRun", autoRun ? 1 : 0);
        Joiner joiner = Joiner.on('$');
        Joiner sub_joiner = Joiner.on('#');
        params.put("memberRule", toString4Save(memberRule, sub_joiner, joiner));
        params.put("noMemberRule", toString4Save(noMemberRule, sub_joiner, joiner));
        params.put("crossMemberRule", toString4Save(crossMemberRule, sub_joiner, joiner));
        params.put("crossNoMemberRule", toString4Save(crossNoMemberRule, sub_joiner, joiner));
        return params;
    }

    private String toString4Save(List<List<Divided>> rules, Joiner sub_joiner, Joiner joiner) {
        if (CollectionUtils.isEmpty(rules)) return null;
        return joiner.join(rules.stream().map(sub_joiner::join).collect(Collectors.toList()));
    }

    private List<List<Divided>> reduction4Save(String values) {
        if (Strings.isNullOrEmpty(values)) return null;
        List<List<Divided>> res_list = Lists.newArrayList();
        String[] sub_list = StringUtils.split(values, '$');
        for (String $it : sub_list) {
            res_list.add(Stream.of(StringUtils.split($it, '#')).map(Divided::new).collect(Collectors.toList()));
        }
        return res_list;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("companyId", companyId)
                .add("storeId", storeId)
                .add("autoRun", autoRun)
                .add("memberRule", memberRule)
                .add("noMemberRule", noMemberRule)
                .add("crossMemberRule", crossMemberRule)
                .add("crossNoMemberRule", crossNoMemberRule)
                .toString();
    }

    public static class Divided {
        private final int type; //  1  服务导购  2 销售导购
        private final double value; // 分成比例

        private Divided(int type, double value) {
            this.type = type;
            Preconditions.checkArgument(value > 0.0 && value <= 1, "分成比例非法 value=%s", value);
            this.value = value;
        }

        Divided(String str) {
            String[] args = StringUtils.split(str, ',');
            this.type = Integer.parseInt(args[0]);
            this.value = Double.parseDouble(args[1]);
        }

        int getType() {
            return type;
        }

        boolean isSvrEmp() {
            return this.type == 1;
        }

        double allocation(double total) {
            return total * value;
        }

        double getValue() {
            return value;
        }

        static Divided serviceEmp(double divided) {
            return new Divided(1, divided);
        }

        static Divided saledEmp(double divided) {
            return new Divided(2, divided);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Divided that = (Divided) o;
            return type == that.type &&
                    Double.compare(that.value, value) == 0;
        }

        @Override
        public int hashCode() {
            return Objects.hash(type, value);
        }

        @Override
        public String toString() {
            return String.format("%s,%s", type, value);
        }
    }

}
