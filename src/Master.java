import java.io.IOException;
import java.util.ArrayList;

/**
 * This is the master node. Get md5 from interface, and assign it to workers.
 */
public class Master {
    private static int port = 1203;
    private static int workerPort = 1207;

    private ArrayList<WorkerInfo> workerList = new ArrayList<WorkerInfo>();

    private Server toInterface;
    private Server toWorker;


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
            if (request.startsWith("md5")){
                String md5 = request.substring(4);
                assign(md5);
                System.out.println("get md5");
            }else if (request.startsWith("remove")){
                // remove
            }

        }
    }

    private void assign(String md5) throws IOException {
        assign(md5, "AAAAA", "zzzzz");
    }

    private void assign(String md5, String startFromAll, String endWithAll) throws IOException {
        int workerNum = workerList.size();
        int[] sizePerWorker = new int[5];
        for (int i = 0; i < 5; i++) {
            sizePerWorker[i] = (endWithAll.charAt(i) - startFromAll.charAt(i) + 1);
        }
        for (int i = 0; i < workerNum; i++) {
            WorkerInfo workerInfo = workerList.get(i);
            StringBuilder startFrom = new StringBuilder();
            for (int j = 0; j < 5; j++) {
                startFrom.append((char) (startFromAll.charAt(j) + i * sizePerWorker[j]));
            }
            StringBuilder endWith = new StringBuilder();
            if ( i == workerNum -1){
                endWith = new StringBuilder(endWithAll);
            }else{
                for (int j = 0; j < 4; j++) {
                    endWith.append((char) (startFromAll.charAt(j) + (i + 1) * sizePerWorker[j]));
                }
                endWith.append((char) (startFromAll.charAt(4) + (i + 1) * sizePerWorker[4] - 1));
            }
            workerInfo.send(md5, startFrom.toString(), endWith.toString());
        }
    }

    public void remove(WorkerInfo workerInfo) throws IOException {
        workerList.remove(workerInfo);
        workerInfo.close();
    }

    public void removeWith(String md5, String startFrom, String endWith, WorkerInfo workerInfo) throws IOException {
        workerList.remove(workerInfo);
        workerInfo.close();
        assign(md5, startFrom, endWith);
    }

    public void success(String ans, WorkerInfo workerInfo) throws IOException {
        toInterface.response(ans);
        for (WorkerInfo info : workerList) {
            info.stop();
        }
    }



    public static void main(String[] args) {
        try {
            new Master();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
