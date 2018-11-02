package si.rso.bicycle.types;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import org.apache.commons.validator.routines.EmailValidator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class UsernameValidator implements ConstraintValidator<Username, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
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
