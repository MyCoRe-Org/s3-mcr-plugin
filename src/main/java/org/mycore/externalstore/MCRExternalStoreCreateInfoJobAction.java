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

import java.util.concurrent.ExecutionException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.services.queuedjob.MCRJob;
import org.mycore.services.queuedjob.MCRJobAction;

/**
 * A {@link MCRJobAction} which creates and saves job info.
 */
public class MCRExternalStoreCreateInfoJobAction extends MCRJobAction {

    private static final Logger LOGGER = LogManager.getLogger();

    /**
     * Derivate id property name.
     */
    private static final String DERIVATE_ID = "derivate_id";

    /**
     * Constucts new job action with job.
     *
     * @param job job
     */
    public MCRExternalStoreCreateInfoJobAction(MCRJob job) {
        super(job);
    }

    /**
     * Creates new job with derivate id.
     *
     * @param derivateId derivate id
     * @return job
     */
    public static MCRJob createJob(MCRObjectID derivateId) {
        final MCRJob job = new MCRJob(MCRExternalStoreCreateInfoJobAction.class);
        job.setParameter(DERIVATE_ID, derivateId.toString());
        return job;
    }

    @Override
    public boolean isActivated() {
        return true;
    }

    @Override
    public String name() {
        return getClass().getName();
    }

    @Override
    public void execute() throws ExecutionException {
        final MCRObjectID derivateId = getDerivateId();
        LOGGER.debug("Embedding external store to {}", derivateId);
        try {
            MCRExternalStoreService.createStoreInfo(derivateId);
            LOGGER.debug("Finished embedding external store to {}", derivateId);
        } catch (Exception e) {
            LOGGER.error("There was an Error while embedding external store to {}", derivateId);
            throw new ExecutionException(e);
        }
    }

    @Override
    public void rollback() {
        // nothing to rollback
    }

    private MCRObjectID getDerivateId() {
        return MCRObjectID.getInstance(job.getParameter(DERIVATE_ID));
    }

}
