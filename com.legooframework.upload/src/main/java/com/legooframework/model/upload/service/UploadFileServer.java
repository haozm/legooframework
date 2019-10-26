package com.legooframework.model.upload.service;

import java.io.File;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.legooframework.model.upload.entity.ChannelEntity;
import com.legooframework.model.upload.entity.ChannelEntityAction;
import com.legooframework.model.upload.entity.FilePath;
import com.legooframework.model.upload.entity.NamespaceEntity;
import com.legooframework.model.upload.entity.NamespaceEntityAction;
import com.legooframework.model.upload.entity.UploadRecordAction;
import com.legooframework.model.upload.entity.UploadRecordEntity;
import com.legooframework.model.upload.entity.UploadRecordSimpleDTO;
import com.legooframework.model.upload.util.MultiFileHandler;

/**
 * update 22:00
 * 
 * @author bear
 *
 */
@Service
public class UploadFileServer extends AbstractBaseServer {
	
	public UploadFileServer() {
		
	}
	public UploadRecordEntity decodeFile(CommonsMultipartFile multiFile,String channelId, Integer companyId,Map<String, Object> datas) {
		Objects.requireNonNull(multiFile, "入参multiFile文件不能为空");
		Preconditions.checkArgument(!Strings.isNullOrEmpty(channelId), "入参channelId不能为空");
		Objects.requireNonNull(companyId, "入参companyId不能为空");
		NamespaceEntity namespace = null;
		Optional<NamespaceEntity> namespaceOpt = namespaceAction.findByCompanyId(companyId);
		if(!namespaceOpt.isPresent()) {
			Optional<NamespaceEntity> defaultNamespaceOpt = namespaceAction.findByCompanyId(-1);
			Preconditions.checkState(defaultNamespaceOpt.isPresent(), "默认存储空间不存在");
			namespace = defaultNamespaceOpt.get();
		}
		namespace = namespaceOpt.get();
		ChannelEntity channel = channelAction.loadById(channelId);
		FilePath path = new FilePath(channel.getPath(), namespace.getNamespace(), namespace.getHttpDomain(),namespace.getNameShort());
		FilePath execPath = path.execute(datas);
		MultiFileHandler handler = new MultiFileHandler(multiFile, execPath.getKey(), channel.getDecoder());
		File file = handler.handle();
		String qiniuUrl = qiniuServer.upload(file, path);
		UploadRecordEntity entity = uploadAction.insert(path.getKey(), multiFile.getOriginalFilename(), file.getAbsolutePath(),
				path.getEncryptKey(), path.getDomain(), path.getNamespace(), qiniuUrl,
				qiniuServer.getToken(namespace.getNamespace()));
		return entity;
	}

	public UploadRecordSimpleDTO getUploadInformation(String channelId, Integer companyId, Map<String, Object> datas) {
		Preconditions.checkArgument(!Strings.isNullOrEmpty(channelId), "入参channelId不能为空");
		Objects.requireNonNull(companyId, "入参companyId不能为空");
		Optional<NamespaceEntity> namespaceOpt = namespaceAction.findByCompanyId(companyId);
		Preconditions.checkState(namespaceOpt.isPresent(), String.format("公司[%s]未设置存储空间", companyId));
		ChannelEntity channel = channelAction.loadById(channelId);
		FilePath path = new FilePath(channel.getPath(), namespaceOpt.get().getNamespace(), namespaceOpt.get().getHttpDomain(),namespaceOpt.get().getNameShort());
		FilePath execPath = path.execute(datas);
		UploadRecordSimpleDTO dto = new UploadRecordSimpleDTO(execPath.getDomain(),
				qiniuServer.getToken(execPath.getNamespace()), execPath.getEncryptKey());
		return dto;
	}
	
	public UploadRecordSimpleDTO getUploadInformation(String channelId, Map<String, Object> datas) {
		Preconditions.checkArgument(!Strings.isNullOrEmpty(channelId), "入参channelId不能为空");
		return getUploadInformation(channelId, -1, datas);
	}
	
	public Map<String, Object> getQiniuInformation(Integer companyId) {
		Objects.requireNonNull(companyId, "入参companyId不能为空");
		Optional<NamespaceEntity> namespaceOpt = namespaceAction.findByCompanyId(companyId);
		Map<String, Object> map = Maps.newHashMap();
		if(!namespaceOpt.isPresent()) return map;
		map.put("serverUrl", namespaceOpt.get().getServerUrl());
		map.put("namespace", namespaceOpt.get().getNamespace());
		map.put("domain", namespaceOpt.get().getHttpDomain());
		return map;
	}
	
	public Integer addNamespace(Integer companyId, String companyName,String nameShort,String namespace, String domain) {
		List<ChannelEntity> channels = channelAction.loadAll();
		return namespaceAction.addNamespace(companyId, companyName,nameShort,domain, namespace, channels);
	}
	@Autowired
	private NamespaceEntityAction namespaceAction;
	@Autowired
	private ChannelEntityAction channelAction;
	@Autowired
	private UploadRecordAction uploadAction;
	@Autowired
	private QiniuUploadServer qiniuServer;
}
