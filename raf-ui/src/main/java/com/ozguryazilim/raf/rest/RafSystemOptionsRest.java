package com.ozguryazilim.raf.rest;

import com.ozguryazilim.raf.ReadOnlyModeService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Arrays;
import java.util.List;

@RequiresPermissions("admin")
@Path("/api/system-option")
public class RafSystemOptionsRest {

    private static final Logger LOG = LoggerFactory.getLogger(RafSystemOptionsRest.class);

    @Inject
    private ReadOnlyModeService readOnlyModeService;

    @GET
    @Path("/read-only/state")
    @Produces(MediaType.APPLICATION_JSON)
    public SystemOptionState getReadOnlyModeState() {
        return getReadOnlyModeSystemOptionState();
    }

    @PUT
    @Path("/read-only/state")
    @Produces(MediaType.APPLICATION_JSON)
    public SystemOptionState setReadOnlyModeState(@FormParam("state") String state) {
        if (!readOnlyModeService.isValidState(ReadOnlyModeService.ReadOnlyState.RAF_READ_ONLY, state)) {
            LOG.info("Requested state is not valid.");
            throw new BadRequestException();
        }
        readOnlyModeService.setState(ReadOnlyModeService.ReadOnlyState.RAF_READ_ONLY, state);
        return getReadOnlyModeSystemOptionState();
    }

    private SystemOptionState getReadOnlyModeSystemOptionState() {
        return new SystemOptionState(
                ReadOnlyModeService.ReadOnlyState.RAF_READ_ONLY.getDefaultValue(),
                readOnlyModeService.getState(ReadOnlyModeService.ReadOnlyState.RAF_READ_ONLY),
                Arrays.asList(ReadOnlyModeService.ReadOnlyState.RAF_READ_ONLY.getValues())
            );
    }

    public class SystemOptionState {
        private final String defaultState;
        private final String currentState;
        private final List<String> possibleStates;

        public SystemOptionState(String defaultState, String currentState, List<String> posibleStates) {
            this.defaultState = defaultState;
            this.currentState = currentState;
            this.possibleStates = posibleStates;
        }

        public String getDefaultState() {
            return defaultState;
        }

        public String getCurrentState() {
            return currentState;
        }

        public List<String> getPossibleStates() {
            return possibleStates;
        }
    }
}
