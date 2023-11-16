package org.mycore.externalstore.model;

import java.util.ArrayList;
import java.util.List;

public class MCRExternalStoreArchiveInfo {

    private String path;

    public List<MCRExternalStoreFileInfo> files = new ArrayList<MCRExternalStoreFileInfo>();

    public MCRExternalStoreArchiveInfo() {

    }

    public MCRExternalStoreArchiveInfo(String path) {
        this.path = path;
    }

    public void setFiles(List<MCRExternalStoreFileInfo> files) {
        this.files = files;
    }

    public List<MCRExternalStoreFileInfo> getFiles() {
        return files;
    }

    public void addFile(MCRExternalStoreFileInfo file) {
        files.add(file);
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
