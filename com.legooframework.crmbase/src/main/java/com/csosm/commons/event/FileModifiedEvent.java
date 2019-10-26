package com.csosm.commons.event;

import com.csosm.commons.adapter.LoginUserContext;
import com.google.common.base.MoreObjects;
import org.apache.commons.vfs2.FileObject;

public class FileModifiedEvent implements BusEvent {

    private final FileObject file;
    private final int action;

    private FileModifiedEvent(int action, FileObject file) {
        this.file = file;
        this.action = action;
    }

    @Override
    public void setLoginUser(LoginUserContext user) {

    }

    public static FileModifiedEvent addFileEvent(FileObject file) {
        return new FileModifiedEvent(1, file);
    }

    public static FileModifiedEvent updateFileEvent(FileObject file) {
        return new FileModifiedEvent(0, file);
    }

    public static FileModifiedEvent deleteFileEvent(FileObject file) {
        return new FileModifiedEvent(-1, file);
    }

    public boolean isAdd() {
        return 1 == action;
    }

    public boolean isUpdate() {
        return 0 == action;
    }

    public boolean isDelete() {
        return -1 == action;
    }

    public FileObject getFile() {
        return file;
    }

    public String getFileName() {
        return file.getName().getBaseName();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("action", action)
                .add("file", file).toString();
    }
}
