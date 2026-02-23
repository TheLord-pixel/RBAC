package filters;

import models.User;

public class UserFilters {

    private UserFilters() {
    }

    public static UserFilter byUsername(String username) {
        return user -> username.equals(user.username());
    }

    public static UserFilter byUsernameContains(String substring) {
        return user -> {
            String uname = user.username();
            return uname != null && uname.toLowerCase().contains(substring.toLowerCase());
        };
    }

    public static UserFilter byEmail(String email) {
        return user -> email.equals(user.email());
    }

    public static UserFilter byEmailDomain(String domain) {
        return user -> {
            String mail = user.email();
            return mail != null && mail.endsWith(domain);
        };
    }

    public static UserFilter byFullNameContains(String substring) {
        return user -> {
            String fname = user.fullName();
            return fname != null && fname.toLowerCase().contains(substring.toLowerCase());
        };
    }
}