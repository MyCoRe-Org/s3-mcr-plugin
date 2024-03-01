package org.mycore.externalstore.model;

import java.util.ArrayList;
import java.util.List;

/**
 * An {@link MCRExternalStoreArchiveInfo} describes an archive.
 */
public class MCRExternalStoreArchiveInfo {

    private String path;

    private List<MCRExternalStoreFileInfo> fileInfos = new ArrayList<MCRExternalStoreFileInfo>();

    /**
     * Creates an archive info.
     */
    public MCRExternalStoreArchiveInfo() {

    }

    /**
     * Create an archive info with path.
     *
     * @param path path
     */
    public MCRExternalStoreArchiveInfo(String path) {
        this.path = path;
    }

    /**
     * Returns path.
     *
     * @return path
     */
    public String getPath() {
        return path;
    }

    /**
     * Sets path.
     *
     * @param path path
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * Sets list of {@link MCRExternalStoreFileInfo} elements.
     *
     * @param fileInfos list of file info elements
     */
    public void setFileInfos(List<MCRExternalStoreFileInfo> fileInfos) {
        this.fileInfos = fileInfos;
    }

    /**
     * Returns a list of {@link MCRExternalStoreFileInfo} elements.
     *
     * @return list of file info elements
     */
    public List<MCRExternalStoreFileInfo> getFileInfos() {
        return fileInfos;
    }

    /**
     * Adds {@link MCRExternalStoreFileInfo} to file info list.
     *
     * @param fileInfo file info
     */
    public void addFile(MCRExternalStoreFileInfo fileInfo) {
        fileInfos.add(fileInfo);
    }

}
