import java.net.ServerSocket;
import java.net.Socket;
import java.io.*;

public class MyProcess implements Runnable{
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private Socket serverSoc;
    private ObjectInputStream objectInputStream;
    private ObjectOutputStream objectOutputStream;

    private Message msg;
    private String clientIP;
    private int serverPort, clientPort;
    public BufferedWriter writer;
    public String lastSeenIP;


    public MyProcess(String clientIP, int serverPort, int clientPort) throws IOException {
        this.msg = new Message(); //unique message
        this.serverPort = serverPort; //server port on local machine
        this.clientIP = clientIP; //neighbor IP
        this.clientPort = clientPort; //neighbor port #
        this.writer = new BufferedWriter(new FileWriter("log.txt")); //create the log file for the ring system
        this.writer.write("----------------------------------------------"+"\n"); //divider to be written to file
    }

    /*
        recieveMessage()
            ~runs the ring system and will send and receive messages
    */
    public void receiveMessage(){
        try{
            this.serverSocket = new ServerSocket(this.serverPort); //establish the server socket
            System.out.println("Sever connection is established");
            while(true){
                this.serverSoc = serverSocket.accept(); //accept a new connection
                System.out.println("Connected to : "+ this.serverSoc.getInetAddress().getHostAddress()); //print out client ip

                //read in the sent Message object
                objectInputStream = new ObjectInputStream(serverSoc.getInputStream());
                this.lastSeenIP = this.serverSoc.getInetAddress().getHostAddress(); //keep track of the last connected ip
                Message msg = (Message) objectInputStream.readObject(); //message received
                outputToFile(msg, 1); //log the received message

                //output the message ID (received and the current user ID)
                System.out.println("Message that was received: "+ msg.msgId);
                System.out.println("Compared to your message: "+ this.msg.msgId);

                //leader message was received
                if(msg.flag == 1){
                    System.out.println("Leader was elected.");
                    if(!msg.msgId.equals(this.msg.msgId)){ //msg id does not match current object id
                        System.out.println("You are not the leader");
                    }else{ //msg ids match
                        System.out.println("You are the leader");
                    }
                    forwardMessage(msg); //forward the leader message
                    break; //exit gracefully
                }
                //if the incoming message does match yours, declare your Msg the leader
                if(msg.msgId.equals(this.msg.msgId)){
                    System.out.println("You are the leader!");
                    this.msg.flag = 1; //set your msg id to 1
                    forwardMessage(this.msg); //forward your message
                }
                //keep facilitating the sending of messages
                else if (msg.msgId.compareTo(this.msg.msgId) > 0){
                    System.out.println("You are not the leader");
                    forwardMessage(msg);
                }
                System.out.println("From client: "+msg.msgId);
            }
        }catch (IOException e){
            System.out.println("Error: Could not start the server");
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public void forwardMessage(Message msg){
        try{
            outputToFile(msg, 2); //output details of forwarded message
            this.clientSocket = new Socket(this.clientIP , this.clientPort); //create a client connection to next node
            this.objectOutputStream = new ObjectOutputStream(clientSocket.getOutputStream()); //create the output stream
            this.objectOutputStream.writeObject(msg); //write out the object to buffer
            this.objectOutputStream.flush(); //flush and send
        }catch(IOException e){
            System.out.println("Error: Could not connect.");
            e.printStackTrace();
        }
    }

    public void outputToFile(Message msg, int type) throws IOException{
        if(type == 1){ //message received
            this.writer.append("MESSAGE RECEIVED FROM "+this.lastSeenIP+"\n");
            this.writer.append("UUID: "+msg.msgId.toString()+"\n");
            this.writer.append("FLAG: "+msg.flag+"\n");
            this.writer.append("----------------------------------------------"+"\n");
        }
        if (type == 2){ //message to send to other client
            this.writer.append("MESSAGE BEING SENT TO "+this.clientIP+"\n");
            this.writer.append("UUID: "+msg.msgId+"\n");
            this.writer.append("FLAG: "+msg.flag+"\n");
            this.writer.append("----------------------------------------------"+"\n");
        }
        this.writer.flush(); //push to file
    }


    public static void main(String[] args) throws InterruptedException, IOException {
        System.out.println("Establishing a connection");
        try{
            BufferedReader reader = new BufferedReader(new FileReader("src/config.txt")); //read in config file
            String line = reader.readLine();
            String [] connectionDetails = line.split(","); //ip,portNumber
            MyProcess process = new MyProcess(connectionDetails[0], 5000, //make new instance of process
                   Integer.parseInt(connectionDetails[1]));
            Thread.sleep(1000); //second delay for synchronization
            Thread server = new Thread(process); //spawn a new thread for initial connection
            server.start(); //start the ring process
            System.out.println("Assigned ID: "+process.msg.msgId);
            Thread.sleep(1000); //second delay
            process.forwardMessage(process.msg); //initial forwarding of the message exchange
        }catch (IOException e){
            System.out.println(e);
        }

    }

    @Override
    public void run() {
        receiveMessage();
    }
}
