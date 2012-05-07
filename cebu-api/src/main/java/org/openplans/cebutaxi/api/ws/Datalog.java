/* This program is free software: you can redistribute it and/or
 modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation, either version 3 of
 the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>. */

package org.openplans.cebutaxi.api.ws;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.sun.jersey.api.spring.Autowire;

// /ws/datalog is the full path.
// This is the interface used by taxis to log their data.

@Path("/datalog")
@Autowire
public class Datalog {

    private static final Logger LOGGER = Logger.getLogger(Datalog.class.getCanonicalName());

    /**
     */
    @POST
    @Produces( { MediaType.APPLICATION_JSON })
    public Object postLocation(
            @QueryParam("lat") Double lat,
            @QueryParam("lon") Double lon,
            @QueryParam("hdop") Double hdop, //accuracy
            @QueryParam("time") Long time, //time of data collection on taxi
            @QueryParam("id") String id //taxi's id
            ) {
        return null;
    }
    
}
