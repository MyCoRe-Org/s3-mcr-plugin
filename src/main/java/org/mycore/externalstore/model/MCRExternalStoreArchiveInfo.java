package org.mycore.externalstore.model;

import java.util.List;

/**
 * An {@link MCRExternalStoreArchiveInfo} describes an archive.
 *
 * @param path path
 * @param fileInfos list over file info elements
 */
public record MCRExternalStoreArchiveInfo(String path, List<MCRExternalStoreFileInfo> fileInfos) {
}
