import java.io.* ;
import java.net.* ;
import javax.net.ssl.*;
import java.security.*;
import java.security.cert.CertificateException;

class Client
{
    public static void main(String args[]) throws IOException
    {
        try {
            int port = 2501;
            //****************************************************************************************
            SecureRandom secureRandom = new SecureRandom() ;
            secureRandom.nextInt() ;
            // creating keystore objects
            KeyStore clientKeyStore = KeyStore.getInstance("JKS") ;
            clientKeyStore.load(new FileInputStream("client.private"), "clientpw".toCharArray()) ;
            // intitialzing server keystore
            KeyStore serverKeyStore = KeyStore.getInstance("JKS") ;
            serverKeyStore.load(new FileInputStream("server.public"), "public".toCharArray()) ;
            TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509") ;
            tmf.init(serverKeyStore);
            KeyManagerFactory kmf = KeyManagerFactory.getInstance( "SunX509" );
            kmf.init(clientKeyStore, "clientpw".toCharArray()) ;
            SSLContext sslContext = SSLContext.getInstance("TLS") ;
            sslContext.init(kmf.getKeyManagers(),tmf.getTrustManagers(), secureRandom) ;
            SSLSocketFactory sf = sslContext.getSocketFactory() ;
            SSLSocket s = (SSLSocket)sf.createSocket(InetAddress.getLocalHost(),port) ;

            SSLSession sslSession = s.getSession() ;
            if (s!=null) {
                System.out.println("Connected successfully using SSL connection: ");
                System.out.println(new StringBuilder().append("Cipher suite: ").append(sslSession.getCipherSuite()).toString());
                System.out.println(new StringBuilder().append("Protocol: ").append(sslSession.getProtocol()).toString());
            }
            //****************************************************************************************
            //Socket s = new Socket(InetAddress.getLocalHost(), port);
            BufferedReader msgFromServer = new BufferedReader(new InputStreamReader(s.getInputStream())) ;
            BufferedWriter msgToServer = new BufferedWriter(new OutputStreamWriter(s.getOutputStream())) ;
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in)) ;
            // initialize a listening thread

            Thread listenThread = new Thread(()->{
                String msg ;
                try {
                    //System.out.println("Inside listen thread") ;
                    while ((msg = msgFromServer.readLine())!=null) {
                        System.out.println(msg);
                    }
                } catch(IOException e)
                {
                    System.out.println("IOException : ") ;
                    e.printStackTrace();
                }
            }) ;
            listenThread.start() ;
            try {
                while (true) {
                    //System.out.println("Your MSG: ");
                    String msg = br.readLine();
                    msgToServer.write(msg);
                    msgToServer.newLine();
                    msgToServer.flush();
                    // Thread.sleep(500);
                }
            }catch (IOException  e)
            {
                //
            }

        } catch (UnknownHostException | NoSuchAlgorithmException | KeyManagementException | KeyStoreException | UnrecoverableKeyException | CertificateException e)
        {
            // handle all exceptions
        }
    }
}