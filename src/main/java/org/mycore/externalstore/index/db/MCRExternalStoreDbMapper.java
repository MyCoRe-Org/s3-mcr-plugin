package org.mycore.externalstore.index.db;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.mycore.externalstore.index.db.model.MCRExternalStoreFileInfoData;
import org.mycore.externalstore.model.MCRExternalStoreFileInfo;

/**
 * Provides methods to map between data and domain.
 */
public class MCRExternalStoreDbMapper {

    /**
     * Maps and returns {@link MCRExternalStoreFileInfo} from {@link MCRExternalStoreFileInfoData}.
     *
     * @param fileInfoData file info data
     * @return file info
     */
    public static MCRExternalStoreFileInfo toDomain(MCRExternalStoreFileInfoData fileInfoData) {
        final Set<MCRExternalStoreFileInfo.FileFlag> flags
            = fileInfoData.getFlags().stream().map(MCRExternalStoreDbMapper::toDomain).collect(Collectors.toSet());
        final MCRExternalStoreFileInfo.Builder result
            = new MCRExternalStoreFileInfo.Builder(fileInfoData.getName(), fileInfoData.getParentPath())
                .checksum(fileInfoData.getChecksum()).directory(fileInfoData.isDirectory()).size(fileInfoData.getSize())
                .flags(flags);
        Optional.ofNullable(fileInfoData.getLastModified()).map(MCRExternalStoreDbMapper::convertToDate)
            .ifPresent(result::lastModified);
        return result.build();
    }

    /**
     * Maps and returns {@link MCRExternalStoreFileInfoData} from {@link MCRExternalStoreFileInfo}.
     *
     * @param fileInfo file info
     * @return file info data
     */
    protected static MCRExternalStoreFileInfoData toData(MCRExternalStoreFileInfo fileInfo) {
        final MCRExternalStoreFileInfoData data = new MCRExternalStoreFileInfoData();
        data.setParentPath(fileInfo.parentPath());
        data.setName(fileInfo.name());
        data.setChecksum(fileInfo.checksum());
        data.setDirectory(fileInfo.isDirectory());
        Optional.ofNullable(fileInfo.lastModified()).map(MCRExternalStoreDbMapper::convertToLocalDateTime)
            .ifPresent(data::setLastModified);
        data.setSize(fileInfo.size());
        final Set<String> flags
            = fileInfo.flags().stream().map(MCRExternalStoreDbMapper::toData).collect(Collectors.toSet());
        data.setFlags(flags);
        return data;
    }

    /**
     * Maps and returns a string from {@link MCRExternalStoreFileInfo}.
     *
     * @param fileFlag flag
     * @return string
     */
    protected static String toData(MCRExternalStoreFileInfo.FileFlag fileFlag) {
        return fileFlag.toString();
    }

    /**
     * Maps and returns {@link MCRExternalStoreFileInfo.FileFlag} from string.
     *
     * @param flag string
     * @return file flag
     */
    protected static MCRExternalStoreFileInfo.FileFlag toDomain(String flag) {
        return MCRExternalStoreFileInfo.FileFlag.valueOf(flag);
    }

    private static LocalDateTime convertToLocalDateTime(Date date) {
        final Instant instant = Instant.ofEpochMilli(date.getTime());
        return LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
    }

    private static Date convertToDate(LocalDateTime dateTime) {
        return Date.from(dateTime.toInstant(ZoneOffset.UTC));
    }

}
