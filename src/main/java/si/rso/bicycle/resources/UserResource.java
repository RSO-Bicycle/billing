package si.rso.bicycle.resources;

import com.kumuluz.ee.streaming.common.annotations.StreamProducer;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.codehaus.jackson.map.util.ISO8601Utils;
import org.hibernate.validator.constraints.Length;
import si.rso.bicycle.entity.UserEntity;
import si.rso.bicycle.schemas.Notification;
import si.rso.bicycle.schemas.NotificationType;
import si.rso.bicycle.schemas.UserCreated;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.*;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * @author Uroš Hercog
 * @since 1.0.0
 */
@RequestScoped
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Path("/users")
public class UserResource {
    private static int DEFAULT_ITERATION_COUNT = 10000;
    private static int DEFAULT_KEY_LENGTH = 256;

    private static final Logger log = Logger.getLogger(UserResource.class.getName());

    @PersistenceContext
    private EntityManager em;

    @Inject
    @StreamProducer
    private Producer producer;

    @Context
    private SecurityContext sc;

    @POST
    @Path("/")
    public Response createUser(@Valid CreateUserRequest request) {
        try {
            // Check if the username is already taken
            this.em.getTransaction().begin();

            Query q = em.createQuery("SELECT COUNT(u) FROM UserEntity u WHERE u.username = :username");
            q.setParameter("username", request.username);
            long c = (long) q.getSingleResult();

            if (c != 0) {
                // Already exists
                // TODO(uh): Return a proper response
                log.info("user " + request.username + " already exists");
                return Response.status(Response.Status.BAD_REQUEST).build();
            }

            // The data is probably valid? Let's create a new request
            UserEntity u = new UserEntity();
            u.setUsername(request.username);


            // Generate random salt
            byte[] salt = new byte[16];
            new Random().nextBytes(salt);

            // Hash the password
            try {
                SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
                PBEKeySpec spec = new PBEKeySpec(request.password.toCharArray(), salt, DEFAULT_ITERATION_COUNT, DEFAULT_KEY_LENGTH);
                u.setPassword(Hex.encodeHexString(skf.generateSecret(spec).getEncoded()));
            } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
                e.printStackTrace();
                // Return an error response. Log this somewhere.
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }


            u.setUid(UUID.randomUUID());
            this.em.persist(u);
            this.em.getTransaction().commit();
            this.em.refresh(u);

            UserCreated uc = UserCreated.newBuilder()
                    .setUid(u.getUid().toString())
                    .setUsername(u.getUsername())
                    .setCreatedAt(ISO8601Utils.format(u.getCreatedAt())).build();

            // Send an event to Kafka, notifying any receiver that a new request was created. Also publish a new command
            // indicating that the account should be activated.
            ProducerRecord<String, String> record = new ProducerRecord<>("usersCreated", u.getUid().toString(), uc.toString());
            this.producer.send(record, (meta, e) -> {
                if (e != null) {
                    e.printStackTrace();
                } else {
                    log.info("publishing new user record successful");
                }
            });
            this.sendActivationCode(u);

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

    @POST
    @Path("/activate")
    public Response activateUser(@NotNull ActivateUserRequest request) {
        try {
            // Get token from query param. Extract the user from the token.
            TypedQuery<UserEntity> tq = this.em.createQuery("SELECT u FROM UserEntity u WHERE u.username = :username", UserEntity.class);
            tq.setParameter("username", request.username);

            UserEntity u = tq.getSingleResult();

            this.activateUser(u, request.code);

            return Response.status(Response.Status.NO_CONTENT).build();
        } catch (InvalidActivationCodeException | UserAlreadyActivated e) {
            // The token was invalid
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }

    private void sendActivationCode(UserEntity user) throws UserAlreadyActivated {
        if (user.isActivated()) {
            throw new UserAlreadyActivated();
        }

        // Generate a new activation code.
        String code = RandomStringUtils.randomAlphanumeric(6).toUpperCase();

        // Store the code. If the entity already has a code set, override it.
        user.setActivationCode(code);
        user.setActivationCodeValidity(DateUtils.addHours(new Date(), 6));

        this.em.getTransaction().begin();
        this.em.persist(user);
        this.em.getTransaction().commit();

        HashMap<CharSequence, CharSequence> m = new HashMap<>();
        m.put("code", code);

        Notification n = new Notification(user.getUid().toString(), NotificationType.ACTIVATION_CODE, m);

        // Create a new send notification event. Limit to email and sms.
        ProducerRecord<String, String> record = new ProducerRecord<>("notifications", user.getUid().toString(), n.toString());
        this.producer.send(record, (meta, e) -> {
            if (e != null) {
                e.printStackTrace();
            } else {
                log.info("publishing activation record successful");
            }
        });
    }

    private void activateUser(UserEntity user, String code) throws UserAlreadyActivated, InvalidActivationCodeException {
        // If the user is already activated, reject it.
        if (user.isActivated()) {
            throw new UserAlreadyActivated();
        }

        // Check if activation code is valid, e.g. the actual value matches and it has not yet expired.
        if (!user.getActivationCode().equals(code) || user.getActivationCodeValidity().before(new Date())) {
            throw new InvalidActivationCodeException();
        }

        user.setActivated(true);

        // If is valid, activate the user
        this.em.getTransaction().begin();
        this.em.persist(user);
        this.em.getTransaction().commit();
    }
}

class InvalidActivationCodeException extends Exception {
}

class UserAlreadyActivated extends Exception {
}

class CreateUserRequest {
    @NotNull(message = "username cannot be omitted")
    @Length(min = 3, max = 50)
    public String username;

    @NotNull(message = "password cannot be omitted")
    @Length(min = 3, max = 50)
    public String password;
}

class ActivateUserRequest {
    @NotNull(message = "username cannot be omitted")
    @Length(min = 3, max = 50)
    public String username;

    @NotNull(message = "code cannot be omitted")
    @Length(min = 6, max = 6)
    public String code;
}