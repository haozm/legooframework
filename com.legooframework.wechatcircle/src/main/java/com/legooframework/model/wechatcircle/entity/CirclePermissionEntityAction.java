package com.legooframework.model.wechatcircle.entity;

import com.legooframework.model.core.base.entity.BaseEntityAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class CirclePermissionEntityAction extends BaseEntityAction<CirclePermissionEntity> {

    private static final Logger logger = LoggerFactory.getLogger(CirclePermissionEntityAction.class);

    public CirclePermissionEntityAction() {
        super("CirclePermissionCache");
    }

    public void saveOrUpdate(CirclePermissionEntity permission) {
        Optional<CirclePermissionEntity> optional = findById(permission.getWinxinId());
        if (optional.isPresent()) {
            Optional<CirclePermissionEntity> clone = optional.get().change(permission);
            clone.ifPresent(x -> {
                replacrInto(x);
                evictEntity(x);
            });
        } else {
            replacrInto(permission);
        }
    }

    private void replacrInto(CirclePermissionEntity entity) {
        super.update("REPLACE INTO WECHAT_CIRCLE_PERMISSION (weixin_id, permission, block_list) VALUES (:id, :permission, :blockWxIds )",
                entity.toParamMap());
        if (logger.isDebugEnabled())
            logger.debug(String.format("replacrInto(%s) finisded", entity.toString()));
    }

    @Override
    protected RowMapper<CirclePermissionEntity> getRowMapper() {
        return new RowMapperImpl();
    }

    class RowMapperImpl implements RowMapper<CirclePermissionEntity> {
        @Override
        public CirclePermissionEntity mapRow(ResultSet res, int rowNum) throws SQLException {
            return new CirclePermissionEntity(res.getString("id"), res);
        }
    }
}
