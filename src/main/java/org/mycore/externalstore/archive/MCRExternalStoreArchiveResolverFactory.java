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

    private static Map<String, String> suffixMap = new HashMap<String, String>();

    static {
        final Map<String, String> properties = MCRConfiguration2.getSubPropertiesMap(CONFIG_PREFIX);
        properties.entrySet().stream().map(Map.Entry::getKey).filter(k -> k.endsWith(".Suffix"))
            .map(k -> k.substring(0, k.length() - ".Suffix".length())).forEach(t -> {
                Optional.ofNullable(properties.get(t + ".Suffix")).ifPresentOrElse(p -> suffixMap.put(p, t),
                    () -> new MCRExternalStoreException("Suffix required for resolver: " + t));
            });
        LOGGER.debug("Found {} configured archive resolver", suffixMap.keySet().size());
    }

    /**
     * Creates {@link MCRExternalStoreArchiveResolver} with id and {@link MCRExternalStoreFileContent}.
     *
     * @param id the resolver id
     * @param content the content
     * @return the resolver
     */
    public static MCRExternalStoreArchiveResolver createResolver(String id, MCRSeekableChannelContent content) {
        final MCRExternalStoreArchiveResolver resolver
            = MCRConfiguration2.<MCRExternalStoreArchiveResolver>getInstanceOf(CONFIG_PREFIX + id + ".Class")
                .orElseThrow();
        resolver.setContent(content);
        return resolver;
    }

    /**
     * Checks if there is {@link MCRExternalStoreArchiveResolver} for given path.
     *
     * @param path the path
     * @return the resolver instance
     */
    public static boolean checkResolveable(String path) {
        LOGGER.debug("Searching resolver for path: {}", path);
        return findResolverId(path).isPresent();
    }

    /**
     * Checks whether {@link MCRExternalStoreArchiveResolver} exists that offers downloads.
     *
     * @param path the path
     * @return true is resolver exists
     */
    public static boolean checkDownloadable(String path) {
        final Optional<String> resolverId = findResolverId(path);
        if (resolverId.isEmpty()) {
            return false;
        }
        return MCRConfiguration2.getBoolean(CONFIG_PREFIX + resolverId.get() + ".Download").orElseThrow();
    }

    /**
     * Returns resolver id for given path.
     *
     * @param path the path
     * @return resolver type
     */
    public static Optional<String> findResolverId(String path) {
        return MCRExternalStoreArchiveResolverFactory.listAvailableSuffixes().stream()
            .filter(s -> path.endsWith(s)).findAny();
    }

    private static List<String> listAvailableSuffixes() {
        return List.copyOf(suffixMap.keySet());
    }

}
