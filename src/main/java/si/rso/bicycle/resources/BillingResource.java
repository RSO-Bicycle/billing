package si.rso.bicycle.resources;

import com.kumuluz.ee.streaming.common.annotations.StreamProducer;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
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
//@Consumes(MediaType.APPLICATION_JSON)
//@Produces(MediaType.APPLICATION_JSON)
@Path("/billing")
public class BillingResource {
    private static final Logger log = Logger.getLogger(BillingResource.class.getName());

    @PersistenceContext
    private EntityManager em;

    @Inject
    @StreamProducer
    private Producer producer;


    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String sayPlainTextHello() {
        return "Hello Jersey";
    }

    @GET
    @Path("/test")
    public Response testBilling(){
        return Response.status(77).build();
    }


    @POST
    @Path("/start")
    public Response startBilling(@Valid StartBilling request) {
        //create new billing entry

        //Additional data.
        String currency = "EUR";
        double VAT = 0.2;
        double rate = 0.5;

        try{
            this.em.getTransaction().begin();

            BillingEntity be = new BillingEntity();
            be.setUser_id(request.user_id);
            be.setBorrow_id(request.borrow_id);
            be.setStart_station_id(request.start_station_id);
            be.setStart_time(new Date());
            be.setRate(rate);
            be.setVat(VAT);
            be.setCurrency(currency);

            this.em.persist(be);
            this.em.getTransaction().commit();
            this.em.refresh(be);

            return Response.status(Response.Status.CREATED).build();

        }catch (Exception e){
            e.printStackTrace();
            //return "Error catched"+e;
        }
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }

    @POST
    @Path("/stop")
    public String stopBilling(@Valid StopBilling request) {
        try{
            TypedQuery<BillingEntity> tq = this.em.createQuery("SELECT b FROM BillingEntity b WHERE b.borrow_id = :borrow_id", BillingEntity.class);
            tq.setParameter("borrow_id", request.borrow_id);

            BillingEntity be = tq.getSingleResult();

            long diff = new Date().getTime() - be.getStart_time().getTime();
            double diffHours = diff / (60 * 60 * 1000) % 24;

            this.em.getTransaction().begin();

            be.setEnd_time(new Date());
            be.setEnd_station_id(request.stop_station_id);
            be.setTotal(diffHours*be.getRate());
            be.setWith_vat((1+be.getVat()) *diffHours*be.getRate());

            this.em.persist(be);
            this.em.getTransaction().commit();
            this.em.refresh(be);

            return "Success";
        }catch (Exception e){
            e.printStackTrace();
        }
        //return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        return "Error";
    }


    @POST
    @Path("/getBill")
    public String getBill(@Valid GetBill request) {
        try{


            //Get random quote from third party API
            HttpResponse<JsonNode> response = Unirest.get("https://qvoca-bestquotes-v1.p.rapidapi.com/quote")
                    .header("X-RapidAPI-Key", "ab928df105msh337d6fda6886cb8p18d25djsn0355c6416136")
                    .asJson();
            return (String)response.getBody().getObject().get("message");
        }catch (Exception e){
            e.printStackTrace();
        }
        //return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        return "Failed";
    }

}


class StartBilling {
    @NotNull(message = "user_id cannot be omitted")
    public Integer user_id;

    @NotNull(message = "borrow_id cannot be omitted")
    public Integer borrow_id;

    @NotNull(message = "start_station_id cannot be omitted")
    public Integer start_station_id;

}

class StopBilling {
    @NotNull(message = "borrow_id cannot be omitted")
    public Integer borrow_id;

    @NotNull(message = "stop_station_id cannot be omitted")
    public Integer stop_station_id;

}

class GetBill {

    @NotNull(message = "borrow_id cannot be omitted")
    public Integer borrow_id;

}
class AllValid {
}
