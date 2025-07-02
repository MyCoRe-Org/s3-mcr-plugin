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

package org.mycore.externalstore.proxy;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.hc.client5.http.utils.URIUtils;
import org.mitre.dsmiley.httpproxy.ProxyServlet;
import org.mycore.common.config.MCRConfiguration2;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.externalstore.MCRExternalStoreService;
import org.mycore.externalstore.exception.MCRExternalStoreException;
import org.mycore.externalstore.exception.MCRExternalStoreNotExistsException;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.UnavailableException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Proxy servlet for external store.
 */
public class MCRExternalStoreProxy extends ProxyServlet {

    private static final long serialVersionUID = 1L;

    @Override
    public String getServletInfo() {
        return "Proxy servlet for external store";
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        final boolean disabled
            = MCRConfiguration2.getBoolean("MCR.ExternalStore.ProxyServlet.Disabled").orElseThrow();
        if (disabled) {
            throw new UnavailableException("ProxyServlet is disabled in configuration");
        }
        super.init(config);
    }

    @Override
    protected void initTarget() throws ServletException {
        // Nothing to do
    }

    @Override
    protected void service(HttpServletRequest servletRequest, HttpServletResponse servletResponse)
        throws ServletException, IOException {
        final String[] pathSplit = servletRequest.getPathInfo().split("/", 3);
        if (pathSplit.length < 3 || !MCRObjectID.isValid(pathSplit[1])) {
            servletResponse.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        final MCRObjectID derivateId = MCRObjectID.getInstance(pathSplit[1]);
        try {
            final URL baseUrl
                = MCRExternalStoreService.getInstance().getStore(derivateId).getStoreProvider().getEndpointUrl();
            servletRequest.setAttribute(ATTR_TARGET_URI, baseUrl.toString());
            servletRequest.setAttribute(ATTR_TARGET_HOST, URIUtils.extractHost(URI.create(baseUrl.toString())));
            super.service(servletRequest, servletResponse);
        } catch (MCRExternalStoreNotExistsException e) {
            servletResponse.sendError(HttpServletResponse.SC_NOT_FOUND);
        } catch (MCRExternalStoreException | URISyntaxException e) {
            servletResponse.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    protected String rewritePathInfoFromRequest(HttpServletRequest servletRequest) {
        return "/" + servletRequest.getPathInfo().split("/", 3)[2];
    }
}
