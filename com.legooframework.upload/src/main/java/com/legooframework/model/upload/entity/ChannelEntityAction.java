package com.legooframework.model.upload.entity;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.legooframework.model.upload.decoder.Decoder;
import com.legooframework.model.upload.decoder.DecoderFactory;

public class ChannelEntityAction extends NamedParameterJdbcDaoSupport{
	
	
	public ChannelEntity loadById(String id) {
		Preconditions.checkArgument(!Strings.isNullOrEmpty(id), "入参ID不能为空");
		Optional<ChannelEntity> opt = findById(id);
		Preconditions.checkArgument(opt.isPresent(),String.format("渠道[%s]不存在", id));
		return opt.get();
	}
	
	public Optional<ChannelEntity> findById(String id) {
		Preconditions.checkArgument(!Strings.isNullOrEmpty(id), "入参ID不能为空");
		Map<String,Object> paramMap = Maps.newHashMap();
		String sql = "SELECT id AS 'id',name AS 'name',file_type AS 'fileType',decoder AS 'decoder',path AS 'path' FROM yyfilestore.qiniu_channel WHERE id = :channelId";
		paramMap.put("channelId", id);
		ChannelEntity entity = getNamedParameterJdbcTemplate().query(sql, paramMap, getResultSetExtractor());
		if(entity == null)
			return Optional.absent();
		return Optional.of(entity);
	}
	
	
	public List<ChannelEntity> loadAll(){
		String sql = "SELECT " + 
				"						id AS 'id'," + 
				"						name AS 'name'," + 
				"						file_type AS 'fileType'," + 
				"						decoder AS 'decoder'," + 
				"						path AS 'path'" + 
				"				FROM yyfilestore.qiniu_channel";
		List<ChannelEntity> channels = getNamedParameterJdbcTemplate().query(sql, getAllChannelExtractor());
		return channels;
	}
	
	private ResultSetExtractor<List<ChannelEntity>> getAllChannelExtractor(){
		return new ResultSetExtractor<List<ChannelEntity>>() {
			@Override
			public List<ChannelEntity> extractData(ResultSet rs) throws SQLException, DataAccessException {
				List<ChannelEntity> channels = Lists.newArrayList();
				while(rs.next()) channels.add(buildByResultSet(rs));
				return channels;
			}
			
		};
	}
	
	protected ResultSetExtractor<ChannelEntity> getResultSetExtractor() {
		return new ResultSetExtractorImpl();
	}
	
	class ResultSetExtractorImpl implements ResultSetExtractor<ChannelEntity> {

		@Override
		public ChannelEntity extractData(ResultSet rs) throws SQLException, DataAccessException {
			// TODO Auto-generated method stub
			while(rs.next()) {
				return buildByResultSet(rs);
			}
			return null;
		}
		
	}
	
	private ChannelEntity buildByResultSet(ResultSet resultSet) throws SQLException {
		String id = resultSet.getString("id");
		String name = resultSet.getString("name");
		String fileType = resultSet.getString("fileType");
		String decoderStr = resultSet.getString("decoder");
		Decoder decoder = null;
		if(!Strings.isNullOrEmpty(decoderStr)) {
			decoder = DecoderFactory.getDecoder(decoderStr).isPresent()?DecoderFactory.getDecoder(decoderStr).get():null;
		}
		String path = resultSet.getString("path");
		return new ChannelEntity(id, name, fileType, decoder, path);
    }

	
	
	
}
