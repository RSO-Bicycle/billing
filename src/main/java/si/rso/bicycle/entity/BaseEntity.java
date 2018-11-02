package si.rso.bicycle.entity;

import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.Positive;
import java.util.UUID;

public class BaseEntity {
    @Id
    @Positive
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "uid")
    @Type(type = "pg-uuid")
    private UUID uid;

    public UUID getUid() {
        return uid;
    }
}
