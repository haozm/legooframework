package com.legooframework.model.core.config;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSelectInfo;
import org.apache.commons.vfs2.FileSelector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class MonitorFileSystem {

    private static final Logger logger = LoggerFactory.getLogger(MonitorFileSystem.class);

    private final FileObject fileObject;

    public MonitorFileSystem(FileObject fileObject) {
        this.fileObject = fileObject;
    }

    public Optional<Collection<File>> findFiles(List<String> patterns) {
        FileSelector selector = new PatternMatchSelector(patterns);
        try {
            FileObject[] fileObjects = this.fileObject.findFiles(selector);
            if (ArrayUtils.isEmpty(fileObjects)) return Optional.empty();
            List<File> files = Lists.newArrayListWithCapacity(fileObjects.length);
            for (FileObject $it : fileObjects) {
                if ($it.isFile()) files.add(ResourceUtils.getFile($it.getURL()));
            }
            return Optional.of(files);
        } catch (Exception e) {
            String msg = String.format("findFiles(String %s) has error", patterns);
            logger.error(msg, e);
            throw new RuntimeException(msg, e);
        }
    }

    class PatternMatchSelector implements FileSelector {

        String[] patterns;

        PatternMatchSelector(List<String> patterns) {
            this.patterns = patterns.toArray(new String[0]);
        }

        @Override
        public boolean includeFile(FileSelectInfo fileSelectInfo) throws Exception {
            if (fileSelectInfo.getFile().isFile()) {
                String file_path = fileSelectInfo.getFile().getName().getPath();
                return PathMatcherUtil.match(patterns, file_path);
            }
            return false;
        }

        @Override
        public boolean traverseDescendents(FileSelectInfo fileSelectInfo) {
            return true;
        }
    }

}
