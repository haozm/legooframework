package com.legooframework.model.upload.service;

import java.io.File;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.legooframework.model.upload.entity.FilePath;
import com.legooframework.model.upload.util.ConfigUtil;
import com.legooframework.model.upload.util.FileUtil;
import com.qiniu.common.QiniuException;
import com.qiniu.common.Zone;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.UploadManager;
import com.qiniu.storage.model.DefaultPutRet;
import com.qiniu.util.Auth;
/**
 * update 22:00
 * @author bear
 *
 */
@Service
public class QiniuUploadServer {
	
	private static final Logger logger = LoggerFactory.getLogger(QiniuUploadServer.class);
	//公钥
	private String publicKey = ConfigUtil.getConfig("publicKey");
	//私钥
	private String secretKey = ConfigUtil.getConfig("privateKey");
	
	private final UploadManager uploadManager = new UploadManager(new Configuration(Zone.zone0()));
	
	
	public String upload(File file,FilePath path) {
		Objects.requireNonNull(file,"文件上传文件不能为空");
		Objects.requireNonNull(path,"文件上传路径不能为空");
		String key = path.getEncryptKey(file.getName());
		boolean uploadOk = uploadToQiniu(FileUtil.readFileToBytes(file),path.getNamespace(),key);
		Preconditions.checkState(uploadOk, "文件上传七牛云失败");
		return String.format("%s%s", path.getDomain(),key);
	}
	
	public String getToken(String namespace) {
		Auth auth = Auth.create(publicKey, secretKey);
		String upToken = auth.uploadToken(namespace,null,3600*12,null);
		return upToken;
	}
	
	private boolean uploadToQiniu(byte[] bs,String namespace,String key) {
		try {
			Response response = uploadManager.put(bs, key, getToken(namespace));
			DefaultPutRet putRet = new Gson().fromJson(response.bodyString(), DefaultPutRet.class);
		} catch (QiniuException ex) {
			Response r = ex.response;
			if(logger.isDebugEnabled())
				logger.debug(String.format("上传文件到七牛云失败[%s]", r.getInfo()));
			return false;
		}
		return true;
	}
	
}
