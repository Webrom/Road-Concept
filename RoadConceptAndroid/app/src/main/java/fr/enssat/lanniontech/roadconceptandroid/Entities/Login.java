package fr.enssat.lanniontech.roadconceptandroid.Entities;

/**
 * Created by Romain on 09/12/2016.
 */

public class Login implements Entity {
<<<<<<< Updated upstream
    private String token;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
=======
    private String Cookie;

    public Login(String cookie) {
        Cookie = cookie;
    }

    public String getCookie() {
        return Cookie;
    }

    public void setCookie(String cookie) {
        Cookie = cookie;
    }
}
>>>>>>> Stashed changes
