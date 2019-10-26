package com.csosm.commons.vfs;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSelectInfo;
import org.apache.commons.vfs2.FileSelector;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.util.List;
import java.util.regex.Pattern;

public class MonitorFileSystem {

    private final FileObject fileObject;

    public MonitorFileSystem(FileObject fileObject) {
        this.fileObject = fileObject;
    }

    public Optional<List<File>> getAllChildren() {
        try {
            FileObject[] files = fileObject.getChildren();
            if (ArrayUtils.isEmpty(files)) return Optional.absent();
            List<File> fileList = Lists.newArrayListWithCapacity(files.length);
            for (FileObject $it : files) {
                fileList.add(ResourceUtils.getFile($it.getURL()));
            }
            return Optional.of(fileList);
        } catch (Exception e) {
            throw new RuntimeException("getAllChildren files by %s has error.", e);
        }
    }

    public Optional<List<File>> findFiles(String endsWith) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(endsWith), "String endsWith can not be empty...");
        FileSelector selector = new EndsWithFileSelector(endsWith);
        try {
            FileObject[] files = fileObject.findFiles(selector);
            if (ArrayUtils.isEmpty(files)) return Optional.absent();
            List<File> fileList = Lists.newArrayListWithCapacity(files.length);
            for (FileObject $it : files) {
                fileList.add(ResourceUtils.getFile($it.getURL()));
            }
            return Optional.of(fileList);
        } catch (Exception e) {
            throw new RuntimeException(String.format("find files by %s has error.", endsWith), e);
        }
    }

    class EndsWithFileSelector implements FileSelector {
        private Pattern pattern;
        String endsWith;

        EndsWithFileSelector(String endsWith) {
            this.pattern = Pattern.compile(endsWith);
        }

        @Override
        public boolean includeFile(FileSelectInfo fileSelectInfo) throws Exception {
            if (fileSelectInfo.getFile().isFile()) {
                String file_path = fileSelectInfo.getFile().getName().getPath();
                return pattern.matcher(file_path).matches();
            }
            return false;
        }

        @Override
        public boolean traverseDescendents(FileSelectInfo fileSelectInfo) throws Exception {
            return true;
        }
    }

}
