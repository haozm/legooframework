package com.legooframework.model.upload.util;
import java.io.File;

import java.util.Objects;
import java.util.UUID;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.io.Files;
import com.legooframework.model.upload.decoder.Decoder;

public class MultiFileHandler {

	private static final Logger logger = LoggerFactory.getLogger(MultiFileHandler.class);

	private CommonsMultipartFile multfile;

	private String fileName;

	private String[] paths = "/usr/local/espirit/files".replaceAll("\\\\", "/").split("/");

	private Decoder decoder;

	private static final Joiner FILE_PATH_JOINER = Joiner.on(File.separatorChar);

	public MultiFileHandler(CommonsMultipartFile multfile, String path) {
		Objects.requireNonNull(multfile);
		Preconditions.checkArgument(!Strings.isNullOrEmpty(path), "path must not be empty");
		this.multfile = multfile;
		paths = ArrayUtils.addAll(paths, path.replaceAll("\\\\", "/").split("/"));
		this.fileName = multfile.getOriginalFilename();
	}

	public MultiFileHandler(CommonsMultipartFile multfile, String path, Decoder decoder) {
		Objects.requireNonNull(multfile);
		Objects.requireNonNull(decoder);
		Preconditions.checkArgument(!Strings.isNullOrEmpty(path), "path must not be empty");
		this.multfile = multfile;
		paths = ArrayUtils.addAll(paths, path.replaceAll("\\\\", "/").split("/"));
		this.decoder = decoder;
		this.fileName = multfile.getOriginalFilename();
	}

	public File handle() {
		Optional<File> orginFileOpt = save();
		Preconditions.checkState(orginFileOpt.isPresent(), String.format("原文件[%s]保存失败", fileName));
		Optional<File> destFileOpt = decode(orginFileOpt.get());
		if(!destFileOpt.isPresent()) {
			if(logger.isErrorEnabled())
				logger.error(String.format("转换文件[%s]失败", fileName));
			return orginFileOpt.get();
		}
		return destFileOpt.get();
	}

	public Optional<File> decode(File orginFile) {
		return decoder.decode(orginFile);
	}

	/**
	 * @return
	 * @throws Exception
	 */
	public Optional<File> save() {
		String id = UUID.randomUUID().toString();
		String suffix = Files.getFileExtension(this.fileName);
		String fileName = String.format("%s.%s", id, suffix);
		if (Strings.isNullOrEmpty(suffix))
			fileName = id;
		String savePath = FILE_PATH_JOINER
				.join(ArrayUtils.isEmpty(paths) ? new String[] { fileName } : ArrayUtils.add(paths, fileName));
		if (logger.isDebugEnabled())
			logger.debug("savePath:{}", new Object[] { savePath });
		File destFile = new File(savePath);;
		try {
			File parentFile = destFile.getParentFile();
			if(!parentFile.exists())
				parentFile.mkdirs();
			if (!parentFile.isDirectory())
				Preconditions.checkState(!parentFile.mkdirs(),
						String.format("create path : %s directory failed", parentFile.getAbsolutePath()));
			if(destFile.createNewFile())
				save(destFile);
		} catch (Exception e) {
			return Optional.absent();
		}
		return Optional.of(destFile);
	}

	/**
	 * 将文件保存到相对应的目录中
	 * 
	 * @param savePath
	 * @return
	 * @throws Exception
	 */
	private void save(File destFile) throws Exception {
		FileUtil.writeFile(multfile.getBytes(), destFile);
		Preconditions.checkState(destFile.exists(), "文件[%s]未保存成功");
	}

}
