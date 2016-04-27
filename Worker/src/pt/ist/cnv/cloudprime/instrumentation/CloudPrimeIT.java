package pt.ist.cnv.cloudprime.instrumentation;

import BIT.highBIT.*;
import org.json.simple.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;



public class CloudPrimeIT {

    private static final String THIS_CLASS = "pt/ist/cnv/cloudprime/instrumentation/CloudPrimeIT";
    private static final int MSS_PORT = 9000;
    private static HashMap<Long, ThreadMetrics> threadMetrics = new HashMap<Long, ThreadMetrics>();
    private static HashMap<Long, String> threadRequestNumber = new HashMap<Long, String>();
    private static HashMap<Long, String> threadRequestID = new HashMap<Long, String>();
    private static String MSS_IP;
    private static final int REQUEST_INFO_TYPE = 1;
    private static final int EVENT_INFO_TYPE = 2;

    public static void main(String argv[]) {

        if(argv.length < 2){
            printUsage();
        }

        File fileIn = new File(argv[0]);


        for (String classFile : fileIn.list()) {

            if (!classFile.endsWith(".class")) {
                continue;
            }

            ClassInfo ci = new ClassInfo(argv[0] + System.getProperty("file.separator") + classFile);
            instrumentForRequestInfo(ci);
            ci.write(argv[1] + System.getProperty("file.separator") + classFile);
        }
    }


    private static void instrumentForRequestInfo(ClassInfo ci){
        ci.addBefore(THIS_CLASS, "startNewRequest", new Integer(0));

        for (Enumeration e = ci.getRoutines().elements(); e.hasMoreElements(); ) {
            Routine routine = (Routine) e.nextElement();
            InstructionArray instructions = routine.getInstructionArray();

            for (Enumeration b = routine.getBasicBlocks().elements(); b.hasMoreElements(); ) {
                BasicBlock bb = (BasicBlock) b.nextElement();

                int comparisons = 0;
                int memoryAllocation = 0;
                int iNumber = bb.size();

                for(int i = bb.getStartAddress(); i <= bb.getEndAddress(); i++){
                    Instruction instr = instructions.elementAt(i);

                    int opcode = instr.getOpcode();
                    short instr_type = InstructionTable.InstructionTypeTable[opcode];


                    if (instr_type == InstructionTable.COMPARISON_INSTRUCTION ||instr_type ==  InstructionTable.CONDITIONAL_INSTRUCTION) {
                        comparisons += 1;
                    }

                    if (instr_type == InstructionTable.UNCONDITIONAL_INSTRUCTION){

                        if(isFunctionCall(opcode)){
                            instr.addBefore(THIS_CLASS, "newFunctionCalled", new Integer(0));
                        }
                        else{
                            instr.addBefore(THIS_CLASS, "newFunctionReturned", new Integer(0));
                        }
                    }

                    if (isAllocationInstruction(opcode)){
                        memoryAllocation += 1;
                    }
                }

                bb.addBefore(THIS_CLASS, "addInstructionMetrics", iNumber + "&" + memoryAllocation + "&" + comparisons);
            }
        }

        ci.addAfter(THIS_CLASS, "endRequest", new Integer(0));
    }

    private static boolean isAllocationInstruction(int opcode){
        return opcode==InstructionTable.NEW ||  (opcode==InstructionTable.newarray) ||
                (opcode==InstructionTable.anewarray) || (opcode==InstructionTable.multianewarray);
    }

    private static boolean isFunctionCall(int opcode){
        return opcode==InstructionTable.invokeinterface || opcode==InstructionTable.invokespecial ||
                opcode==InstructionTable.invokestatic || opcode==InstructionTable.invokevirtual;
    }


    public static synchronized void addThreadRequest(Long threadID, String request, String requestID, String mssIp){
        threadRequestNumber.put(threadID, request);
        threadRequestID.put(threadID, requestID);
        MSS_IP = mssIp;
    }

    public static void addInstructionMetrics(String iInfo)
    {
        long threadID = Thread.currentThread().getId();
        String[] instructionsInfoSplited = iInfo.split("&");

        long instructionsNumber = Long.valueOf(instructionsInfoSplited[0]);
        long allocationInstructions = Long.valueOf(instructionsInfoSplited[1]);
        long comparisonInstructions = Long.valueOf(instructionsInfoSplited[2]);

        synchronized (CloudPrimeIT.class){
            ThreadMetrics tm = getThreadMetrics(threadID);

            tm.incAllocCount(allocationInstructions);

            if(tm.incCompCount(comparisonInstructions)){
                JSONObject jsonObj = new JSONObject();
                jsonObj.put("metric_name", "comparisons");
                jsonObj.put("metric_value", "" + tm.getCompCount());
                jsonObj.put("request_id", tm.getRequestID());
                sendToMSSAsync(EVENT_INFO_TYPE, jsonObj.toString());
            }

            if(tm.incICount(instructionsNumber)){ //has reached the threshold
                JSONObject jsonObj = new JSONObject();
                jsonObj.put("metric_name", "total_instructions");
                jsonObj.put("metric_value", "" + tm.getiCount());
                jsonObj.put("request_id", tm.getRequestID());
                sendToMSSAsync(EVENT_INFO_TYPE, jsonObj.toString());
            }

        }

    }

    public static void newFunctionCalled(int ignore){
        long threadID = Thread.currentThread().getId();

        synchronized (CloudPrimeIT.class) {
            ThreadMetrics tm = getThreadMetrics(threadID);

            if(tm.incFunctionCalls()){
                JSONObject jsonObj = new JSONObject();
                jsonObj.put("metric_name", "functions_call");
                jsonObj.put("metric_value", "" + tm.getFunctionCallsCount());
                jsonObj.put("request_id", tm.getRequestID());
                sendToMSSAsync(EVENT_INFO_TYPE, jsonObj.toString());
            }
        }

    }

    public static void newFunctionReturned(int ignore){
        long threadID = Thread.currentThread().getId();

        synchronized (CloudPrimeIT.class) {
            ThreadMetrics tm = getThreadMetrics(threadID);
            tm.incReturnFunction();
        }

    }

    public static void startNewRequest(int ignore){

        long threadID = Thread.currentThread().getId();
        String request = threadRequestNumber.get(threadID);
        String requestID = threadRequestID.get(threadID);

        synchronized (CloudPrimeIT.class) {
            ThreadMetrics tm = getThreadMetrics(threadID);
            tm.resetMetrics();
            tm.setRequest(request);
            tm.setRequestID(requestID);
        }
    }

    public static void endRequest(int ignore){

        long threadID = Thread.currentThread().getId();

        synchronized (CloudPrimeIT.class){
            ThreadMetrics tm = getThreadMetrics(threadID);
            JSONObject objJSON = new JSONObject();

            objJSON.put("request_number",  tm.getRequest());
            objJSON.put("total_instructions", "" + tm.getiCount());
            objJSON.put("total_comparisons", "" + tm.getCompCount());
            objJSON.put("total_function_calls", "" + tm.getFunctionCallsCount());

            sendToMSSAsync(REQUEST_INFO_TYPE, objJSON.toString());
        }
    }

    private static void sendToMSSAsync(final int type, final String json) {

        new Thread() {
            public void run() {
                try{
                    byte[] body = json.getBytes("UTF-8");
                    URL url = new URL("http://" + MSS_IP + ":" + MSS_PORT + "/requestInfo?type=" + type);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                    conn.setRequestMethod("POST");
                    conn.setDoOutput(true);
                    conn.setRequestProperty( "Content-Type", "application/x-www-form-urlencoded");
                    conn.setRequestProperty( "charset", "utf-8");
                    conn.setRequestProperty( "Content-Length", Integer.toString(body.length));

                    DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
                    wr.write(body);
                    wr.flush();
                    wr.close();

                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    String inputLine;


                    while ((inputLine = in.readLine()) != null) {
                        //ignore
                    }

                    in.close();

                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }.start();
    }


    private static ThreadMetrics getThreadMetrics(long id) {

        ThreadMetrics tm = threadMetrics.get(id);

        if(tm == null){
            tm = new ThreadMetrics(id);
            threadMetrics.put(id, tm);
        }

        return tm;
    }

    private static void printUsage() {
        System.out.println("usage: java CloudPrimeIT <dir> <output_dir>");
    }
}
