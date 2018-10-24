package si.rso.bicycle.entities;

import org.hibernate.validator.constraints.Length;
import si.rso.bicycle.types.Country;
import si.rso.bicycle.types.Gender;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import java.util.Date;

@Entity
@Table(name = "profiles")
class ProfileEntity extends BaseEntity {
    @NotEmpty
    @Length(min = 2, max = 255)
    private String firstName;

    @NotEmpty
    @Length(min = 2, max = 255)
    private String lastName;

    private String middleName;

    @Gender
    private String gender;

    @NotEmpty
    @Temporal(TemporalType.DATE)
    private Date birthday;

    @NotEmpty
    @Length(min = 2, max = 255)
    private String address;

    @NotEmpty
    @Length(min = 2, max = 255)
    private String addressCity;

    @Country
    private String addressCountry;

    @NotEmpty
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    // Connections
    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;
}
