package com.legooframework.model.upload.decoder;

import java.io.File;

import java.io.IOException;
import java.util.Objects;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.legooframework.model.upload.util.ConfigUtil;

import org.nutz.lang.Encoding;
import org.nutz.lang.Lang;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Mp3ToAmrDecoder implements Decoder {

	private final static Logger logger = LoggerFactory.getLogger(Mp3ToAmrDecoder.class);
	
	public final static String TYPE = "Mp3ToAmr";
	
	private final static String MP3_TO_AMR_LINUX_CMD = ConfigUtil.getConfig("mp32amr.decoder.linux.cmd");

	private final static String AUDIO_AMR_SUFFIX = "amr";

	private final static String AUDIO_MP3_SUFFIX = "mp3";

	/**
	 * 文件转码
	 */
	public Optional<File> decode(File orginFile) {
		Objects.requireNonNull(orginFile, "AMR TO MP3 原文件不存在");
		suffixCheck(orginFile);
		String mp3 = orginFile.getAbsolutePath();
		String amr = mp3.replaceAll(AUDIO_MP3_SUFFIX, AUDIO_AMR_SUFFIX);
		Preconditions.checkState(execMp3ToAmr(mp3, amr), "linux 环境下执行MP3 TO AMR命令失败");
		File destFile = new File(amr);
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
		Preconditions.checkState(AUDIO_MP3_SUFFIX.equals(suffix.trim()), "MP3 TO AMR 文件格式不匹配");
	}

	/**
	 * 在linux环境下执行MP3转AMR
	 *
	 * @param mp3
	 * @param amr
	 * @return
	 */
	private boolean execMp3ToAmr(String mp3, String amr) {
		boolean flag = true;
		String cmd = String.format(MP3_TO_AMR_LINUX_CMD, mp3, amr);
		try {
			StringBuilder msg = Lang.execOutput(cmd, Encoding.CHARSET_UTF8);
			if (logger.isDebugEnabled())
				logger.debug(String.format("执行转码程序[%s] 返回结果[%s]", cmd, msg));
		} catch (IOException e) {
			flag = false;
		}
		return flag;
	}
}
