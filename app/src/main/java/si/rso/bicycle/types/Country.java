package si.rso.bicycle.types;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Constraint(validatedBy = {CountryValidator.class})
@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
public @interface Country {
    String message() default "{javax.validation.constraints.Country.message}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};


    /**
     * Defines several {@code @Country} constraints on the same element.
     *
     * @see Country
     */
    @Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
    @Retention(RUNTIME)
    @Documented
    @interface List {
        Country[] value();
    }
}