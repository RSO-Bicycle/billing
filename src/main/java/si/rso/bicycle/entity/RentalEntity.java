package si.rso.bicycle.entity;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "profile")
public class RentalEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    private int id;

    @Column(name = "uid")
    private String uid;

    @Column(name = "user_uuid")
    private String userUuid;

    @Column(name = "start_location_uuid")
    private String startLocationUuid;

    @Column(name = "stop_location_uuid")
    private String stopLocationUuid;

    @Column(name = "started_at", insertable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date startedAt;

    @Column(name = "ended_at", insertable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date endedAt;
}
