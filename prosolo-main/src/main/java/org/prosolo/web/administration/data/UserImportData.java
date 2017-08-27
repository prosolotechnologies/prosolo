package org.prosolo.web.administration.data;

/**
 * @author stefanvuckovic
 * @date 2017-08-09
 * @since 1.0.0
 */
public class UserImportData {

    private String email;
    private String firstName;
    private String lastName;
    private String position;

    public UserImportData(String email, String firstName, String lastName, String position) {
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.position = position;
    }

    public String getEmail() {
        return email;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getPosition() {
        return position;
    }
}
