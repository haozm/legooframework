package com.legooframework.model.upload.mvc;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.legooframework.model.upload.entity.NamespaceEntityAction;
import com.legooframework.model.upload.entity.UploadRecordEntity;
import com.legooframework.model.upload.entity.UploadRecordSimpleDTO;
import com.legooframework.model.upload.service.UploadFileServer;
import com.legooframework.model.upload.util.QiniuClientUtils;

/**
 * update 22:00
 *
 * @author bear
 */
@Controller(value = "uploadController")
public class MvcController extends BaseController {

	private final static Logger logger = LoggerFactory.getLogger(MvcController.class);

	@RequestMapping(value = "/upload/single")
	@ResponseBody
	public Map<String, Object> handleFile(@RequestParam("file") CommonsMultipartFile file,
			@RequestParam("channel") String channel, @RequestParam("fileType") String fileType,
			@RequestParam("imei") String imei, @RequestParam("company") Integer company,
			@RequestParam(required = false) Map<String, Object> map, HttpServletRequest request) {
		if (logger.isDebugEnabled())
			logger.debug(String.format("[%s]->handleFile(file[%s],channel[%s],fileType[%s],imei[%s],company[%s])",
					request.getRequestURI(), file.getOriginalFilename(), channel, fileType, imei, company));
		if (file.getSize() == 0L) {
			if (logger.isDebugEnabled())
				logger.debug(String.format("文件[%s]为空文件", file.getOriginalFilename()));
			return wrapperEmptyResponse();
		}
		Stopwatch sw = Stopwatch.createStarted();
		UploadRecordEntity record = server.decodeFile(file, channel, company, null == map ? Maps.newHashMap() : map);
		if (logger.isDebugEnabled())
			logger.debug(String.format("handleFile(request=%s) elapsed %s ms ", request.getRequestURI(),
					sw.elapsed(TimeUnit.MILLISECONDS)));
		Map<String, Object> vo = Maps.newHashMap();
		vo.put("url", record.getQiniuPath());
		return wrapperResponse(vo);
	}

	@RequestMapping(value = "/upload/information")
	@ResponseBody
	public Map<String, Object> getUploadInfo(@RequestParam Map<String, Object> map, HttpServletRequest request) {
		if (logger.isDebugEnabled())
			logger.debug(String.format("[%s]->http_request_map=%s", request.getRequestURI(), map));
		Preconditions.checkArgument(map.containsKey("channel") && map.containsKey("company"),
				"请求参数缺少channel或company参数");
		String channelId = MapUtils.getString(map, "channel");
		Integer companyId = MapUtils.getInteger(map, "company");
		UploadRecordSimpleDTO dto = server.getUploadInformation(channelId, companyId, map);
		return wrapperResponse(dto.toMap());
	}

	@RequestMapping(value = "/upload/backet")
	@ResponseBody
	public Map<String, Object> getBacketInfo(@RequestParam Map<String, Object> paramMap, HttpServletRequest request) {
		if (logger.isDebugEnabled())
			logger.debug(String.format("[%s]->http_request_map=%s", request.getRequestURI(), paramMap));
		List<Map<String, Object>> voMaps = Lists.newArrayList();
		try {
			List<String> backets = QiniuClientUtils.getBuckets();
			if (backets.isEmpty())
				return wrapperEmptyResponse();
			for (String backet : backets) {
				Map<String, Object> map = Maps.newHashMap();
				map.put("backet", backet);
				String domain = QiniuClientUtils.getDomainForCsosm(backet);
				if (Strings.isNullOrEmpty(domain))
					domain = "";
				map.put("domain", domain);
				voMaps.add(map);
			}
		} catch (Exception e) {
			return wrapperErrorResponse(e, "请求获取七牛云存储空间有误");
		}
		return wrapperResponse(voMaps);
	}

	@RequestMapping(value = "/upload/addNamespace")
	@ResponseBody
	public Map<String, Object> addNamespace(@RequestBody(required = false) Map<String, Object> paramMap,
			HttpServletRequest request) {
		if (logger.isDebugEnabled())
			logger.debug(String.format("[%s]->http_request_map=%s", request.getRequestURI(), paramMap));
		Preconditions.checkArgument(paramMap.containsKey("companyId") && paramMap.containsKey("namespace"),
				"入参缺少companyId或namespace");
		Integer companyId = MapUtils.getInteger(paramMap, "companyId");
		String companyName = MapUtils.getString(paramMap, "companyName");
		String nameShort = MapUtils.getString(paramMap, "nameShort", "");
		String data = MapUtils.getString(paramMap, "namespace");
		String[] datas = data.split("@");
		Preconditions.checkState(datas.length == 2, "请求参数namespace规则有误，规则为xxx@yyy");
		String namespace = datas[0].trim();
		String domain = datas[1].trim();
		Integer id = server.addNamespace(companyId, companyName,nameShort, namespace, domain);
		Map<String, Object> map = Maps.newHashMap();
		map.put("id", id);
		return wrapperResponse(map);
	}

	@RequestMapping(value = "/upload/modifyNamespace")
	@ResponseBody
	public Map<String, Object> modifyNamespace(@RequestBody(required = false) Map<String, Object> paramMap,
			HttpServletRequest request) {
		if (logger.isDebugEnabled())
			logger.debug(String.format("[%s]->http_request_map=%s", request.getRequestURI(), paramMap));
		Preconditions.checkArgument(paramMap.containsKey("companyId"), "请求参数缺少companyId");
		Integer companyId = MapUtils.getInteger(paramMap, "companyId");
		String domain = MapUtils.getString(paramMap, "domain");
		String namespace = MapUtils.getString(paramMap, "namespace");
		Integer id = getBean(NamespaceEntityAction.class, request).modify(companyId, domain, namespace);
		Map<String, Object> map = Maps.newHashMap();
		map.put("id", id);
		return wrapperResponse(map);
	}

	@RequestMapping(value = "/upload/unbindNamespace")
	@ResponseBody
	public Map<String, Object> removeNamespace(@RequestBody(required = false) Map<String, Object> paramMap,
			HttpServletRequest request) {
		if (logger.isDebugEnabled())
			logger.debug(String.format("[%s]->http_request_map=%s", request.getRequestURI(), paramMap));
		Preconditions.checkArgument(paramMap.containsKey("companyId"), "请求参数缺少companyId");
		Integer companyId = MapUtils.getInteger(paramMap, "companyId");
		getBean(NamespaceEntityAction.class, request).delete(companyId);
		return wrapperEmptyResponse();
	}

	@RequestMapping(value = "/upload/loadAllNamespace")
	@ResponseBody
	public Map<String, Object> loadAllNamespace(@RequestParam Map<String, Object> paramMap,
			HttpServletRequest request) {
		if (logger.isDebugEnabled())
			logger.debug(String.format("[%s]->http_request_map=%s", request.getRequestURI(), paramMap));
		List<Map<String, Object>> result = getBean(NamespaceEntityAction.class, request).queryNamespaces();
		if(null == result || result.isEmpty()) return wrapperResponse(Lists.newArrayList());
		return wrapperResponse(result);
	}
	
	@RequestMapping(value = "/upload/{companyId}/getUploadInfo")
	@ResponseBody
	public Map<String, Object> getUploadInfo(@PathVariable Integer companyId,
			HttpServletRequest request) {
		return wrapperResponse(server.getQiniuInformation(companyId));
	}
	

	@Resource
	private UploadFileServer server;

}
