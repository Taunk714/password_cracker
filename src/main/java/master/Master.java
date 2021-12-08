package master;

import utils.Server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This is the master node. Get md5 from interface, and assign it to workers.
 */
public class Master {
    private static int port = 1203;
    private static int workerPort = 1207;

    private ArrayList<WorkerInfo> workerList = new ArrayList<WorkerInfo>();

    private Server toInterface;
    private Server toWorker;

    private AtomicInteger currentWorker = new AtomicInteger(3);
    private AtomicInteger availableWorker = new AtomicInteger(9);
    private AtomicInteger getResponse = new AtomicInteger(0);

    private AtomicInteger status = new AtomicInteger(0);

    private AtomicInteger crackerId = new AtomicInteger(0);

    public Master() throws IOException {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    toWorker = new Server(workerPort);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                while(true){
                    try {
                        toWorker.accept();
                        System.out.println("connect to worker register");
                        String request = null;
                        while((request = toWorker.receive())!= null){
                            if (request.startsWith("register")){
                                String[] s = request.split(" ");
                                workerList.add(new WorkerInfo("127.0.0.1", Integer.parseInt(s[2]), Master.this));
                                currentWorker.incrementAndGet();
                                availableWorker.incrementAndGet();
                                System.out.println("worker register");
                                toWorker.response("success");
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();

        try{
            toInterface = new Server(port);
            toInterface.accept();
            System.out.println("connect to interface");
            listen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void listen() throws IOException {
        String request = null;
        while((request = toInterface.receive()) != null){
            String finalRequest = request;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    String[] split = finalRequest.split("&");
                    int workerNum = 3;
                    try{
                        workerNum = Integer.parseInt(split[0]);
                        if (workerNum > availableWorker.get() || workerNum <= 0){
                            workerNum = availableWorker.get();
                        }
                    }catch (Exception e){
                        workerNum = Math.min(availableWorker.get(),3);
                    }

                    currentWorker.set(workerNum);
                    String md5 = split[1];
                    try {
                        int i = crackerId.get();
                        assign(md5, i);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }


//                        if (finalRequest.startsWith("md5")){
//                            String md5 = finalRequest.substring(4);
//
//                            assign(md5);
//
//                            System.out.println("get md5");
//                        }
//                        else if (finalRequest.startsWith("remove")){
//                            // remove
//                            if (currentWorker.get() == 1){
//                                toInterface.response("only one worker left, can't remove");
//                            }else if (currentWorker.get() > 1){
//                                WorkerInfo remove = workerList.remove(0);
//                                assign(remove.getTargetMD5(), remove.getStartFrom(), remove.getEndWith());
//                                remove.remove();
//                            }
//                        }else if (finalRequest.startsWith("add")){
//                            if (currentWorker.get() >= availableWorker.get()+1){
//                                toInterface.response("Available worker reach maximum");
//                            }else{
//                                currentWorker.incrementAndGet();
//                            }
//                        }
                }
            }).start();
            String stop = "";
        }
    }

    private void assign(String md5, int id) throws IOException {
        assign(md5, "AAAAA", "zzzzz", id);
    }

    private void assign(String md5, String startFromAll, String endWithAll, int id) throws IOException {
        int workerNum = currentWorker.get();
        int[] sizePerWorker = new int[5];
        for (int i = 0; i < 5; i++) {
            sizePerWorker[i] = (Math.max(endWithAll.charAt(i)-96, 0) + Math.max(91-startFromAll.charAt(i),0))/workerNum;
        }
        for (int i = 0; i < workerNum; i++) {
            WorkerInfo workerInfo = workerList.get(i);
            StringBuilder startFrom = new StringBuilder();
            for (int j = 0; j < 5; j++) {
                char s = (char) (startFromAll.charAt(j) + i * sizePerWorker[j]);
                if (s <=96 && s >= 91){
                    s += 6;
                }
                startFrom.append(s);
            }
            StringBuilder endWith = new StringBuilder();
            if ( i == workerNum -1){
                endWith = new StringBuilder(endWithAll);
            }else{
                for (int j = 0; j < 4; j++) {
                    char s = (char) (startFromAll.charAt(j) +( i+1) * sizePerWorker[j]);
                    if (s <=96 && s >= 91){
                        s += 6;
                    }
                    endWith.append(s);
                }
                char s = (char) (startFromAll.charAt(4) + (i + 1) * sizePerWorker[4] - 1);
                if (s <=96 && s >= 91){
                    s += 6;
                }
                endWith.append(s);
            }
            workerInfo.send(md5, startFrom.toString(), endWith.toString(), id);
        }
    }
//
//    public void remove(WorkerInfo workerInfo) throws IOException {
//        workerList.remove(workerInfo);
//        workerInfo.close();
//    }
//
//    public void removeWith(String md5, String startFrom, String endWith, WorkerInfo workerInfo) throws IOException {
//        workerList.remove(workerInfo);
//        workerInfo.close();
//        assign(md5, startFrom, endWith);
//    }

    public void success(int id, String ans, WorkerInfo workerInfo) throws IOException {
        if (id != crackerId.get()){
            return;
        }
        toInterface.response(ans);
        crackerId.incrementAndGet();
        getResponse.set(0);
        for (WorkerInfo info : workerList) {
            info.stop();
        }
    }

    public void fail(int id, WorkerInfo workerInfo) throws IOException {
        if (id != crackerId.get()){
            return;
        }
        int i = getResponse.incrementAndGet();
        if (i == currentWorker.get()){
            toInterface.response("fail, can't find ans");
            getResponse.set(0);
        }
//        toInterface.response("fail");
//        for (WorkerInfo info : workerList) {
//            info.stop();
//        }
    }

    public static void main(String[] args) {
        try {
            new Master();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
