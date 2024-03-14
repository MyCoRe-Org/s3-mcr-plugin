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

package org.mycore.externalstore;

import java.util.Map;

import org.mycore.common.config.MCRConfiguration2;

/**
 * This class provides factory methods for {@link MCRExternalStoreProvider}.
 */
public class MCRExternalStoreProviderFactory {

    /**
     * Constructs an {@link MCRExternalStoreProvider} for given store type and provider settings.
     *
     * @param storeType store type
     * @param storeProviderSettings provider settings
     * @return store provider
     */
    public static MCRExternalStoreProvider createStoreProvider(String storeType,
        Map<String, String> storeProviderSettings) {
        final MCRExternalStoreProvider provider
            = MCRConfiguration2.<MCRExternalStoreProvider>getInstanceOf(getProperty(storeType)).orElseThrow();
        provider.init(storeProviderSettings);
        return provider;
    }

    private static String getProperty(String storeType) {
        return "MCR.ExternalStore." + storeType + ".Provider.Class";
    }
}
