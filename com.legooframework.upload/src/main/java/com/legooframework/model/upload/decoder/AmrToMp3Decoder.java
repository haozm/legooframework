package com.legooframework.model.upload.decoder;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import org.nutz.lang.Encoding;
import org.nutz.lang.Lang;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.legooframework.model.upload.util.ConfigUtil;

/**
 * amr格式转mp3
 *
 * @author Administrator
 */
public class AmrToMp3Decoder implements Decoder {
	
	public final static String TYPE = "AmrToMp3";
	
	private final static Logger logger = LoggerFactory.getLogger(AmrToMp3Decoder.class);

	private final static String AMR_TO_MP3_LINUX_CMD = ConfigUtil.getConfig("amr2mp3.decoder.linux.cmd");

	private final static String AUDIO_AMR_SUFFIX = "amr";

	private final static String AUDIO_MP3_SUFFIX = "mp3";

	/**
	 * 文件转码
	 */
	public Optional<File> decode(File orginFile) {
		Objects.requireNonNull(orginFile, "AMR TO MP3 原文件不存在");
		suffixCheck(orginFile);
		String amr = orginFile.getAbsolutePath();
		String mp3 = amr.replaceAll(AUDIO_AMR_SUFFIX, AUDIO_MP3_SUFFIX);
		Preconditions.checkState(execAmrToMap3(amr), "linux 环境下执行AMR TO MP3命令失败");
		File destFile = new File(mp3);
		if (!destFile.exists())
			return Optional.absent();
		return Optional.of(destFile);
	}

	/**
	 * 文件后缀校验
	 *
	 * @param file
	 */
	private void suffixCheck(File file) {
		String fileName = file.getName();
		String suffix = fileName.substring(fileName.lastIndexOf(".") + 1);
		Preconditions.checkState(AUDIO_AMR_SUFFIX.equals(suffix.trim()), "AMR TO MP3 文件格式不匹配");
	}

	/**
	 * 使用linux命令进行 AMR TO MP3 转码
	 *
	 * @param amr
	 * @param mp3
	 * @return
	 */
	private boolean execAmrToMap3(String amr) {
		boolean flag = true;
		String cmd = String.format("%s %s %s", AMR_TO_MP3_LINUX_CMD, amr, AUDIO_MP3_SUFFIX);
		try {
			StringBuilder msg = Lang.execOutput(cmd, Encoding.CHARSET_UTF8);
			if (logger.isInfoEnabled())
				logger.info(String.format("执行转码程序[%s] 返回结果[%s]", cmd, msg));
		} catch (IOException e) {
			flag = false;
		}
		return flag;
	}
}
