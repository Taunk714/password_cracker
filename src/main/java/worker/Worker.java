package worker;

import java.io.IOException;
import java.net.*;
import java.util.Enumeration;
import java.util.logging.Logger;


import org.apache.commons.codec.digest.DigestUtils;
import utils.Client;
import utils.Server;

public class Worker {


    public static final int PORT_NUM = 1211;
    public static final String MASTER_IP = "127.0.0.1";
    private static final int MASTER_PORT = 1207;

    private static final Logger logger = Logger.getLogger("worker");

    private long counter = 0;
    private int id;


//    public static Server socket;
    public static ThreadLocal<Server> threadLocal = new ThreadLocal<>();



    private String givenHash;
    private String lowerRange;
    private String upperRange;

    public static void main(String args[]){
        for (int i = 0; i < 5; i++) {
            int finalI = i;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    connectToMaster(PORT_NUM+ finalI);
                    while(true){
                        try {
                            threadLocal.get().accept();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        String msgReceived;
                        CrackingThread crackingTh = null;
                        while ((msgReceived= getMsgFromMaster()) != null) {
                            if (msgReceived.equals("remove")){
                                if (crackingTh != null) {
                                    crackingTh.interrupt();
                                }
                                threadLocal.get().response("remove");
                                disconnectFromMaster();
                            }else if (msgReceived.startsWith("stop")){
                                // interrupt

                                if (crackingTh != null) {
                                    crackingTh.interrupt();
                                }
                                System.out.println("worker stop");
                            }else{
                                String[] msgs = msgReceived.split(" ");
                                int version = -1;
                                try{
                                    version = Integer.parseInt(msgs[3]);
                                }catch (Exception e){
                                    e.printStackTrace();
                                }

                                Worker worker = new Worker(msgs[0], msgs[1], msgs[2], version );
                                crackingTh = new CrackingThread(worker, threadLocal.get());
                                crackingTh.start();
//                            String crackRes = worker.crack();
//                            if (crackRes.isEmpty()){
//                                threadLocal.get().response("fail");
//                            } else {
//                                threadLocal.get().response("success " + crackRes);
//                            }
                            }
                        }
                    }
                }
            }).start();
        }

    }


    private static class CrackingThread extends Thread {
        private Worker worker;
        private Server server;

        public CrackingThread(Worker worker, Server server){
            this.worker = worker;
            this.server = server;
        }

        @Override
        public void run() {
            while (true) {
                if(Thread.interrupted()) {
                    server.response("cracking process is interrupted");
                    break;
                }
                String crackRes = worker.crack();
                if (crackRes.isEmpty()){
                    server.response(worker.id + "&fail " + worker.counter);
                } else {
                    server.response(worker.id + "&success " + crackRes);
                }
                return;
            }
        }
    }

    public static String getMsgFromMaster(){
        String msgReceived = "";
        try{

            msgReceived = threadLocal.get().receive();
        } catch (IOException e) {
            e.printStackTrace();
        }
//        String[] msgs = msgReceived.split(" ");
        return msgReceived;
    }

    public static void connectToMaster(int PORT_NUM) {
        try {
            threadLocal.set(new Server(PORT_NUM));
//            threadLocal.get() =
        } catch (IOException e) {
            e.printStackTrace();
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Client client = new Client(MASTER_IP, MASTER_PORT);
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

    public static String getLocalIP() {
        Enumeration<NetworkInterface> nifs = null;
        try {
            nifs = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            e.printStackTrace();
        }
        while (nifs.hasMoreElements()) {
            NetworkInterface nif = nifs.nextElement();
            Enumeration<InetAddress> addresses = nif.getInetAddresses();
            while (addresses.hasMoreElements()) {
                InetAddress addr = addresses.nextElement();
                if (addr instanceof Inet4Address) {
                    if (nif.getName().equals("eth1")) {
                        return addr.getHostAddress();
                    }
                }
            }
        }
        return "";
    }

    public static String getMasterIP() {
        String localIP = getLocalIP();
        String[] partitions = localIP.split("\\.");
        partitions[3] = "1";
        return String.join(".", partitions);
    }

    public static void disconnectFromMaster() {
        try{
            threadLocal.get().close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Worker(String givenHash, String lowerRange, String upperRange, int id) {
        this.givenHash = givenHash;
        this.lowerRange = lowerRange;
        this.upperRange = upperRange;
        this.counter = 0;
        this.id = id;
        logger.info(this.toString() + ": range from " + lowerRange +" to " + upperRange);
    }

    public String crack() {
        char[] lowerRangeLst = lowerRange.toCharArray();
        char[] upperRangeLst = upperRange.toCharArray();
        boolean cracked;
        StringBuilder strToHash = new StringBuilder();
        for (char a0 = lowerRangeLst[0]; a0 <= 'z'; a0++) {
            if (a0>= 91 && a0 <= 96){
                continue;
            }
            strToHash.append(a0);
            for (char a1 = 'A'; a1 <= 'z'; a1++) {
                if (a1>= 91 && a1 <= 96){
                    continue;
                }
                strToHash.append(a1);
                if (strToHash.toString().compareTo(upperRange.substring(0,2)) > 0 || (strToHash.toString().compareTo(lowerRange.substring(0,2)) < 0)){
                    strToHash.deleteCharAt(1);
                    continue;
                }
                for (char a2 = 'A'; a2 <= 'z'; a2++) {
                    if (a2>= 91 && a2 <= 96){
                        continue;
                    }
                    strToHash.append(a2);
                    if (strToHash.toString().compareTo(upperRange.substring(0,3)) > 0 || (strToHash.toString().compareTo(lowerRange.substring(0,3)) < 0)){
                        strToHash.deleteCharAt(2);
                        continue;
                    }
                    for (char a3 = 'A'; a3 <= 'z'; a3++) {
                        if (a3>= 91 && a3 <= 96){
                            continue;
                        }
                        strToHash.append(a3);
                        if (strToHash.toString().compareTo(upperRange.substring(0,4)) > 0 || (strToHash.toString().compareTo(lowerRange.substring(0,4)) < 0)){
                            strToHash.deleteCharAt(3);
                            continue;
                        }
                        for (char a4 = 'A'; a4 <= 'z'; a4++) {
                            if (a4>= 91 && a4 <= 96){
                                continue;
                            }
                            strToHash.append(a4);
                            if (strToHash.toString().compareTo(upperRange) > 0 || (strToHash.toString().compareTo(lowerRange) < 0)){
                                strToHash.deleteCharAt(4);
                                continue;
                            }
//                            System.out.println("cracking: " + strToHash.toString());
                            counter++;
                            String generatedHash = generateHash(strToHash.toString());
                            cracked = compareHash(generatedHash);
                            if (cracked) {
                                System.out.println("find ans " + counter);
                                return strToHash.toString();
                            }
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
