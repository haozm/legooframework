package com.legooframework.model.reactor.entity;

import com.google.common.base.Joiner;
import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.legooframework.model.core.base.entity.BaseEntity;
import com.legooframework.model.core.jdbc.ResultSetUtil;
import org.apache.commons.lang3.ArrayUtils;
import org.joda.time.LocalDateTime;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class RetailFactEntity extends BaseEntity<Long> {

    private final String retailId, vipId, vipName, birthday, phone, sex, oldStoreId, vipType, employeeIds, companypy, fmtCreatetime;
    private final LocalDateTime createCardTime, createtime;
    private final List<SaleGoods> saleGoods;
    private final Integer storeId, companyId, employeeId;
    private final double integral, addIntegral, reserve;
    private final Member member;

    RetailFactEntity(Long id, LocalDateTime createtime, List<SaleGoods> saleGoods, ResultSet res) {
        super(id, createtime);
        try {
            this.retailId = ResultSetUtil.getOptString(res, "retailid", null);
            this.vipId = ResultSetUtil.getOptString(res, "vipid", null);
            this.vipName = ResultSetUtil.getOptString(res, "vipname", null);
            this.birthday = ResultSetUtil.getOptString(res, "birthday", null);
            this.phone = ResultSetUtil.getOptString(res, "phone", null);
            this.sex = ResultSetUtil.getOptString(res, "sex", null);
            this.oldStoreId = ResultSetUtil.getOptString(res, "old_store_id", null);
            this.vipType = ResultSetUtil.getOptString(res, "viptype", null);
            this.saleGoods = Lists.newArrayList(saleGoods);
            this.integral = ResultSetUtil.getOptObject(res, "integral", BigDecimal.class).orElse(new BigDecimal(0.0D)).doubleValue();
            this.addIntegral = ResultSetUtil.getOptObject(res, "addIntegral", BigDecimal.class).orElse(new BigDecimal(0.0D)).doubleValue();
            this.reserve = ResultSetUtil.getOptObject(res, "reserve", BigDecimal.class).orElse(new BigDecimal(0.0D)).doubleValue();
            this.companypy = ResultSetUtil.getOptString(res, "companypy", null);
            this.employeeIds = ResultSetUtil.getOptString(res, "employeeids", null);
            this.createCardTime = ResultSetUtil.getLocalDateTime(res, "createcardtime");
            this.createtime = ResultSetUtil.getLocalDateTime(res, "createtime");
            this.fmtCreatetime = ResultSetUtil.getOptString(res, "fmtCreatetime", null);
            this.companyId = ResultSetUtil.getOptObject(res, "companyId", Integer.class).orElse(null);
            this.storeId = ResultSetUtil.getOptObject(res, "storeId", Integer.class).orElse(null);
            Integer memberId = ResultSetUtil.getOptObject(res, "memberId", Integer.class).orElse(-1);
            if (memberId == -1) {
                this.member = null;
            } else {
                this.member = new Member(res);
            }
            this.employeeId = ResultSetUtil.getOptObject(res, "employeeId", Integer.class).orElse(null);
        } catch (SQLException e) {
            throw new RuntimeException("Restore RetailFactEntity has SQLException", e);
        }
    }

    Map<String, Object> toReplaceMap() {
        DecimalFormat format_money = new DecimalFormat("#,###.00");
        Map<String, Object> params = Maps.newHashMap();
        params.put("会员姓名", Strings.nullToEmpty(this.vipName));
        params.put("会员电话", Strings.nullToEmpty(this.phone));
        params.put("会员生日", Strings.nullToEmpty(this.birthday));
        params.put("开卡日期", this.createCardTime == null ? "----" : this.createCardTime.toString("yyyy-MM-dd"));
        params.put("当前积分", this.integral);
        if (addIntegral >= 0.0) {
            params.put("积分变化", String.format("新增积分 %s", this.addIntegral));
        } else {
            params.put("积分变化", String.format("扣除积分 %s", Math.abs(this.addIntegral)));
        }
        params.put("会员等级", this.vipType == null ? "--" : vipType);
        params.put("消费日期", fmtCreatetime == null ? "--" : fmtCreatetime);
        List<String> details = saleGoods.stream().map(SaleGoods::toReplaceStr).collect(Collectors.toList());
        params.put("本次消费明细", Joiner.on("; ").join(details));
        double total_price = saleGoods.stream().mapToDouble(SaleGoods::getSalePrice).sum();
        params.put("本次消费总额", format_money.format(total_price));
        int goodNums = saleGoods.stream().mapToInt(SaleGoods::getGoodNum).sum();
        params.put("本次消费数量", goodNums);
        params.put("会员卡号", this.member == null ? "" : Strings.nullToEmpty(this.member.cardNum));
        return params;
    }

    Integer getStoreId() {
        return storeId;
    }

    Optional<Integer> getEmployeeId() {
        return Optional.ofNullable(employeeId);
    }

    Integer getCompanyId() {
        return companyId;
    }

    Optional<String> getPhone() {
        return Optional.ofNullable(phone);
    }

    Optional<String> getVipName() {
        return Optional.ofNullable(vipName);
    }

    LocalDateTime getCreatetime() {
        return createtime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        RetailFactEntity that = (RetailFactEntity) o;
        return Objects.equals(getId(), that.getId()) &&
                Objects.equals(retailId, that.retailId) &&
                Objects.equals(vipId, that.vipId) &&
                Objects.equals(vipName, that.vipName) &&
                Objects.equals(birthday, that.birthday) &&
                Objects.equals(phone, that.phone) &&
                Objects.equals(sex, that.sex) &&
                Objects.equals(oldStoreId, that.oldStoreId) &&
                Objects.equals(vipType, that.vipType) &&
                Objects.equals(employeeIds, that.employeeIds) &&
                Objects.equals(companypy, that.companypy) &&
                Objects.equals(createCardTime, that.createCardTime) &&
                Objects.equals(integral, that.integral) &&
                Objects.equals(companyId, that.companyId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), retailId, vipId, vipName, birthday, phone, sex, oldStoreId, vipType,
                employeeIds, companypy, createCardTime, integral, companyId);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", getId())
                .add("companyId", companyId)
                .add("storeId", storeId)
                .add("employeeId", employeeId)
                .add("retailId", retailId)
                .add("vipId", vipId)
                .add("vipName", vipName)
                .add("birthday", birthday)
                .add("phone", phone)
                .add("sex", sex)
                .add("oldStoreId", oldStoreId)
                .add("vipType", vipType)
                .add("employeeIds", employeeIds)
                .add("companypy", companypy)
                .add("createCardTime", createCardTime)
                .add("integral", integral)
                .add("SaleGoods'size ", saleGoods.size())
                .add("Member ", this.member)
                .toString();
    }

    static class Member {
        private final int id;
        private final String name, phone, cardNum;

        Member(ResultSet res) throws SQLException {
            this.id = res.getInt("memberId");
            this.cardNum = ResultSetUtil.getOptString(res, "memberCardNum", null);
            this.name = ResultSetUtil.getOptString(res, "vipname", null);
            this.phone = ResultSetUtil.getOptString(res, "phone", null);
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("id", id)
                    .add("name", name)
                    .add("cardNum", cardNum)
                    .add("phone", phone)
                    .toString();
        }
    }

    static class SaleGoods {
        private final long id;
        private final double cardPrice, salePrice;
        private final int goodNum;
        private final String goodsName, goodsId;

        int getGoodNum() {
            return goodNum;
        }

        // crt.id, '##', crt.goodsid, '##', crt.saleprice, '##', crt.cardprice , '##' , crt.goodnum, '##' , crt.goodname
        SaleGoods(String[] args) {
            Preconditions.checkArgument(ArrayUtils.isNotEmpty(args) && args.length == 6);
            this.id = Long.parseLong(args[0]);
            this.goodsId = args[1];
            this.salePrice = Double.parseDouble(args[2]);
            this.cardPrice = Double.parseDouble(args[3]);
            this.goodNum = Integer.parseInt(args[4]);
            this.goodsName = args[5];
        }

        double getSalePrice() {
            return salePrice;
        }

        String toReplaceStr() {
            return String.format("%s*%s，价格:￥%s", goodsName, goodNum, (salePrice * goodNum));
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("id", id)
                    .add("goodsId", goodsId)
                    .add("cardPrice", cardPrice)
                    .add("salePrice", salePrice)
                    .add("goodNum", goodNum)
                    .add("goodsName", goodsName)
                    .toString();
        }
    }
}
