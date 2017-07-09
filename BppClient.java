import java.io.*;
import java.net.*;
import java.util.regex.*;
import java.util.concurrent.*;

/**
 * @author vreutskiy
 */

 class HttpThread implements Runnable{
   private BlockingQueue<Integer> queue;
   private int port;
   private String address;
   private InetAddress ipAddress;
   private Thread t;

   public HttpThread(BlockingQueue<Integer> queue) {
     this.queue = queue;
     this.port = 14159;
     this.address = "tv-test-load-balancer-1025018990.us-east-2.elb.amazonaws.com";
     try {
       this.ipAddress = InetAddress.getByName(this.address);
     } catch (UnknownHostException e) {
       e.printStackTrace();
     }
   }

   public void start() {
     if (t == null) {
       t = new Thread(this, String.format("Thread for consuming"));
       t.start();
     }
   }

   public void run() {
     while(!this.queue.isEmpty()) {
       try {
         int position = this.queue.take();
         this.sendHttpRequestForNumber(position);
       } catch (InterruptedException e) {
         e.printStackTrace();
       }
     }

     System.out.println("Worker thread finished");
   }

   private void sendHttpRequestForNumber(int position) {
     try(Socket socket = new Socket(this.ipAddress, this.port)) {
       String httpRequest = String.format("GET /%d HTTP/1.1\r\nHost: %s:%d\r\nAccept: text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8\r\nAccept-Encoding: gzip, deflate\r\nAccept-Language: ru-RU,ru;q=0.8,en-US;q=0.6,en;q=0.4,de;q=0.2\r\nCache-Control: max-age=0\r\nUpgrade-Insecure-Requests: 1\r\nUser-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/59.0.3071.115 Safari/537.36\r\nConnection: keep-alive\r\n\r\n", position, this.address, this.port);

       socket.getOutputStream().write(httpRequest.getBytes("UTF-8"));

       InputStreamReader isr = new InputStreamReader(socket.getInputStream());
       BufferedReader reader = new BufferedReader(isr);
       String line = reader.readLine();

       while (line != null) {
         this.getNumberFromHttpResponse(line);
         line = reader.readLine();
       }
     } catch (IOException e) {
       e.printStackTrace();
     }
   }

   private int getNumberFromHttpResponse(String line) {
     String patternStr = "^([0-9]+) - ([0-9]+)$";
     Pattern pattern = Pattern.compile(patternStr);
     Matcher m = pattern.matcher(line);
     if(m.matches()) {
       String result = String.format("%s: %s", m.group(1), m.group(2));
       System.out.println(result);
     }
     return 0;
   }
 }

public class BppClient {
  private BlockingQueue<Integer> queue;

  public BppClient() {
    this.queue = new LinkedBlockingQueue<Integer>();
  }

  private int getUserInput() {
    int userAns = 10;
    try {
      BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
      userAns = Integer.parseInt(in.readLine());
    } catch (Exception e) {
      System.out.println("Problem in getting int from user input.");
      e.printStackTrace();
    }
    return userAns;
  }

  private void fillQueue(int amount) {
    for (int count = 1; count <= amount; count++) {
      // client.sendHttpRequestForNumber(i);
      try {
        this.queue.put(count);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }

  private void createWorkerThreads(int amount) {
    for (int i = 0; i < amount; ++i) {
      HttpThread thread = new HttpThread(queue);
      thread.start();
    }
  }

  /**
   * Runs the program
   * @param args
   */
  public static void main(String args[]) {
    BppClient client = new BppClient();
    System.out.println("How many digits of pi do you want to see?:");

    int userAns = client.getUserInput();

    client.fillQueue(userAns);

    System.out.println("How much workers you want to start?:");

    int workers = client.getUserInput();
    client.createWorkerThreads(workers);
  }
}
