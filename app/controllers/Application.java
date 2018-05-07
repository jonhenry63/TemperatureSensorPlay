package controllers;

import models.UserInfoDB;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.Controller;
import play.mvc.Result;
import views.formdata.RegisterFormData;
import views.html.Index;
import views.html.Login;
import views.formdata.LoginFormData;
import play.mvc.Security;
import views.html.Register;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Implements the controllers for this application.
 */
@Singleton
public class Application extends Controller {

    private final Form<LoginFormData> loginForm;
    private final Form<RegisterFormData> registerForm;

    @Inject
    public Application(FormFactory formFactory) {
        UserInfoDB.addUserInfo("John Smith", "smith@example.com", "password");
        loginForm = formFactory.form(LoginFormData.class);
        registerForm = formFactory.form(RegisterFormData.class);
    }

    /**
     * Provides the Index page.
     * @return The Index page.
     */
    public Result index() {
        return ok(Index.render("Home", Secured.isLoggedIn(ctx()), Secured.getUserInfo(ctx())));
    }

    /**
     * Provides the Login page (only to unauthenticated users).
     * @return The Login page.
     */
    public Result login() {
        return ok(Login.render("Login", Secured.isLoggedIn(ctx()), Secured.getUserInfo(ctx()), loginForm));
    }

    /**
     * Processes a login form submission from an unauthenticated user.
     * First we bind the HTTP POST data to an instance of LoginFormData.
     * The binding process will invoke the LoginFormData.validate() method.
     * If errors are found, re-render the page, displaying the error data.
     * If errors not found, render the page with the good data.
     * @return The index page with the results of validation.
     */
    public Result postLogin() {

        // Get the submitted form data from the request object, and run validation.
        Form<LoginFormData> formData = loginForm.bindFromRequest();

        if (formData.hasErrors()) {
            flash("error", "Login credentials not valid.");
            return badRequest(Login.render("Login", Secured.isLoggedIn(ctx()), Secured.getUserInfo(ctx()), formData));
        }
        else {
            // email/password OK, so now we set the session variable and only go to authenticated pages.
            session().clear();
            session("email", formData.get().email);
            return redirect(routes.Application.dashboard());
        }
    }

    /**
     * Logs out (only for authenticated users) and returns them to the Index page.
     * @return A redirect to the Index page.
     */
    @Security.Authenticated(Secured.class)
    public Result logout() {
        session().clear();
        return redirect(routes.Application.index());
    }

    /**
     * Provides the Profile page (only to authenticated users).
     * @return The Profile page.
     */
    @Security.Authenticated(Secured.class)
    public Result dashboard() {
        return ok(views.html.dash.index.render("Dashboard", Secured.isLoggedIn(ctx()), Secured.getUserInfo(ctx())));
    }

    public Result register() {
        return ok(Register.render("Register", Secured.isLoggedIn(ctx()), Secured.getUserInfo(ctx()), registerForm));
    }

    public Result postRegister() {
        // Get the submitted form data from the request object, and run validation.
        Form<RegisterFormData> formData = registerForm.bindFromRequest();

        if (formData.hasErrors()) {
            flash("error", "Failed to register with those details.");
            return badRequest(Register.render("Register", Secured.isLoggedIn(ctx()), Secured.getUserInfo(ctx()), formData));
        }
        else {
            // go to login page
            session().clear();
            return redirect(routes.Application.login());
        }
    }
}