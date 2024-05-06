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

package org.mycore.externalstore.index.db;

import java.util.List;
import java.util.Optional;

import org.mycore.backend.jpa.MCREntityManagerProvider;

import jakarta.persistence.EntityManager;

/**
 * Abstract base class for a repository.
 *
 * @param <T> repository entity
 */
public abstract class MCRBaseRepository<T> {

    /**
     * Returns {@link EntityManager}.
     *
     * @return entity manager
     */
    protected EntityManager getEntityManager() {
        return MCREntityManagerProvider.getCurrentEntityManager();
    }

    /**
     * Returns {@code Optional} with entity by id.
     *
     * @param id id
     * @return optional with entity
     */
    public abstract Optional<T> find(long id);

    /**
     * Returns a list of all entity elements.
     *
     * @return the list
     */
    public abstract List<T> listAll();

    /**
     * Inserts an object.
     *
     * @param object object.
     */
    public abstract void insert(T object);

    /**
     * Saves an object.
     *
     * @param object object
     */
    public abstract void save(T object);

    /**
     * Deletes an object.
     *
     * @param object object
     */
    public abstract void delete(T object);

}
