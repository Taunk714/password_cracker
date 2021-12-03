package worker;

import java.io.IOException;
import java.net.*;


import org.apache.commons.codec.digest.DigestUtils;
import utils.Client;
import utils.Server;

public class Worker {


    public static final int PORT_NUM = 1204;
    public static final String MASTER_IP = "127.0.0.1";
    private static final int MASTER_PORT = 1203;


    public static Server socket;


    private String givenHash;
    private String lowerRange;
    private String upperRange;

    public static void main(String args[]){

        new Thread(new Runnable() {
            @Override
            public void run() {
                connectToMaster();
                while(true){
                    try {
                        socket.accept();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    String msgReceived;
                    CrackingThread crackingTh = null;
                    while ((msgReceived= getMsgFromMaster()) != null) {
                        if (msgReceived.equals("remove")){
                            crackingTh.interrupt();
                            socket.response("remove");
                            disconnectFromMaster();
                        }else if (msgReceived.startsWith("stop")){
                            // interrupt
                            crackingTh.interrupt();
                        }else{
                            String[] msgs = msgReceived.split(" ");
                            Worker worker = new Worker(msgs[0], msgs[1], msgs[2]);
                            crackingTh = new CrackingThread(worker);
                            crackingTh.start();
//                            String crackRes = worker.crack();
//                            if (crackRes.isEmpty()){
//                                socket.response("fail");
//                            } else {
//                                socket.response("success " + crackRes);
//                            }
                        }
                    }
                }
            }
        }).start();
    }


    private static class CrackingThread extends Thread {
        private Worker worker;

        public CrackingThread(Worker worker){
            this.worker = worker;
        }

        @Override
        public void run() {
            while (true) {
                if(Thread.interrupted()) {
                    socket.response("cracking process is interrupted");
                    break;
                }
                String crackRes = worker.crack();
                if (crackRes.isEmpty()){
                    socket.response("fail");
                } else {
                    socket.response("success " + crackRes);
                }
                return;
            }
        }
    }

    public static String getMsgFromMaster(){
        String msgReceived = "";
        try{

            msgReceived = socket.receive();
        } catch (IOException e) {
            e.printStackTrace();
        }
//        String[] msgs = msgReceived.split(" ");
        return msgReceived;
    }

    public static void connectToMaster() {
        try {
            socket = new Server(PORT_NUM);
        } catch (IOException e) {
            e.printStackTrace();
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Client client = new Client(MASTER_IP, 1207);
                    client.send("register " + Inet4Address.getLocalHost() + " " + PORT_NUM);
                    String response = client.receive();
                    if (!response.equals("success")){
                        disconnectFromMaster();
                    }
                    client.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public static void disconnectFromMaster() {
        try{
            socket.close();

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
        StringBuilder strToHash = new StringBuilder();
        for (char a0 = lowerRangeLst[0]; a0 <= upperRangeLst[0]; a0++) {
            if (a0>= 91 && a0 <= 96){
                continue;
            }
            strToHash.append(a0);
            for (char a1 = lowerRangeLst[1]; a1 <= upperRangeLst[1]; a1++) {
                if (a1>= 91 && a1 <= 96){
                    continue;
                }
                strToHash.append(a1);
                for (char a2 = lowerRangeLst[2]; a2 <= upperRangeLst[2]; a2++) {
                    if (a2>= 91 && a2 <= 96){
                        continue;
                    }
                    strToHash.append(a2);
                    for (char a3 = lowerRangeLst[3]; a3 <= upperRangeLst[3]; a3++) {
                        if (a3>= 91 && a3 <= 96){
                            continue;
                        }
                        strToHash.append(a3);
                        for (char a4 = lowerRangeLst[4]; a4 <= upperRangeLst[4]; a4++) {
                            if (a4>= 91 && a4 <= 96){
                                continue;
                            }
                            strToHash.append(a4);
                            String generatedHash = generateHash(strToHash.toString());
                            cracked = compareHash(generatedHash);
                            if (cracked)
                                return strToHash.toString();
                            strToHash.deleteCharAt(4);
                        }
                        strToHash.deleteCharAt(3);
                    }
                    strToHash.deleteCharAt(2);
                }
                strToHash.deleteCharAt(1);
            }
            strToHash.deleteCharAt(0);
        }
        return strToHash.toString();
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