package com.legooframework.model.upload.decoder;

import java.io.File;

import com.google.common.base.Optional;

public interface Decoder {

	Optional<File> decode(File orginFile);

}
