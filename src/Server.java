import java.io.* ;
import javax.net.ssl.*;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.* ;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class Server  {
    final int port ;
    private int clientNum;
    private ArrayList<clientThread> clientList;

    public static void main(String args[]) {
        int port = 2501 ;
        new Server(port)  ;
    }

    Server(int port) {
        this.clientNum = 0;
        this.port = port;
        try {
            clientList = new ArrayList<clientThread>();
            // initialize executor service
            ExecutorService executorService = Executors.newFixedThreadPool(5);
            //*******************************************************************************
            SecureRandom secureRandom = new SecureRandom();
            secureRandom.nextInt(); // ensure that delay will happen st the start of the program and not later in the program.
            // creating keystore objects
                // initializing server keystores
                KeyStore clientKeyStore = KeyStore.getInstance("JKS");
                clientKeyStore.load(new FileInputStream("client.public"), "public".toCharArray());
                // intitialzing client keystore
                KeyStore serverKeyStore = KeyStore.getInstance("JKS");
                serverKeyStore.load(new FileInputStream("server.private"), "serverpw".toCharArray());
                TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
                tmf.init(clientKeyStore);
                KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
                kmf.init(serverKeyStore, "serverpw".toCharArray());
                SSLContext sslContext = SSLContext.getInstance("TLS");
                sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), secureRandom);
                SSLServerSocketFactory sf = sslContext.getServerSocketFactory();
                SSLServerSocket ss = (SSLServerSocket) sf.createServerSocket(port);
                ss.setNeedClientAuth(false);
                //*******************************************************************************
                //ServerSocket ss = new ServerSocket(port);
                while (true) {
                    SSLSocket s = (SSLSocket) ss.accept();
                    if (s == null)
                        System.out.println("Socket could not be intialized !");
                    clientNum++;
                    executorService.execute(new clientThread(s));
                    //System.out.println("client thread started") ;
                }
            } catch (IOException | NoSuchAlgorithmException | KeyManagementException | KeyStoreException | UnrecoverableKeyException | CertificateException e) {
                System.out.println("IOException occurred: ");
                e.printStackTrace();


            }
        }


 /*   class acceptorThread implements Runnable
    {
        acceptorThread(ExecutorService executorService)
        {
            Thread t = new Thread(this,"socketAcceptor") ;
            t.start() ;
        }

        public void run()
        {
            //runnable code
        }
    } */

    class clientThread implements Runnable {
        String username;
        SSLSocket socket;
        //Thread clientMain;
        //Thread clientListen;
        InputStream inputStream;
        OutputStream outputStream;
        BufferedWriter sendToClient ;
        SSLSession sslSession ;

        clientThread(SSLSocket socket) throws IOException {
            username = "temp";
            this.socket = socket;
            sslSession = socket.getSession() ;
            System.out.println(this.socket) ;
            inputStream = socket.getInputStream();
            outputStream = socket.getOutputStream();
            sendToClient = new BufferedWriter(new OutputStreamWriter(outputStream));
            //Thread t = new Thread(this, username);
            //clientMain = t ;
            clientList.add(this) ;
            //System.out.println("Starting client thread") ;
            //t.start();
        }

        public void run()  {
            try {
                BufferedReader msgFromClient = new BufferedReader(new InputStreamReader(inputStream));
                sendToClient.write("Welcome to chat server. There are currently " + clientNum + " users online. \n Please pick a username: ");
                sendToClient.newLine();
                sendToClient.flush();
                System.out.println("Thread name: " + Thread.currentThread()) ;
                //System.out.println("Sent a message to client") ;
                username = msgFromClient.readLine();

                // initialize a new thread for listening from the client

                while (true) {
                    String msg;
                    if ((msg = msgFromClient.readLine())!=null) {
                        if (msg.equalsIgnoreCase("$crypto"))
                        {
                            writeToClient(new StringBuilder().append(">>SERVER: Cipher suite: ").append(sslSession.getCipherSuite()).toString());
                            writeToClient(new StringBuilder().append(">>SERVER: Protocol: ").append(sslSession.getProtocol()).toString());
                            continue ;
                        }
                        System.out.println(">> MSG from " + username + ": " + msg);
                        // do something ==> broadcast
                        broadcast(this.username,msg) ;
                        Thread.sleep(100) ;

                    }
                }
            } catch (IOException | InterruptedException e)
            {
                System.out.println("IOException occured: ") ;
                e.printStackTrace();
            }



        }

        private void writeToClient(String msg)
        {
            try {
                sendToClient.write(msg);
                sendToClient.newLine();
                sendToClient.flush();
            } catch(IOException e)
            {
                System.out.println("IOException generated: " );
                e.printStackTrace();
            }
        }

    }

    private synchronized void broadcast(String fromUser , String msg) throws IOException
    {
        for (clientThread client : clientList)
        {
            // System.out.println("Inside broadcast: ") ;
            //BufferedWriter sendToClient = new BufferedWriter(new OutputStreamWriter(client.outputStream));
            client.writeToClient(fromUser + ": " + msg);

        }
    }


}

