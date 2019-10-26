package com.legooframework.model.wechatcircle.entity;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.legooframework.model.core.base.entity.BaseEntityAction;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class WechatCircleEntityAction extends BaseEntityAction<WechatCircleEntity> {

    private static final Logger logger = LoggerFactory.getLogger(WechatCircleEntityAction.class);

    public WechatCircleEntityAction() {
        super(null);
    }

    /**
     * 保存或 更新 朋友圈 基本信息
     *
     * @param wechatCircle 朋友圈信息
     * @param sourcesFrom  来源信息
     */
    public Map<String, Object> saveOrUpdate(WechatCircleEntity wechatCircle, DataSourcesFrom sourcesFrom) {
        Optional<WechatCircleEntity> optional = findById(wechatCircle.getId(), wechatCircle.getWeixinId());
        if (optional.isPresent()) {
            Optional<WechatCircleEntity> circleSources = optional.get().mergeSource(sourcesFrom);
            circleSources.ifPresent(source -> batchUpateDateSource(Lists.newArrayList(source)));
            wechatCircle.getImagesIfExits().ifPresent(imgs -> optional.get().mergeImages(imgs).ifPresent(this::batchSaveImages));
        } else {
            super.updateAction(wechatCircle, "insert");
            wechatCircle.getImagesIfExits().ifPresent(this::batchSaveImages);
        }
        return wechatCircle.toPkParams();
    }

    /**
     * @param circleIds IDS
     * @return 等等
     */
    public Optional<List<WechatCircleEntity>> findByCircleIds(Collection<Long> circleIds) {
        if (CollectionUtils.isEmpty(circleIds)) return Optional.empty();
        Map<String, Object> params = Maps.newHashMap();
        params.put("circleIds", circleIds);
        params.put("sql", "findByCircleIds");
        return super.queryForEntities("loadWechatCircles", params, getRowMapper());
    }

    /**
     * 获取指定大V手机的朋友圈子
     *
     * @param ownerId daV手机微信号
     * @param pages   开始位置 20  max 1000
     * @param rows    读取多少行
     * @return 你猜猜猜
     */
    public Optional<List<WechatCircleEntity>> loadWechatCircles(String ownerId, String weixinId, int pages, int rows) {
        int _rows = rows < 0 ? 20 : rows > 50 ? 50 : rows;
        int _offset = pages <= 1 ? 0 : (pages - 1) * rows;
        Map<String, Object> params = Maps.newHashMap();
        params.put("ownerId", ownerId);
        params.put("weixinId", weixinId);
        params.put("offset", _offset);
        params.put("rows", _rows);
        params.put("pagination", true);
        if (Strings.isNullOrEmpty(weixinId)) {
            params.put("sql", "loadBatchWechatCircles");
        } else {
            params.put("sql", "loadSingleWechatCircles");
        }
        Optional<List<WechatCircleEntity>> list = super.queryForEntities("loadWechatCircles", params, getRowMapper());
        if (logger.isDebugEnabled())
            logger.debug(String.format("loadWechatCircles(%s,%s,%s, %s) size is %s", ownerId, weixinId, _offset, _rows,
                    list.map(List::size).orElse(0)));
        return list;
    }

    /**
     * 批量处理   喂猪了吧
     *
     * @param circles4Save 待存储的数据
     * @param sourcesFrom  数据的来源
     * @return 你的样子
     */
    public List<Map<String, Object>> batchSaveOrUpdate(List<WechatCircleEntity> circles4Save, DataSourcesFrom sourcesFrom) {
        List<Map<String, Object>> pks = circles4Save.stream().map(WechatCircleEntity::toPkParams).collect(Collectors.toList());
        Optional<List<WechatCircleEntity>> exits_entities = findByIds(pks);
        List<WechatCircleEntity> save_entits_list = Lists.newArrayList();
        List<WechatCircleEntity> update_source_list = Lists.newArrayList();
        List<WechatCircleImage> images_list = Lists.newArrayList();
        if (exits_entities.isPresent()) {
            circles4Save.forEach(save -> {
                Optional<WechatCircleEntity> opt = exits_entities.get().stream().filter(ext -> ext.equalsById(save)).findFirst();
                if (opt.isPresent()) {
                    opt.get().mergeSource(sourcesFrom).ifPresent(update_source_list::add);
                    save.getImagesIfExits().ifPresent(imgs -> opt.get().mergeImages(imgs).ifPresent(images_list::addAll));
                } else {
                    save_entits_list.add(save);
                    save.getImagesIfExits().ifPresent(images_list::addAll);
                }
            });
        } else {
            save_entits_list.addAll(circles4Save);
            circles4Save.forEach(x -> x.getImagesIfExits().ifPresent(images_list::addAll));
        }
        if (CollectionUtils.isNotEmpty(save_entits_list))
            super.batchInsert("batchInsert", save_entits_list);
        if (CollectionUtils.isNotEmpty(update_source_list)) batchUpateDateSource(update_source_list);
        if (CollectionUtils.isNotEmpty(images_list)) batchSaveImages(images_list);
        return pks;
    }

    public void updateReadStatus(Collection<WechatCircleEntity> circles) {
        if (CollectionUtils.isEmpty(circles)) return;
        super.batchUpdate("updatReadStatus", (ps, circle) -> {
            ps.setObject(1, Joiner.on(';').join(circle.getReadStatus()));
            ps.setObject(2, circle.getId());
            ps.setObject(3, circle.getWeixinId());
        }, circles);
    }

    private void batchUpateDateSource(Collection<WechatCircleEntity> circles) {
        super.batchUpdate("updateSourcesFrom", (ps, circle) -> {
            Map<String, Object> params = circleSourcesParams(circle.getSourcesFrom());
            ps.setObject(1, MapUtils.getString(params, "sourcesFrom"));
            ps.setObject(2, MapUtils.getString(params, "sourceWxIds"));
            ps.setObject(3, MapUtils.getString(params, "sourceComIds"));
            ps.setObject(4, MapUtils.getString(params, "sourceStoIds"));
            ps.setObject(5, Joiner.on(';').join(circle.getReadStatus()));
            ps.setObject(6, circle.getId());
            ps.setObject(7, circle.getWeixinId());
        }, circles);
    }

    private Optional<List<WechatCircleEntity>> findByIds(List<Map<String, Object>> ids) {
        if (CollectionUtils.isEmpty(ids)) return Optional.empty();
        Map<String, Object> params = Maps.newHashMap();
        params.put("pks", ids);
        params.put("sql", "findByIds");
        return super.queryForEntities("findByIds", params, getRowMapper());
    }

    /**
     * 已将成名万古枯
     *
     * @param id       朋友圈ID
     * @param weixinId 朋友圈主任ID
     * @param images   图片资料信息
     */
    public void addImages(Long id, String weixinId, List<WechatCircleImage> images) {
        if (CollectionUtils.isEmpty(images)) return;
        Optional<WechatCircleEntity> optional = findById(id, weixinId);
        Preconditions.checkState(optional.isPresent(), "findById(%s,%s) 对应的朋友圈资料不存在", id, weixinId);
        Preconditions.checkState(CircleType.MixCircle == optional.get().getCircleType(), "错误的朋友圈类型，无法添加图片...");
        images.forEach(img -> img.setCircle(optional.get().getId(), optional.get().getCircleId()));
        optional.get().mergeImages(images).ifPresent(this::batchSaveImages);
    }

    static Map<String, Object> circleSourcesParams(Collection<DataSourcesFrom> circleSources) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("sourcesFrom", StringUtils.join(circleSources, "||"));
        params.put("sourceWxIds", StringUtils.join(circleSources.stream().map(DataSourcesFrom::getWeixinId)
                .collect(Collectors.toList()), ','));
        params.put("sourceComIds", StringUtils.join(circleSources.stream().map(DataSourcesFrom::getCompanyId)
                .collect(Collectors.toList()), ','));
        params.put("sourceStoIds", StringUtils.join(circleSources.stream().map(DataSourcesFrom::getStoreId)
                .collect(Collectors.toList()), ','));
        return params;
    }

    public WechatCircleEntity loadById(Long id, String weixinId) {
        Optional<WechatCircleEntity> optional = findById(id, weixinId);
        Preconditions.checkState(optional.isPresent(), "%s,%s 对应的朋友圈不存在...", id, weixinId);
        return optional.get();
    }

    @Override
    public Optional<WechatCircleEntity> findById(Object id) {
        throw new UnsupportedOperationException("不支持该方法，烦请执行 findById(String id, String weixinId) 方法");
    }

    Optional<WechatCircleEntity> findById(Long id, String weixinId) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("id", id);
        params.put("weixinId", weixinId);
        params.put("sql", "findById");
        Optional<WechatCircleEntity> opt = super.queryForEntity("findById", params, getRowMapper());
        if (logger.isDebugEnabled())
            logger.debug(String.format("findById(%s.%s) return %s", id, weixinId, opt.orElse(null)));
        return opt;
    }

    private void batchSaveImages(Collection<WechatCircleImage> images) {
        super.batchInsert("REPLACE INTO WECHAT_CIRCLE_IMAGES (id ,circle_id, url, sub_url, img_order, owner_id) VALUES (?, ?, ?, ?, ?, ?)",
                images.size(), images);
        if (logger.isDebugEnabled())
            logger.debug(String.format("batchSaveImages(size is : %s)", images.size()));
    }

    @Override
    protected RowMapper<WechatCircleEntity> getRowMapper() {
        return new RowMapperImpl();
    }

    class RowMapperImpl implements RowMapper<WechatCircleEntity> {
        @Override
        public WechatCircleEntity mapRow(ResultSet res, int rowNum) throws SQLException {
            return new WechatCircleEntity(res.getLong("id"), res);
        }
    }
}
