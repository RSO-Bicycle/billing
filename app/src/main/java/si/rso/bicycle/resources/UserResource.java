package si.rso.bicycle.resources;

import com.kumuluz.ee.streaming.common.annotations.StreamProducer;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.hibernate.validator.constraints.Length;
import si.rso.bicycle.entities.UserEntity;
import si.rso.bicycle.types.Password;
import si.rso.bicycle.types.Username;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Date;
import java.util.Random;

/**
 * @author Uroš Hercog
 * @since 1.0.0
 */
@RequestScoped
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Path("/")
public class UserResource {
    private static int DEFAULT_ITERATION_COUNT = 10000;
    private static int DEFAULT_KEY_LENGTH = 256;


    @PersistenceContext
    private EntityManager em;

    @Inject
    @StreamProducer
    private Producer<String, String> producer;

    @POST
    @Path("/")
    public Response createUser(@NotNull CreateUserRequest request) {
        // Check if the username is already taken
        this.em.getTransaction().begin();

        Query q = em.createQuery("SELECT COUNT(id) FROM UserEntity u where username = :username");
        q.setParameter("username", request.username);
        long c = (long) q.getSingleResult();

        if (c != 0) {
            // Already exists
            // TODO(uh): Return a proper response
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
            u.setPassword(new String(skf.generateSecret(spec).getEncoded()));
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
            // Return an error response. Log this somewhere.
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }

        this.em.persist(u);
        this.em.refresh(u);

        // Send an event to Kafka, notifying any receiver that a new request was created. Also publish a new command
        // indicating that the account should be activated.
        ProducerRecord<String, String> record = new ProducerRecord<>("users", u.getUid().toString(), null);
        this.producer.send(record);
        this.sendActivationCode(u);

        // Issue a new bearer token and send it in the request. This token must be present in any subsequent request.
        return Response.status(Response.Status.CREATED).build();
    }

    @POST
    @Path("/activate")
    public Response activateUser(@NotNull ActivateUserRequest request) {
        // Get token from query param. Extract the user from the token.
        UserEntity u = new UserEntity();

        try {
            this.activateUser(u, request.code);
        } catch (InvalidActivationCodeException | UserAlreadyActivated e) {
            // The token was invalid
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        return Response.status(Response.Status.NO_CONTENT).build();
    }

    private void sendActivationCode(UserEntity user) {
        // Generate a new activation code.
        String code = RandomStringUtils.randomAlphanumeric(8);

        // Store the code. If the entity already has a code set, override it.
        user.setActivationCode(code);
        this.em.persist(user);

        // Create a new send notification event. Limit to email and sms.
        ProducerRecord<String, String> record = new ProducerRecord<>("notifications", user.getUid().toString(), null);
        this.producer.send(record);
    }

    private void activateUser(UserEntity user, String code) throws UserAlreadyActivated, InvalidActivationCodeException {
        // If the user is already activated, reject it.
        if (user.isActivated()) {
            throw new UserAlreadyActivated();
        }

        // Check if activation code is valid, e.g. the actual value matches and it has not yet expired.
        if (user.getActivationCode().equals(code) && user.getActivationCodeValidity().before(new Date())) {
            throw new InvalidActivationCodeException();
        }

        // If is valid, activate the user
        user.setActivated(true);
        this.em.persist(user);
    }
}

class InvalidActivationCodeException extends Exception {
}

class UserAlreadyActivated extends Exception {
}

class CreateUserRequest {
    @NotEmpty
    @Length(max = 255)
    @Username String username;

    @NotEmpty
    @Password String password;
}

class ActivateUserRequest {
    @NotEmpty
    @Length(min = 8, max = 8)
    public String code;
}