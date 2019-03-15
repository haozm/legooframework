package com.csosm.module.base.entity;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Objects;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;

import com.csosm.commons.entity.BaseEntityAction;
import com.csosm.commons.entity.ResultSetUtil;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

public class MemberExtraEntityAction extends BaseEntityAction<MemberExtraEntity> {

	protected MemberExtraEntityAction() {
		super("MemberEntity", null);
	}

	public Optional<MemberExtraEntity> findByMember(MemberEntity member) {
		Objects.requireNonNull(member);
		Map<String, Object> map = Maps.newHashMap();
		map.put("memberId", member.getId());
		String sql = getExecSql("findMemberExtraById", null);
		MemberExtraEntity entity = getNamedParameterJdbcTemplate().query(sql, map, getResultSetExtractor());
		if (entity == null)
			return Optional.absent();
		return Optional.of(entity);
	}

	public MemberExtraEntity loadByMember(MemberEntity member) {
		Objects.requireNonNull(member);
		Optional<MemberExtraEntity> opt = findByMember(member);
		Preconditions.checkState(opt.isPresent(), "会员扩展信息不存在");
		return opt.get();
	}
	
	public Integer saveOrUpdateMemberExtra(MemberEntity member, String jacketSize, String bottomsSize, String braSize,
			String briefsSize, String shoeSize, BigDecimal chest, BigDecimal clothingLong, BigDecimal sleeveLength,
			BigDecimal shoulder, BigDecimal waistline, BigDecimal hipline, BigDecimal thighCircumference,
			BigDecimal kneeCircumference, BigDecimal trouserLeg, BigDecimal beforeFork, BigDecimal afterFork,
			BigDecimal outseam, BigDecimal onChest, BigDecimal underChest, BigDecimal footLength) {
		Objects.requireNonNull(member);
		Optional<MemberExtraEntity> opt = findByMember(member);
		if (opt.isPresent()) {
			modifyMemberExtra(member, jacketSize, bottomsSize, braSize, briefsSize, shoeSize, chest, clothingLong,
					sleeveLength, shoulder, waistline, hipline, thighCircumference, kneeCircumference, trouserLeg,
					beforeFork, afterFork, outseam, onChest, underChest, footLength);
			return opt.get().getId();
		}
		MemberExtraEntity memberExtra = new MemberExtraEntity(member, jacketSize, bottomsSize, braSize, briefsSize,
				shoeSize, chest, clothingLong, sleeveLength, shoulder, waistline, hipline, thighCircumference,
				kneeCircumference, trouserLeg, beforeFork, afterFork, outseam, onChest, underChest, footLength, null);
		String sql = getExecSql("insertMemberExtra", null);
		getNamedParameterJdbcTemplate().update(sql, memberExtra.toMap());
		if(getCache().isPresent()) getCache().get().cleanUp();
		return memberExtra.getId();
	}
	
	public void modifyMemberExtra(MemberEntity member, String jacketSize, String bottomsSize, String braSize,
			String briefsSize, String shoeSize, BigDecimal chest, BigDecimal clothingLong, BigDecimal sleeveLength,
			BigDecimal shoulder, BigDecimal waistline, BigDecimal hipline, BigDecimal thighCircumference,
			BigDecimal kneeCircumference, BigDecimal trouserLeg, BigDecimal beforeFork, BigDecimal afterFork,
			BigDecimal outseam, BigDecimal onChest, BigDecimal underChest, BigDecimal footLength) {
		Objects.requireNonNull(member);
		MemberExtraEntity orgin = loadByMember(member);
		MemberExtraEntity clone = orgin.modify(jacketSize, bottomsSize, braSize, briefsSize, shoeSize, chest,
				clothingLong, sleeveLength, shoulder, waistline, hipline, thighCircumference, kneeCircumference,
				trouserLeg, beforeFork, afterFork, outseam, onChest, underChest, footLength);
		if (!orgin.equals(clone))
			update(clone);
	}

	private void update(MemberExtraEntity entity) {
		String sql = getExecSql("updateMemberExtra", null);
		getNamedParameterJdbcTemplate().update(sql, entity.toMap());
	}

	@Override
	protected ResultSetExtractor<MemberExtraEntity> getResultSetExtractor() {
		return new ResultSetExtractor<MemberExtraEntity>() {
			@Override
			public MemberExtraEntity extractData(ResultSet rs) throws SQLException, DataAccessException {
				MemberExtraEntity result = null;
				while (rs.next()) {
					Integer memberId = ResultSetUtil.getOptValue(rs, "memberId", Integer.class).orNull();
					String jacketSize = ResultSetUtil.getOptValue(rs, "jacketSize", String.class).orNull();
					String bottomsSize = ResultSetUtil.getOptValue(rs, "bottomsSize", String.class).orNull();
					String braSize = ResultSetUtil.getOptValue(rs, "braSize", String.class).orNull();
					String briefsSize = ResultSetUtil.getOptValue(rs, "briefsSize", String.class).orNull();
					String shoeSize = ResultSetUtil.getOptValue(rs, "shoeSize", String.class).orNull();
					BigDecimal chest = ResultSetUtil.getOptValue(rs, "chest", BigDecimal.class).orNull();
					BigDecimal clothingLong = ResultSetUtil.getOptValue(rs, "clothingLong", BigDecimal.class).orNull();
					BigDecimal sleeveLength = ResultSetUtil.getOptValue(rs, "sleeveLength", BigDecimal.class).orNull();
					BigDecimal shoulder = ResultSetUtil.getOptValue(rs, "shoulder", BigDecimal.class).orNull();
					BigDecimal waistline = ResultSetUtil.getOptValue(rs, "waistline", BigDecimal.class).orNull();
					BigDecimal hipline = ResultSetUtil.getOptValue(rs, "hipline", BigDecimal.class).orNull();
					BigDecimal thighCircumference = ResultSetUtil
							.getOptValue(rs, "thighCircumference", BigDecimal.class).orNull();
					BigDecimal kneeCircumference = ResultSetUtil.getOptValue(rs, "kneeCircumference", BigDecimal.class)
							.orNull();
					BigDecimal trouserLeg = ResultSetUtil.getOptValue(rs, "trouserLeg", BigDecimal.class).orNull();
					BigDecimal beforeFork = ResultSetUtil.getOptValue(rs, "beforeFork", BigDecimal.class).orNull();
					BigDecimal afterFork = ResultSetUtil.getOptValue(rs, "afterFork", BigDecimal.class).orNull();
					BigDecimal outseam = ResultSetUtil.getOptValue(rs, "outseam", BigDecimal.class).orNull();
					BigDecimal onChest = ResultSetUtil.getOptValue(rs, "onChest", BigDecimal.class).orNull();
					BigDecimal underChest = ResultSetUtil.getOptValue(rs, "underChest", BigDecimal.class).orNull();
					BigDecimal footLength = ResultSetUtil.getOptValue(rs, "footLength", BigDecimal.class).orNull();
					Integer status = ResultSetUtil.getOptValue(rs, "status", Integer.class).orNull();
					result = new MemberExtraEntity(memberId, jacketSize, bottomsSize, braSize, briefsSize, shoeSize,
							chest, clothingLong, sleeveLength, shoulder, waistline, hipline, thighCircumference,
							kneeCircumference, trouserLeg, beforeFork, afterFork, outseam, onChest, underChest,
							footLength, status);
				}
				return result;
			}
		};
	}

}
