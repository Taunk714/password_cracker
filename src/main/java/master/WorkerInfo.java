package master;

import utils.Client;

import java.io.IOException;

public class WorkerInfo {

    private String ipAddress;
    private int port;


    private String targetMD5;
    private String startFrom;
    private String endWith;

    private Client toWorker;

    private Master master;

    public WorkerInfo(String ipAddress, int port, Master master) throws IOException {
        this.ipAddress = ipAddress;
        this.port = port;
        toWorker = new Client(ipAddress, port);
        this.master = master;
    }

    public void send(String md5, String startFrom, String endWith) throws IOException {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    WorkerInfo.this.targetMD5 = md5;
                    WorkerInfo.this.startFrom = startFrom;
                    WorkerInfo.this.endWith = endWith;
                    toWorker.send(generateRequestString(md5, startFrom, endWith));
                    receive();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    public void remove() throws IOException {
        toWorker.send("remove");
    }

    public void receive() throws IOException {
        String request = null;
        while((request = toWorker.receive())!=null){
            if (request.startsWith("success")){
                String[] s = request.split(" ");
                master.success(s[1], this);
            }else if (request.startsWith("fail")){

            }else if (request.startsWith("remove")){
                String[] s = request.split(" ");
                if (s.length == 1){
                    master.remove(this);
                }else{
                    master.removeWith(s[1], s[2], s[3], this);
                }
            }
        }
    }

    public void stop() throws IOException {
        toWorker.send("stop");
    }

    public void close() throws IOException {
        toWorker.close();
    }

    private String generateRequestString(String md5, String startFrom, String endWith){
        return md5 + " "
                + startFrom + " "
                + endWith;
    }

    public String getTargetMD5() {
        return targetMD5;
    }

    public void setTargetMD5(String targetMD5) {
        this.targetMD5 = targetMD5;
    }

    public String getStartFrom() {
        return startFrom;
    }

    public void setStartFrom(String startFrom) {
        this.startFrom = startFrom;
    }

    public String getEndWith() {
        return endWith;
    }

    public void setEndWith(String endWith) {
        this.endWith = endWith;
    }
}
