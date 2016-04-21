package pt.ist.cnv.cloudprime.instrumentation;

import BIT.highBIT.*;

import java.io.*;
import java.util.*;



public class CloudPrimeIT {

    private static final String THIS_CLASS = "pt/ist/cnv/cloudprime/instrumentation/CloudPrimeIT";
    private static final String METRICS_FILENAME = "metrics.txt";
    private static HashMap<Long, ThreadMetrics> threadMetrics = new HashMap<Long, ThreadMetrics>();
    private static HashMap<Long, String> threadRequest = new HashMap<Long, String>();

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


    public static void addThreadRequest(Long threadID, String request){
        threadRequest.put(threadID, request);
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
            tm.incCompCount(comparisonInstructions);
            if(tm.incICount(instructionsNumber)){ //has reached the threshold
                appendToFile(METRICS_FILENAME, "[INSTRUCTIONS THRESHOLD REACHED] | THREAD=" + tm.getThreadID() +" | VALUE=" + tm.getiCount() + "\n");
            }

        }

    }

    public static void newFunctionCalled(int ignore){
        long threadID = Thread.currentThread().getId();

        synchronized (CloudPrimeIT.class) {
            ThreadMetrics tm = getThreadMetrics(threadID);
            tm.incFunctionCalls();
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
        String request = threadRequest.get(threadID);

        synchronized (CloudPrimeIT.class) {
            ThreadMetrics tm = getThreadMetrics(threadID);
            tm.resetMetrics();
            tm.setRequest(request);
            appendToFile(METRICS_FILENAME, "[START REQUEST] | THREAD=" + threadID + " | NUMBER= " + request + "\n");
        }
    }

    public static void endRequest(int ignore){

        long threadID = Thread.currentThread().getId();

        synchronized (CloudPrimeIT.class){
            ThreadMetrics tm = getThreadMetrics(threadID);
            String filename = tm.getRequest() + ".txt";
            appendToFile(filename, "[TOTAL INSTRUCTIONS]   | VALUE=" + tm.getiCount() + "\n");
            appendToFile(filename, "[TOTAL COMPARISONS]    | VALUE=" + tm.getCompCount() + "\n");
            appendToFile(filename, "[TOTAL ALLOCATIONS]    | VALUE=" + tm.getAllocCount() + "\n");
            appendToFile(filename, "[TOTAL FUNCTION CALLS] | VALUE=" + tm.getFunctionCallsCount() + "\n");
            appendToFile(filename, "[MAX FUNCTION INVOCATION DEPTH] | VALUE=" + tm.getMaxInvocationDepth() + "\n");
            appendToFile(METRICS_FILENAME, "[END REQUEST]  | THREAD=" + threadID + " | NUMBER=" + tm.getRequest() + "\n");
        }
    }


     private static void appendToFile(String filename, String content){
        try{
            
            String longFilename = "/home/ec2-user/cloud-prime-server/metrics" +  File.separator + filename;
            //String longFilename = "/home/pedroj/Desktop/cnv/cloud-prime/metrics" +  File.separator + filename;
            //String longFilename = "/home/pedroj/Desktop/server2/metrics" +  File.separator + filename;

            File file =new File(longFilename);

            if(!file.exists()){
                file.createNewFile();
            }

            FileWriter fileWritter = new FileWriter(longFilename,true);
            BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
            bufferWritter.write(content);
            bufferWritter.close();

        }catch(IOException e){
            e.printStackTrace();
        }
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
