/**
 * This file is part of Graylog.
 *
 * Graylog is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Graylog is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Graylog.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.graylog2.shared.rest;

import org.apache.shiro.subject.Subject;
import org.graylog2.jersey.container.netty.NettyContainer;
import org.graylog2.shared.security.ShiroSecurityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.HttpHeaders;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Date;

public class RestAccessLogFilter implements ContainerResponseFilter {
    private static final Logger LOG = LoggerFactory.getLogger("org.graylog2.rest.accesslog");
    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        if (!LOG.isDebugEnabled()) return;
        try {
            final InetSocketAddress remoteAddr = (InetSocketAddress) requestContext.getProperty(NettyContainer.REQUEST_PROPERTY_REMOTE_ADDR);

            final String rawQuery = requestContext.getUriInfo().getRequestUri().getRawQuery();

            final ShiroSecurityContext securityContext = (ShiroSecurityContext) requestContext.getSecurityContext();
            String remoteUser = null;
            if (securityContext.getUserPrincipal() != null) {
                final Subject subject = securityContext.getSubject();
                if (subject.getPrincipal() == null) {
                    remoteUser = "-";
                } else {
                    remoteUser = securityContext.getUserPrincipal().getName();
                }
            }
            final Date requestDate = requestContext.getDate();
            final boolean sessionExtended = requestContext.getHeaders().getFirst("X-Graylog2-No-Session-Extension") == null;
            LOG.debug("{} {} [{}] \"{} {}{}\" {} (Session extended: {}) {} {}",
                      remoteAddr.getAddress().getHostAddress(),
                      (remoteUser == null ? "-" : remoteUser),
                      (requestDate == null ? "-" : requestDate),
                      requestContext.getMethod(),
                      requestContext.getUriInfo().getPath(),
                      (rawQuery == null ? "" : "?" + rawQuery),
                      requestContext.getHeaderString(HttpHeaders.USER_AGENT),
                      (sessionExtended ? "true" : "false"),
                      responseContext.getStatus(),
                      responseContext.getLength());
        } catch (Exception ignored) {
            LOG.error(":(", ignored);
        }
    }

}
