package si.rso.bicycle.entity;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.util.Date;

@Entity
@Table(name = "users")
public class UserEntity extends BaseEntity {
    @NotEmpty
    @Column(name = "username")
    private String username;

    @NotEmpty
    @Column(name = "password")
    private String password;

    @Column(name = "activated")
    private boolean activated;

    @NotEmpty
    @Column(name = "activation_code")
    private String activationCode;

    @NotEmpty
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "activation_code_validity")
    private Date activationCodeValidity;

    @Column(name = "deleted")
    private boolean deleted;

    @NotEmpty
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    // @generated
    public String getUsername() {
        return username;
    }

    public void setUsername(@NotEmpty String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(@NotEmpty String password) {
        this.password = password;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public boolean isActivated() {
        return activated;
    }

    public void setActivated(boolean activated) {
        this.activated = activated;
    }

    public String getActivationCode() {
        return activationCode;
    }

    public void setActivationCode(String activationCode) {
        this.activationCode = activationCode;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public Date getActivationCodeValidity() {
        return activationCodeValidity;
    }

    public void setActivationCodeValidity(Date activationCodeValidity) {
        this.activationCodeValidity = activationCodeValidity;
    }
}