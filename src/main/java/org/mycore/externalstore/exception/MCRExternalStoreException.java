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

package org.mycore.externalstore.exception;

import java.io.Serial;

import org.mycore.common.MCRException;

/**
 * General external store exception.
 */
public class MCRExternalStoreException extends MCRException {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Constructs an exception with message.
     *
     * @param message message
     */
    public MCRExternalStoreException(String message) {
        super(message);
    }

    /**
     * Constructs an exception with message and cause.
     *
     * @param message message
     * @param cause cause
     */
    public MCRExternalStoreException(String message, Throwable cause) {
        super(message, cause);
    }

}
