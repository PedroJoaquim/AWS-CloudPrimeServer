package pt.ist.cnv.cloudprime.mss.httpserver;

import com.sun.net.httpserver.HttpExchange;
import org.json.simple.parser.ParseException;
import pt.ist.cnv.cloudprime.mss.MSServer;
import pt.ist.cnv.cloudprime.mss.metrics.AbstractMetric;
import pt.ist.cnv.cloudprime.mss.metrics.EventInfo;
import pt.ist.cnv.cloudprime.mss.metrics.RequestMetrics;
import pt.ist.cnv.cloudprime.mss.util.Config;
import pt.ist.cnv.cloudprime.mss.util.MetricsFileWriter;

import java.io.IOException;


public class PostInfoHandler extends BaseHandler {

    @Override
    protected void executeRequest(HttpExchange httpExchange) throws IOException {

        String requestType;

        if(!"post".equalsIgnoreCase(httpExchange.getRequestMethod())){
            throw new RuntimeException("must be a post request");
        }

        if((requestType = getURLParameter("type")) == null){
            throw new RuntimeException("missing url parameter 'type'");
        }

        String json = getRequestBody(httpExchange);

        writeResponseToClient(httpExchange, "OK", 200);

        if(Config.REQUEST_ID == (Integer.valueOf(requestType))){
            AbstractMetric m = new RequestMetrics().fromJSON(json);
            MSServer.getInstance().addNewInfo(m);
            MetricsFileWriter.writeRequestToFile((RequestMetrics) m);
        }
        else{
            MSServer.getInstance().addNewInfo(new EventInfo().fromJSON(json));
        }
    }
}
