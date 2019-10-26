package com.legooframework.model.devices.entity;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.legooframework.model.core.base.entity.BaseEntityAction;
import com.legooframework.model.devices.entity.DeviceEntity.OsType;
import com.legooframework.model.devices.entity.DeviceEntity.State;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class DeviceEntityAction extends BaseEntityAction<DeviceEntity> {

	private static final Logger logger = LoggerFactory.getLogger(DeviceEntityAction.class);

	public DeviceEntityAction() {
		super(null);
	}

	/**
	 * 新增或更新设备信息 将不需要改变的信息置为NULL
	 *
	 * @param imei
	 * @param name
	 * @param brand
	 * @param model
	 * @param color
	 * @param cpu
	 * @param memorySize
	 * @param os
	 * @param xportOs
	 * @param screenSize
	 * @param osType
	 * @param price
	 * @param productionDate
	 * @param repairReason
	 * @param scrapReason
	 * @return
	 */
	public DeviceEntity saveOrUpdate(String imei, String name, String brand, String model, String color, String cpu,
			Integer memorySize, String os, String xportOs, Double screenSize, OsType osType, BigDecimal price,
			Date productionDate, String repairReason, String scrapReason, String imei1, String imei2) {
		Preconditions.checkArgument(!Strings.isNullOrEmpty(imei), "设备imei不能为空或null");
		Optional<DeviceEntity> deviceOpt = findByImei(imei);
		if (!deviceOpt.isPresent()) {
			String id = DigestUtils.md5Hex(imei);
			DeviceEntity entity = new DeviceEntity.Builder(id, imei, name).brand(brand).model(model).color(color)
					.cpu(cpu).memorySize(memorySize).os(os).xportOs(xportOs).screenSize(screenSize).osType(osType)
					.price(price).productionDate(productionDate).repairReason(repairReason).scrapReason(scrapReason)
					.imei1(imei1).imei2(imei2).build();
			return save(entity);
		} else {
			DeviceEntity oldDevice = deviceOpt.get();
			DeviceEntity updateDevice = new DeviceEntity.Builder(oldDevice).name(name).brand(brand).model(model)
					.color(color).cpu(cpu).memorySize(memorySize).os(os).xportOs(xportOs).screenSize(screenSize)
					.osType(osType).price(price).productionDate(productionDate).repairReason(repairReason)
					.scrapReason(scrapReason).build();
			return update(updateDevice);
		}
	}

	/**
	 * 新增设备，并向如数据库插入一条记录
	 *
	 * @param entity
	 * @return
	 */
	private DeviceEntity save(DeviceEntity entity) {
		Objects.requireNonNull(entity);
		int result = super.update(getStatementFactory(), getModelName(), "insert", entity);
		if (result != 1) {
			if (logger.isErrorEnabled())
				logger.error("DeviceEntityAction#save save device entity[{}] failed!", new Object[] { entity.getId() });
			throw new IllegalStateException(String.format("新增设备实体[%s]失败", entity.getId()));
		}

		if (logger.isInfoEnabled())
			logger.info("DeviceEntityAction#save insert device entity[{}] success", new Object[] { entity.getId() });

		return entity;
	}

	/**
	 * 更新设备信息
	 *
	 * @param entity
	 * @return
	 */
	private DeviceEntity update(DeviceEntity entity) {

		Objects.requireNonNull(entity);
		int result = super.update(getStatementFactory(), getModelName(), "update_base_info", entity);
		if (result != 1) {
			if (logger.isErrorEnabled())
				logger.error("DeviceEntityAction#update update device entity[{}] failed!",
						new Object[] { entity.getId() });
			throw new IllegalStateException(String.format("更新设备实体[%s]失败", entity.getId()));
		}

		if (logger.isInfoEnabled())
			logger.info("DeviceEntityAction#update update device entity[{}] success", new Object[] { entity.getId() });
		return entity;
	}

	/**
	 * 根据imei号查找设备
	 *
	 * @param imei
	 * @return
	 */
	public Optional<DeviceEntity> findByImei(String imei) {
		if (Strings.isNullOrEmpty(imei))
			throw new IllegalArgumentException("设备imei号不能为空或null");
		Map<String, Object> paramMap = Maps.newHashMap();
		paramMap.put("imei", imei);
		Optional<DeviceEntity> entityOpt = super.queryForEntity(getStatementFactory(), getModelName(), "findByImei",
				paramMap, getRowMapper());
		return entityOpt;
	}

	/**
	 * 根据设备ID查找设备信息
	 *
	 * @param id
	 * @return
	 */
	public Optional<DeviceEntity> findById(String id) {
		if (Strings.isNullOrEmpty(id))
			throw new IllegalArgumentException("设备Id不能为空或null");
		Map<String, Object> paramMap = Maps.newHashMap();
		paramMap.put("id", id);
		Optional<DeviceEntity> entityOpt = super.queryForEntity(getStatementFactory(), getModelName(), "findById",
				paramMap, getRowMapper());
		return entityOpt;
	}

	/**
	 * 将设备状态改变为正常使用状态
	 *
	 * @param imei
	 * @return
	 */
	public DeviceEntity normalAction(String imei) {
		if (Strings.isNullOrEmpty(imei))
			throw new IllegalArgumentException("设备imei号不能为空或null");
		Optional<DeviceEntity> deviceOpt = findByImei(imei);
		if (!deviceOpt.isPresent()) {
			if (logger.isErrorEnabled())
				logger.error("DeviceEntityAction#normalAction device[{}] is not exists ", imei);
			throw new IllegalStateException(String.format("设备[%s] 不存在", imei));
		}
		DeviceEntity orgin = deviceOpt.get();
		if (orgin.isNormalState())
			return orgin;
		DeviceEntity clone = orgin.execNormal();
		int result = super.update(getStatementFactory(), getModelName(), "update_state", clone);
		if (result != 1) {
			if (logger.isErrorEnabled())
				logger.error("DeviceEntityAction#normalAction update device entity[{}] state to normal failed!",
						new Object[] { clone.getId() });
			throw new IllegalStateException(String.format("更新设备实体[%s]状态为正常使用状态失败", clone.getId()));
		}
		if (logger.isInfoEnabled())
			logger.info("DeviceEntityAction#normalAction update device entity[{}] state to normal success",
					clone.getId());
		return clone;
	}

	/**
	 * 将设备状态改变为维修状态
	 *
	 * @param imei
	 * @return
	 */
	public DeviceEntity repairAction(String imei) {
		if (Strings.isNullOrEmpty(imei))
			throw new IllegalArgumentException("设备imei号不能为空或null");
		Optional<DeviceEntity> deviceOpt = findByImei(imei);
		if (!deviceOpt.isPresent()) {
			if (logger.isErrorEnabled())
				logger.error("DeviceEntityAction#repairAction device[{}] is not exists ", imei);
			throw new IllegalStateException(String.format("设备[%s] 不存在", imei));
		}
		DeviceEntity orgin = deviceOpt.get();
		if (orgin.isReqairState())
			return orgin;
		DeviceEntity clone = orgin.execReqair();
		int result = super.update(getStatementFactory(), getModelName(), "update_state", clone);
		if (result != 1) {
			if (logger.isErrorEnabled())
				logger.error("DeviceEntityAction#repairAction update device entity[{}] state to repair failed!",
						new Object[] { clone.getId() });
			throw new IllegalStateException(String.format("更新设备实体[%s]状态为维修状态失败", clone.getId()));
		}
		if (logger.isInfoEnabled())
			logger.info("DeviceEntityAction#repairAction update device entity[{}] state to repair success",
					clone.getId());
		return clone;
	}

	/**
	 * 将设备状态改变为报废状态
	 *
	 * @param imei
	 * @return
	 */
	public DeviceEntity scrapAction(String imei) {
		if (Strings.isNullOrEmpty(imei))
			throw new IllegalArgumentException("设备imei号不能为空或null");
		Optional<DeviceEntity> deviceOpt = findByImei(imei);
		if (!deviceOpt.isPresent()) {
			if (logger.isErrorEnabled())
				logger.error("DeviceEntityAction#scrapAction device[{}] is not exists ", imei);
			throw new IllegalStateException(String.format("设备[%s] 不存在", imei));
		}
		DeviceEntity orgin = deviceOpt.get();
		if (orgin.isScrapState())
			return orgin;
		DeviceEntity clone = orgin.execScrap();
		int result = super.update(getStatementFactory(), getModelName(), "update_state", clone);
		if (result != 1) {
			if (logger.isErrorEnabled())
				logger.error("DeviceEntityAction#scrapAction update device entity[{}] state to scrap failed!",
						new Object[] { clone.getId() });
			throw new IllegalStateException(String.format("更新设备实体[%s]状态为维修状态失败", clone.getId()));
		}
		if (logger.isInfoEnabled())
			logger.info("DeviceEntityAction#repairAction update device entity[{}] state to scrap success",
					clone.getId());
		return clone;
	}

	@Override
	protected RowMapper<DeviceEntity> getRowMapper() {
		// TODO Auto-generated method stub
		return new RowMapperImpl();
	}

	class RowMapperImpl implements RowMapper<DeviceEntity> {

		@Override
		public DeviceEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
			String id = rs.getString("deviceId");
			String imei = rs.getString("deviceImei");
			String name = rs.getString("deviceName");
			DeviceEntity.Builder builder = new DeviceEntity.Builder(id, imei, name);
			builder.brand(rs.getString("deviceBrand"));
			builder.model(rs.getString("deviceModel"));
			builder.color(rs.getString("deviceColor"));
			builder.cpu(rs.getString("deviceCpu"));
			builder.screenSize(Double.valueOf(rs.getString("deviceScreenSize")));
			builder.os(rs.getString("deviceOs"));
			builder.xportOs(rs.getString("deviceXportOs"));
			builder.osType(OsType.valueOf(Integer.valueOf(rs.getString("deviceOsType"))));
			String price = rs.getString("devicePrice");
			builder.price(new BigDecimal(price == null ? "0.0000" : price));
			builder.state(State.valueOf(Integer.valueOf(rs.getString("deviceState"))));
			builder.productionDate(rs.getDate("deviceProductionDate"));
			builder.repairReason(rs.getString("deviceReqairReason"));
			builder.scrapReason(rs.getString("deviceReqairReason"));
			return builder.build();

		}

	}

}
