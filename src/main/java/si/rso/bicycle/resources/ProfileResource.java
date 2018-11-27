package si.rso.bicycle.resources;

import com.kumuluz.ee.streaming.common.annotations.StreamProducer;
import org.apache.kafka.clients.producer.Producer;
import org.hibernate.validator.constraints.Length;
import si.rso.bicycle.entity.ProfileEntity;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.*;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Date;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * @author Uroš Hercog
 * @since 1.0.0
 */
@RequestScoped
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Path("/profiles")
public class ProfileResource {
    private static final Logger log = Logger.getLogger(ProfileResource.class.getName());

    @PersistenceContext
    private EntityManager em;

    @Inject
    @StreamProducer
    private Producer producer;

    @GET
    @Path("/me")
    public Response getProfile() {
        // Fetch the last profile
        try {
            String userUuid = "";
            TypedQuery<ProfileEntity> pe = this.em.createQuery("SELECT p FROM ProfileEntity p WHERE p.user_uuid = :user ORDER BY p.created_at DESC LIMIT 1", ProfileEntity.class);
            pe.setParameter("user", userUuid);
            ProfileEntity p = pe.getSingleResult();
            return Response.status(200).entity(p).build();
        } catch (Exception e) {
            log.warning(e.getMessage());
            return Response.status(400).build();
        }
    }

    @POST
    @Path("/")
    public Response createProfile(@Valid CreateProfileRequest request) {
        // Create a new profile
        try {
            // Check if the username is already taken
            this.em.getTransaction().begin();


            ProfileEntity p = new ProfileEntity();
            p.setUid(UUID.randomUUID().toString());
            p.setFirstName(request.firstName);
            p.setLastName(request.lastName);
            p.setSex(request.sex);
            p.setBirthDay(request.birthDay);
            p.setAddressStreet(request.addressStreet);
            p.setAddressCity(request.addressCity);
            p.setAddressPostalCode(request.addressPostalCode);
            p.setAddressCountryCode(request.addressCountryCode);

            this.em.persist(p);
            this.em.getTransaction().commit();
            this.em.refresh(p);
            return Response.status(Response.Status.CREATED).build();
        } catch (ConstraintViolationException e) {
            for (ConstraintViolation<?> cv : e.getConstraintViolations()) {
                System.err.println(cv.getRootBeanClass().getName() + "." + cv.getPropertyPath() + " " + cv.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
}

class CreateProfileRequest {
    @NotNull(message = "firstName cannot be omitted")
    @Length(min = 3, max = 50)
    public String firstName;

    @NotNull(message = "lastName cannot be omitted")
    @Length(min = 3, max = 50)
    public String lastName;

    @NotNull(message = "sex cannot be omitted")
    @Length(min = 1, max = 1)
    public String sex;

    @NotNull(message = "birthDay cannot be omitted")
    public Date birthDay;

    @NotNull(message = "addressStreet cannot be omitted")
    @Length(min = 3, max = 50)
    public String addressStreet;

    @NotNull(message = "addressCity cannot be omitted")
    @Length(min = 3, max = 50)
    public String addressCity;

    @NotNull(message = "addressPostalCode cannot be omitted")
    @Length(min = 3, max = 50)
    public String addressPostalCode;

    @NotNull(message = "addressCountryCode cannot be omitted")
    @Length(min = 3, max = 3)
    public String addressCountryCode;
}