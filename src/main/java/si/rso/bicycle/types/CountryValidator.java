package si.rso.bicycle.types;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Locale;

public class CountryValidator implements ConstraintValidator<Country, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        for (String s : Locale.getISOCountries()) {
            if (value.equals(s)) {
                return true;
            }
        }
        return false;
    }
}
