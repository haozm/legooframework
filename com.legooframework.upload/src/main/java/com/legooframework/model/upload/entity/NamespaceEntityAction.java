package com.legooframework.model.upload.entity;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class NamespaceEntityAction extends NamedParameterJdbcDaoSupport{
	
	protected ResultSetExtractor<NamespaceEntity> getResultSetExtractor() {
		return new ResultSetExtractorImpl();
	}
	
	class ResultSetExtractorImpl implements ResultSetExtractor<NamespaceEntity> {

		@Override
		public NamespaceEntity extractData(ResultSet rs) throws SQLException, DataAccessException {
			// TODO Auto-generated method stub
			while(rs.next()) {
				return buildByResultSet(rs);
			}
			return null;
		}
		
	}
	
	private NamespaceEntity buildByResultSet(ResultSet resultSet) throws SQLException {
		Integer companyId = resultSet.getInt("companyId");
		String domain = resultSet.getString("domain");
		String namespace = resultSet.getString("namespace");
		String channelIds = resultSet.getString("channelIds");
		String nameShort = resultSet.getString("nameShort");
		String companyName = resultSet.getString("companyName");
		List<String> channels = Lists.newArrayList();
		if(!Strings.isNullOrEmpty(channelIds))
			channels.addAll(Arrays.asList(channelIds.split(",")));
		NamespaceEntity entity = new NamespaceEntity(companyId, companyName,nameShort,domain, namespace, channels);
		return entity;
    }
	/**
	 * 根据命名空间名称获取命名空间
	 * @param companyId
	 * @return
	 */
	public Optional<NamespaceEntity> findByNamespace(String namespace){
		Preconditions.checkState(!Strings.isNullOrEmpty(namespace),"入参namespace");
		Map<String,Object> paramMap = Maps.newHashMap();
		paramMap.put("namespace", namespace);
		String sql = "SELECT 			company_id AS 'companyId',"+
				"						company_name AS 'companyName'," + 
				"						name_short AS 'nameShort'," + 
				"						domain AS 'domain'," + 
				"						namespace AS 'namespace'," + 
				"						channel_ids AS 'channelIds'" + 
				"				FROM yyfilestore.qiniu_company_namespace" + 
				"				WHERE namespace = :namespace";
		NamespaceEntity entity = getNamedParameterJdbcTemplate().query(sql,paramMap,getResultSetExtractor());
		if(entity == null)
			return Optional.empty();
		return Optional.of(entity);
	}
	
	/**
	 * 根据公司ID获取命名空间
	 * @param companyId
	 * @return
	 */
	public Optional<NamespaceEntity> findByCompanyId(Integer companyId) {
		Objects.requireNonNull(companyId,"入参company不能为空");
		Map<String,Object> paramMap = Maps.newHashMap();
		paramMap.put("companyId", companyId);
		String sql = "SELECT company_id AS 'companyId',company_name AS 'companyName',name_short AS 'nameShort',domain AS 'domain',namespace AS 'namespace',channel_ids AS 'channelIds'FROM yyfilestore.qiniu_company_namespace WHERE company_id = :companyId";
		NamespaceEntity entity = getNamedParameterJdbcTemplate().query(sql,paramMap,getResultSetExtractor());
		if(entity == null)
			return Optional.empty();
		return Optional.of(entity);
	}
		
	public NamespaceEntity loadByCompanyId(Integer companyId) {
		Optional<NamespaceEntity> opt = findByCompanyId(companyId);
		Preconditions.checkArgument(opt.isPresent(), String.format("公司【%s】未设置命名空间", companyId));
		return opt.get();
	}
	
	
	public Integer addNamespace(Integer companyId,String companyName,String nameShort,String domain,String namespace,List<ChannelEntity> channels) {
		Objects.requireNonNull(companyId,"入参companyId不能为空");
		Preconditions.checkArgument(!Strings.isNullOrEmpty(domain), "入参domain不能为空");
		Preconditions.checkArgument(!Strings.isNullOrEmpty(namespace),"入参namespace不能为空");
		Preconditions.checkArgument(!channels.isEmpty(),"入参channels不能为空");
		Optional<NamespaceEntity> comNamespaceOpt = findByCompanyId(companyId);
		Preconditions.checkState(!comNamespaceOpt.isPresent(), String.format("公司【%s】对应的存储空间已存在，请勿重复绑定", companyId));
		NamespaceEntity entity = new NamespaceEntity(companyId, companyName,nameShort,domain, channels,namespace);
		String sql = "INSERT INTO yyfilestore.qiniu_company_namespace" + 
				"						(company_id,company_name,name_short,domain,namespace,channel_ids)" + 
				"				VALUES(:companyId,:companyName,:nameShort,:domain,:namespace,:channelIds)";
		getNamedParameterJdbcTemplate().update(sql, entity.toMap());
		return companyId;
	}

	public Integer modify(Integer companyId,String domain,String namespace) {
		Objects.requireNonNull(companyId,"入参companyId不能为空");
		NamespaceEntity orgin = loadByCompanyId(companyId);
		if(!orgin.modify(domain, namespace)) return companyId;
		String sql = "UPDATE yyfilestore.qiniu_company_namespace" + 
				"             			SET" + 
				"             			domain =:domain," + 
				"             			namespace = :namespace" + 
				"				WHERE company_id = :companyId";
		getNamedParameterJdbcTemplate().update(sql, orgin.toMap());
		return companyId;
	}
	
	public void delete(Integer companyId) {
		Objects.requireNonNull(companyId,"入参companyId不能为空");
		Map<String,Object> paramMap = Maps.newHashMap();
		paramMap.put("companyId", companyId);
		String sql = "DELETE FROM yyfilestore.qiniu_company_namespace" + 
				"				WHERE company_id = :companyId";
		getNamedParameterJdbcTemplate().update(sql, paramMap);
	}
	
	public List<Map<String, Object>> queryNamespaces(){
		String sql = "SELECT 			" + 
				"						qcn.company_id AS 'companyId'," + 
				"						CASE " + 
				"						WHEN qcn.company_id = -1 THEN '羿元科技'" + 
				"						ELSE IFNULL(qcn.company_name,'')" + 
				"						END AS 'companyName'," + 
				"						qcn.name_short AS 'nameShort'," + 
				"						qcn.domain AS 'domain'," + 
				"						qcn.namespace AS 'namespace'," + 
				"						qcn.channel_ids AS 'channelIds'," + 
				"						IF(qcn.namespace IS NULL,'0','1') AS 'binded'" + 
				"				FROM" + 
				"						yyfilestore.qiniu_company_namespace qcn";
		return getNamedParameterJdbcTemplate().queryForList(sql, new HashMap());
	}
}
