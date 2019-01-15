package si.rso.bicycle.entity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;


@Entity
@Table(name = "billing")
public class BillingEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    private int id;

    @Column(name = "borrow_id")
    private Integer borrow_id;

    @Column(name = "user_id")
    private Integer user_id;

    @Column(name = "start_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date start_time;

    @Column(name = "start_station_id")
    private Integer start_station_id;

    @Column(name = "end_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date end_time;

    @Column(name = "end_station_id")
    private Integer end_station_id;

    @Column(name = "rate")
    private Double rate;

    @Column(name = "VAT")
    private Double vat;

    @Column(name = "currency")
    private String currency;

    @Column(name = "total")
    private Double total;

    @Column(name = "with_vat")
    private Double with_vat;

    /*
    @Column(name = "created_at", insertable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;
    */

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Integer getBorrow_id() {
        return borrow_id;
    }

    public void setBorrow_id(Integer borrow_id) {
        this.borrow_id = borrow_id;
    }

    public Integer getUser_id() {
        return user_id;
    }

    public void setUser_id(Integer user_id) {
        this.user_id = user_id;
    }

    public Date getStart_time() {
        return start_time;
    }

    public void setStart_time(Date start_time) {
        this.start_time = start_time;
    }

    public Integer getStart_station_id() {
        return start_station_id;
    }

    public void setStart_station_id(Integer start_station_id) {
        this.start_station_id = start_station_id;
    }

    public Date getEnd_time() {
        return end_time;
    }

    public void setEnd_time(Date end_time) {
        this.end_time = end_time;
    }

    public Integer getEnd_station_id() {
        return end_station_id;
    }

    public void setEnd_station_id(Integer end_station_id) {
        this.end_station_id = end_station_id;
    }

    public Double getRate() {
        return rate;
    }

    public void setRate(Double rate) {
        this.rate = rate;
    }

    public Double getVat() {
        return vat;
    }

    public void setVat(Double vat) {
        this.vat = vat;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Double getWith_vat() {
        return with_vat;
    }

    public void setWith_vat(Double with_vat) {
        this.with_vat = with_vat;
    }

    public Double getTotal() {
        return total;
    }

    public void setTotal(Double total) {
        this.total = total;
    }
}