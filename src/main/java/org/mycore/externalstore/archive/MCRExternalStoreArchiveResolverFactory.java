/*
 * This file is part of ***  M y C o R e  ***
 * See http://www.mycore.de/ for details.
 *
 * MyCoRe is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MyCoRe is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MyCoRe.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mycore.externalstore.archive;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mycore.common.config.MCRConfiguration2;
import org.mycore.common.content.MCRSeekableChannelContent;
import org.mycore.externalstore.MCRExternalStoreFileContent;
import org.mycore.externalstore.exception.MCRExternalStoreException;

/**
 * Provides factory and utility methods for {@link MCRExternalStoreArchiveResolver}.
 */
public class MCRExternalStoreArchiveResolverFactory {

    private static final Logger LOGGER = LogManager.getLogger();

    private static final String CONFIG_PREFIX = "MCR.ExternalStore.ArchiveResolver.";

    private static final Map<String, String> SUFFIX_MAP = new HashMap<String, String>();

    static {
        final Map<String, String> properties = MCRConfiguration2.getSubPropertiesMap(CONFIG_PREFIX);
        properties.keySet().stream().filter(k -> k.endsWith(".Suffix"))
            .map(k -> k.substring(0, k.length() - ".Suffix".length())).forEach(t -> {
                Optional.ofNullable(properties.get(t + ".Suffix")).ifPresentOrElse(p -> SUFFIX_MAP.put(p, t), () -> {
                    throw new MCRExternalStoreException("Suffix required for resolver: " + t);
                });
            });
        LOGGER.debug("Found {} configured archive resolver", SUFFIX_MAP.keySet().size());
    }

    /**
     * Constructs {@link MCRExternalStoreArchiveResolver} with id and {@link MCRExternalStoreFileContent}.
     *
     * @param resolverId resolver id
     * @param content content
     * @return archive resolver
     */
    public static MCRExternalStoreArchiveResolver createResolver(String resolverId, MCRSeekableChannelContent content) {
        final MCRExternalStoreArchiveResolver resolver = MCRConfiguration2
            .<MCRExternalStoreArchiveResolver>getInstanceOf(CONFIG_PREFIX + resolverId + ".Class")
            .orElseThrow();
        resolver.setContent(content);
        return resolver;
    }

    /**
     * Checks if an {@link MCRExternalStoreArchiveResolver} exists for a file by path.
     *
     * @param path path
     * @return archive resolver
     */
    public static boolean checkResolvable(String path) {
        return findResolverId(path).isPresent();
    }

    /**
     * Checks if an {@link MCRExternalStoreArchiveResolver} exists for path that offers downloads.
     *
     * @param path path
     * @return true if there is an archive resolver
     */
    public static boolean checkDownloadable(String path) {
        return findResolverId(path)
            .map(id -> MCRConfiguration2.getBoolean(CONFIG_PREFIX + id + ".Download").orElseThrow()).orElse(false);
    }

    /**
     * Returns a resolver id for given path.
     *
     * @param path path
     * @return resolver id
     */
    public static Optional<String> findResolverId(String path) {
        return MCRExternalStoreArchiveResolverFactory.listAvailableSuffixes().stream().filter(path::endsWith).findAny();
    }

    private static List<String> listAvailableSuffixes() {
        return List.copyOf(SUFFIX_MAP.keySet());
    }

}
