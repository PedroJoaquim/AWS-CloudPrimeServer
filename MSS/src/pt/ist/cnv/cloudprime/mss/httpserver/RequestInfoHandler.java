package pt.ist.cnv.cloudprime.mss.httpserver;

import com.sun.net.httpserver.HttpExchange;
import org.json.simple.JSONObject;
import pt.ist.cnv.cloudprime.mss.MSServer;
import pt.ist.cnv.cloudprime.mss.metrics.MetricsInfoContainer;



public class RequestInfoHandler extends BaseHandler {

    @Override
    protected void executeRequest(HttpExchange httpExchange) {

        int lastUpdateID = 0;

        if(getURLParameter("updateID") != null){
            lastUpdateID = Integer.valueOf(getURLParameter("updateID"));
        }

        JSONObject obj = new JSONObject();
        obj.put("new_update_id", MSServer.getInstance().getLastUpdateID());
        obj.put("updates", MSServer.getInstance().getUpdatesSince(lastUpdateID).toJSON());

        writeResponseToClient(httpExchange, obj.toString(), 200);
    }

}
