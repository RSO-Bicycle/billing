package si.rso.bicycle;

import com.kumuluz.ee.jwt.auth.feature.JWTRolesAllowedDynamicFeature;
import com.kumuluz.ee.jwt.auth.filter.JWTAuthorizationFilter;
import si.rso.bicycle.resources.BillingResource;

import javax.ws.rs.ApplicationPath;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Uroš Hercog
 * @since 1.0.0
 */
@ApplicationPath("v1")
public class Application extends javax.ws.rs.core.Application {
    @Override
    public Set<Class<?>> getClasses() {

        Set<Class<?>> classes = new HashSet<>();

        classes.add(JWTAuthorizationFilter.class);
        classes.add(JWTRolesAllowedDynamicFeature.class);

        // resources
        classes.add(BillingResource.class);

        return classes;
    }
}