package si.rso.bicycle.resources;

import com.kumuluz.ee.streaming.common.annotations.StreamProducer;
import org.apache.kafka.clients.producer.Producer;
import si.rso.bicycle.entity.BillingEntity;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.*;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Date;
import java.util.logging.Logger;


/**
 * @author Žiga Kokelj
 * @since 1.0.0
 */

@RequestScoped
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Path("/billing")
public class BillingResource {
    private static final Logger log = Logger.getLogger(BillingResource.class.getName());

    @PersistenceContext
    private EntityManager em;

    @Inject
    @StreamProducer
    private Producer producer;


    @GET
    @Path("billing/test")
    public Response testBilling(@Valid AllValid request){
        return Response.status(77).build();
    }


    @POST
    @Path("/billing/start")
    public Response startBilling(@Valid StartBilling request) {
        //create new billing entry
        try{

            this.em.getTransaction().begin();


            BillingEntity be = new BillingEntity();
            be.setUser_id(request.user_id);
            be.setBorrow_id(request.borrow_id);
            be.setStart_time(request.start_time);
            be.setStart_station_id(request.start_station_id);
            be.setRate(request.rate);
            be.setVat(request.vat);
            be.setCurrency(request.currency);


            this.em.persist(be);
            this.em.getTransaction().commit();
            this.em.refresh(be);
            return Response.status(Response.Status.CREATED).build();


        }catch (Exception e){
            e.printStackTrace();
        }
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }

    @POST
    @Path("/billing/stop")
    public Response stopBilling(@Valid StopBilling request) {
        //create new billing entry
        try{
            //TypedQuery<ProfileEntity> pe = this.em.createQuery("SELECT p FROM ProfileEntity p WHERE p.user_uuid = :user ORDER BY p.created_at DESC LIMIT 1", ProfileEntity.class);
            //TypedQuery<BillingEntity> be = this.em.createQuery("SELECT b FROM BillingEntity b WHERE b.user_id = :user LIMIT 1", BillingEntity.class);
            // TODO: Get end_time & end_station_id from request
            // TODO: Calculate total, with_vat & write everything to DB


        }catch (Exception e){
            e.printStackTrace();
        }
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }


    @POST
    @Path("/billing/getBill")
    public Response getBill(@Valid GetBill request) {
        try{
            //TODO: check if entry with borrow_id has end_time, end_station_id
            //TODO: Get the data an return JSON response

        }catch (Exception e){
            e.printStackTrace();
        }
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }



}


class StartBilling {
    @NotNull(message = "user_id cannot be omitted")
    public Integer user_id;

    @NotNull(message = "borrow_id cannot be omitted")
    public Integer borrow_id;

    @NotNull(message = "start_time cannot be omitted")
    public Date start_time;

    @NotNull(message = "start_station_id cannot be omitted")
    public Integer start_station_id;

    @NotNull(message = "rate cannot be omitted")
    public Double rate;

    @NotNull(message = "VAT cannot be omitted")
    public Double vat;

    @NotNull(message = "currency cannot be omitted")
    public String currency;
}

class StopBilling {
    @NotNull(message = "user_id cannot be omitted")
    public Integer user_id;

    @NotNull(message = "stop_time cannot be omitted")
    public Date  stop_time;

    @NotNull(message = "start_station_id cannot be omitted")
    public Integer start_station_id;

    @NotNull(message = "rate cannot be omitted")
    public Double rate;

    @NotNull(message = "VAT cannot be omitted")
    public Double vat;

    @NotNull(message = "currency cannot be omitted")
    public String currency;
}

class GetBill {

    @NotNull(message = "borrow_id cannot be omitted")
    public Integer borrow_id;

}
class AllValid {
}
