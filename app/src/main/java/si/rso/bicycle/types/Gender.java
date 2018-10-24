package si.rso.bicycle.types;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Constraint(validatedBy = {GenderValidator.class})
@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
public @interface Gender {
    String message() default "{javax.validation.constraints.Gender.message}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};


    /**
     * Defines several {@code @Gender} constraints on the same element.
     *
     * @see Gender
     */
    @Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
    @Retention(RUNTIME)
    @Documented
    @interface List {
        Gender[] value();
    }
}