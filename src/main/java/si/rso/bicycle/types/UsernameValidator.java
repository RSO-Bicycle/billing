package si.rso.bicycle.types;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import org.apache.commons.validator.routines.EmailValidator;
import si.rso.bicycle.resources.UserResource;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.logging.Logger;

public class UsernameValidator implements ConstraintValidator<Username, String> {

    private static final Logger log = Logger.getLogger(UsernameValidator.class.getName());

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        log.info("validating username");
        // Check that the value matches either an email or a phone number.
        if (EmailValidator.getInstance().isValid(value)) {
            return true;
        }

        try {
            PhoneNumberUtil.getInstance().parse(value, "SL");
        } catch (NumberParseException e) {
            return false;
        }
        return false;
    }
}
