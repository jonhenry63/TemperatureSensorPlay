package views.formdata;

import play.data.validation.ValidationError;
import java.util.ArrayList;
import java.util.List;
import models.UserInfoDB;

/**
 * Backing class for the login form.
 */
public class RegisterFormData {

    /** The submitted email. */
    public String email = "";
    /** The submitted password. */
    public String password = "";

    /** Required for form instantiation. */
    public RegisterFormData() {
    }

    /**
     * Validates Form<LoginFormData>.
     * Called automatically in the controller by bindFromRequest().
     * Checks to see that email and password are valid credentials.
     * @return Null if valid, or a List[ValidationError] if problems found.
     */
    public List<ValidationError> validate() {

        List<ValidationError> errors = new ArrayList<>();

        if (UserInfoDB.isUser(email)) {
            errors.add(new ValidationError("email", "email already in use"));
        }

        return (errors.size() > 0) ? errors : null;
    }

}