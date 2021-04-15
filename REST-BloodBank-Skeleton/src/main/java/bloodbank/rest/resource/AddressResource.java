package bloodbank.rest.resource;

import static bloodbank.utility.MyConstants.ADMIN_ROLE;
import static bloodbank.utility.MyConstants.CUSTOMER_ADDRESS_RESOURCE_PATH;
import static bloodbank.utility.MyConstants.CUSTOMER_ADDRESS_SUBRESOURCE_NAME;
import static bloodbank.utility.MyConstants.RESOURCE_PATH_ID_ELEMENT;
import static bloodbank.utility.MyConstants.RESOURCE_PATH_ID_PATH;
import static bloodbank.utility.MyConstants.USER_ROLE;

import java.util.List;

import javax.ws.rs.core.Response.Status;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.inject.Inject;
import javax.security.enterprise.SecurityContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.soteria.WrappingCallerPrincipal;

import bloodbank.ejb.BloodBankService;
import bloodbank.entity.Address;
import bloodbank.entity.Person;
import bloodbank.entity.SecurityUser;

@Path( CUSTOMER_ADDRESS_SUBRESOURCE_NAME)
@Consumes( MediaType.APPLICATION_JSON)
@Produces( MediaType.APPLICATION_JSON)
public class AddressResource {

}
