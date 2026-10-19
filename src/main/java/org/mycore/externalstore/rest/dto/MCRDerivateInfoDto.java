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

package org.mycore.externalstore.rest.dto;

import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Dto for derivate info.
 *
 * @param id derivate id
 * @param type store type
 * @param titles list over derivate title elements
 * @param metadata store settings without secrets, only for users with write permission
 * @param capabilities what the current user can do with the store
 */
public record MCRDerivateInfoDto(@JsonProperty("id") String id, @JsonProperty("type") String type,
    @JsonProperty("titles") List<MCRDerivateTitleDto> titles,
    @JsonProperty("metadata") MCRExternalStoreSettingsDto metadata,
    @JsonProperty("capabilities") Set<MCRStoreCapability> capabilities) {
}
