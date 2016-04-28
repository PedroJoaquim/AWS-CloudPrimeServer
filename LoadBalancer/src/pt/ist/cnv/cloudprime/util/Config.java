package pt.ist.cnv.cloudprime.util;

import java.io.File;
import java.math.BigInteger;

/**
 * Created by Pedro Joaquim on 23-04-2016.
 */
public class Config {

    public static final int GRACE_PERIOD = 90000; //1 30 minute
    public static final BigInteger INSTRUCTIONS_THRESHOLD = new BigInteger("1000000000");
    public static final int LB_PORT = 8000;
    public static final int WORKER_PORT = 8000;
    /**
     * AUTO SCALING
     */

    public static final int MIN_INSTANCES_NR = 1;
    public static final int MAX_INSTANCES_NR = 4;
    public static final long AUTO_SCALER_SLEEP_TIME = 60000;
    public static final long TIME_BETWEEN_RULES = 60000;

    /*
     * INCREASE RULE
     */
    public static final long INCREASE_CPU_LEVEL = 82;

    /*
     * DECREASE RULE
     */
    public static final long DECREASE_CPU_LEVEL = 40;

    public static final int MAX_REQUESTS_MISSED = 2;

    /**
     * MSS Requester
     */

    public static final long MSS_REQUEST_INTERVAL = 60 * 1000;
    public static final int MSS_PORT = 9000;
}
