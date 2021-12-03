import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.*;
import org.apache.commons.codec.digest.DigestUtils;

public class Worker {

    public static final int PORT_NUM = 1202;
    public static final String MASTER_IP = "";

    public static Socket socket;
    public static BufferedReader bf;
    public static PrintStream ps;

    private String givenHash;
    private String lowerRange;
    private String upperRange;

    public static void main(String args[]){
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(true){
                    connectToMaster();
                    String msgReceived = getMsgFromMaster();
                    if (!msgReceived.isEmpty()) {
                        if (msgReceived.equals("remove")){
                            ps.println("remove");
                            disconnectFromMaster();
                        }
                        String[] msgs = msgReceived.split(" ");
                        Worker worker = new Worker(msgs[0], msgs[1], msgs[2]);
                        String crackRes = worker.crack();
                        if (crackRes.isEmpty()){
                            ps.println("fail");
                        } else {
                            ps.println("success" + crackRes);
                        }
                    }
                }
            }
        }).start();
    }

    public static String getMsgFromMaster(){
        String msgReceived = "";
        try{
            msgReceived = bf.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
//        String[] msgs = msgReceived.split(" ");
        return msgReceived;
    }

    public static void connectToMaster() {
        try{
            socket = new Socket(MASTER_IP, PORT_NUM);
            bf = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            ps = new PrintStream(socket.getOutputStream());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void disconnectFromMaster() {
        try{
            socket.close();
            bf.close();
            ps.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Worker(String givenHash, String lowerRange, String upperRange) {
        this.givenHash = givenHash;
        this.lowerRange = lowerRange;
        this.upperRange = upperRange;
    }

    public String crack() {
        char[] lowerRangeLst = lowerRange.toCharArray();
        char[] upperRangeLst = upperRange.toCharArray();
        boolean cracked;
        String strToHash = "";
        for (char a0 = lowerRangeLst[0]; a0 < upperRangeLst[0]; a0++) {
            strToHash += a0;
            for (char a1 = lowerRangeLst[1]; a1 < upperRangeLst[1]; a1++) {
                strToHash += a1;
                for (char a2 = lowerRangeLst[2]; a2 < upperRangeLst[2]; a2++) {
                    strToHash += a2;
                    for (char a3 = lowerRangeLst[3]; a3 < upperRangeLst[3]; a3++) {
                        strToHash += a3;
                        for (char a4 = lowerRangeLst[4]; a4 < upperRangeLst[4]; a4++) {
                            strToHash += a4;
                            String generatedHash = generateHash(strToHash);
                            cracked = compareHash(generatedHash);
                            if (cracked)
                                return strToHash;
                            strToHash = "";
                        }
                    }
                }
            }
        }
        return strToHash;
    }

    public String generateHash(String strToHash) {
        String md5Str = DigestUtils.md5Hex(strToHash);
        return md5Str;
    }

    public boolean compareHash(String generatedHash) {
        boolean identical = false;
        if (givenHash.equals(generatedHash))
            identical = true;
        return identical;
    }

}
