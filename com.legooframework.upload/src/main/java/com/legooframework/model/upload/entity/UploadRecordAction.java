package com.legooframework.model.upload.entity;
import java.util.Map;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;

public class UploadRecordAction extends NamedParameterJdbcDaoSupport  {


    private static final Logger logger = LoggerFactory.getLogger(UploadRecordAction.class);

    /**
     * 往数据库写入上传文件记录
     *
     * @param entity
     */
    public UploadRecordEntity insert(String orginKey, String orginName, String orginPath, String qiniuKey,
                                     String qiniuDomain, String qiniuNamespace, String qiniuPath, String upToken) {
        UploadRecordEntity entity = new UploadRecordEntity(orginKey, orginName, orginPath, qiniuKey, qiniuDomain,
                qiniuNamespace, qiniuPath, upToken);
        String sql = "insert into yyfilestore.upload_qiniu_record" + 
        		"                 (orgin_name,orgin_key,orgin_path,qiniu_key,qiniu_domain,qiniu_namspace,qiniu_path,uptoken)" + 
        		"            values" + 
        		"            	(:orginName,:orginKey,:orginPath,:qiniuKey,:qiniuDomain,:qiniuNamespace,:qiniuPath,:upToken)";
        int res = getNamedParameterJdbcTemplate().update(sql, entity.toMap());
        Preconditions.checkState(1 == res, "保存文件信息到数据库失败");
        if (logger.isDebugEnabled())
            logger.debug(" 保存文件信息到数据库成功");
        return entity;
    }

}
