/**
 * MomServiceFactory - provides a service
 * Copyright (C) 8/24/14 echinopsii
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.echinopsii.ariane.community.scenarios.momcli;

import java.util.List;

public interface MomServiceFactory<SRV, C extends AppMsgWorker, F extends AppFeeder, D, S> {
    /**
     * request worker from a source
     * @param source the source where request are coming from
     * @param requestCB the application request worker
     * @return service
     */
    public SRV requestService(S source, C requestCB);

    /**
     * feed message to a destination
     * @param destination the destination (must be a topic)
     * @param feederCB the application feeder building the message to feed
     * @return service ref
     */
    public SRV feederService(D destination, F feederCB);

    /**
     * receive message from a feed source
     * @param source the feed source
     * @param feedCB the feed message worker
     * @return service ref
     */
    public SRV subscriberService(S source, C feedCB);

    /**
     * get actor(s) of this service
     *
     * @return the service list
     */
    public List<SRV> getServices();
}