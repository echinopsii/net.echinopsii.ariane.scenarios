/**
 * MoM client - init / close a MoM client
 *
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

import java.util.Properties;

public interface MomClient {
    public final static String MOM_HOST = "mom_host.fqdn";
    public final static String MOM_PORT = "mom_host.port";

    public void   init(Properties properties) throws Exception;
    public void   close() throws Exception;

    public Object getConnection();
    public boolean isConnected();
    public MomRequestFactory getRequestFactory();
    public MomServiceFactory getServiceFactory();
}