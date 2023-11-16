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
import org.mycore.common.MCRSession;
import org.mycore.common.MCRSessionMgr;
import org.mycore.common.MCRSystemUserInformation;
import org.mycore.common.MCRUserInformation;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.externalstore.index.db.model.MCRExternalStoreInfoData;
import org.mycore.services.queuedjob.MCRJob;
import org.mycore.services.queuedjob.MCRJobAction;
import org.mycore.util.concurrent.MCRTransactionableCallable;

/**
 * A {@link MCRJobAction} which creates and saves {@link MCRExternalStoreInfoData}.
 */
public class MCRExternalStoreCreateInfoJobAction extends MCRJobAction {

    private final Logger LOGGER = LogManager.getLogger();

    /**
     * Derivate id property name.
     */
    private static final String DERIVATE_ID = "derivate_id";

    @SuppressWarnings("PMD.UnnecessaryConstructor")
    public MCRExternalStoreCreateInfoJobAction() {

    }

    public MCRExternalStoreCreateInfoJobAction(MCRJob job) {
        super(job);
    }

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
        LOGGER.debug("Embedding {} store to {}", derivateId);
        final MCRSession session = MCRSessionMgr.getCurrentSession();
        MCRUserInformation savedUserInformation = session.getUserInformation();
        session.setUserInformation(MCRSystemUserInformation.getGuestInstance());
        session.setUserInformation(MCRSystemUserInformation.getJanitorInstance());
        try {
            new MCRTransactionableCallable<Boolean>(() -> {
                MCRExternalStoreService.createStoreInfo(derivateId);
                return true;
            }).call();
            LOGGER.debug("Finished embedding {} store to {}", derivateId);
        } catch (Exception e) {
            LOGGER.error("There was an Error while embedding {} store to {}", derivateId);
            throw new ExecutionException(e);
        } finally {
            session.setUserInformation(MCRSystemUserInformation.getGuestInstance());
            session.setUserInformation(savedUserInformation);
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
