package com.legooframework.model.upload.decoder;

import java.util.Map;
import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;

public class DecoderFactory {

	private final static Map<String, Decoder> cache = Maps.newConcurrentMap();

	static {
		cache.put(AmrToMp3Decoder.TYPE, new AmrToMp3Decoder());
		cache.put(Mp3ToAmrDecoder.TYPE, new Mp3ToAmrDecoder());
	}

	public static Optional<Decoder> getDecoder(String name) {
		if (Strings.isNullOrEmpty(name))
			return Optional.absent();
		if (cache.containsKey(name))
			return Optional.of(cache.get(name));
		return Optional.absent();
	}

	public static String getDecoderName(Decoder decoder) {
		if (decoder instanceof Mp3ToAmrDecoder)
			return Mp3ToAmrDecoder.TYPE;
		if (decoder instanceof AmrToMp3Decoder)
			return AmrToMp3Decoder.TYPE;
		return null;
	}
	
}
