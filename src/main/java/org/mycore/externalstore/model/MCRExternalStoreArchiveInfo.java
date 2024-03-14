package org.mycore.externalstore.model;

import java.util.List;

/**
 * An {@link MCRExternalStoreArchiveInfo} describes an archive.
 */
public record MCRExternalStoreArchiveInfo(String path, List<MCRExternalStoreFileInfo> fileInfos) {
}
