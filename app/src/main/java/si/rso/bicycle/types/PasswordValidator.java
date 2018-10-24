package si.rso.bicycle.types;


import com.codahale.passpol.BreachDatabase;
import com.codahale.passpol.PasswordPolicy;
import com.codahale.passpol.Status;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class PasswordValidator implements ConstraintValidator<Password, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        final PasswordPolicy policy = new PasswordPolicy(BreachDatabase.haveIBeenPwned(), 8, 64);
        return policy.check(value) == Status.OK;
    }
}
