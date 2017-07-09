import java.net.*;
import java.io.*;
import java.util.regex.*;

/**
 * @author feltocraig
 * @author vreutskiy
 */

class Bpp {
  /**
   * Returns the nth digit of pi followed by the next 8 numbers
   * @param n - nth number of pi to return
   * @return returns an integer value containing 8 digits after n
   */
  public int getDecimal(long n) {
      long av, a, vmax, N, num, den, k, kq, kq2, t, v, s, i;
      double sum;

      N = (long) ((n + 20) * Math.log(10) / Math.log(2));

      sum = 0;

      for (a = 3; a <= (2 * N); a = nextPrime(a)) {

          vmax = (long) (Math.log(2 * N) / Math.log(a));
          av = 1;
          for (i = 0; i < vmax; i++)
              av = av * a;

          s = 0;
          num = 1;
          den = 1;
          v = 0;
          kq = 1;
          kq2 = 1;

          for (k = 1; k <= N; k++) {

              t = k;
              if (kq >= a) {
                  do {
                      t = t / a;
                      v--;
                  } while ((t % a) == 0);
                  kq = 0;
              }
              kq++;
              num = mulMod(num, t, av);

              t = (2 * k - 1);
              if (kq2 >= a) {
                  if (kq2 == a) {
                      do {
                          t = t / a;
                          v++;
                      } while ((t % a) == 0);
                  }
                  kq2 -= a;
              }
              den = mulMod(den, t, av);
              kq2 += 2;

              if (v > 0) {
                  t = modInverse(den, av);
                  t = mulMod(t, num, av);
                  t = mulMod(t, k, av);
                  for (i = v; i < vmax; i++)
                      t = mulMod(t, a, av);
                  s += t;
                  if (s >= av)
                      s -= av;
              }

          }

          t = powMod(10, n - 1, av);
          s = mulMod(s, t, av);
          sum = (sum + (double) s / (double) av) % 1;
      }
      return (int) (sum * 1e1); // 1e9 is 9 decimal places
  }

  private long mulMod(long a, long b, long m) {
      return (long) (a * b) % m;
  }

  private long modInverse(long a, long n) {
      long i = n, v = 0, d = 1;
      while (a > 0) {
          long t = i / a, x = a;
          a = i % x;
          i = x;
          x = d;
          d = v - t * x;
          v = x;
      }
      v %= n;
      if (v < 0)
          v = (v + n) % n;
      return v;
  }

  private long powMod(long a, long b, long m) {
      long tempo;
      if (b == 0)
          tempo = 1;
      else if (b == 1)
          tempo = a;

      else {
          long temp = powMod(a, b / 2, m);
          if (b % 2 == 0)
              tempo = (temp * temp) % m;
          else
              tempo = ((temp * temp) % m) * a % m;
      }
      return tempo;
  }

  private boolean isPrime(long n) {
      if (n == 2 || n == 3)
          return true;
      if (n % 2 == 0 || n % 3 == 0 || n < 2)
          return false;

      long sqrt = (long) Math.sqrt(n) + 1;

      for (long i = 6; i <= sqrt; i += 6) {
          if (n % (i - 1) == 0)
              return false;
          else if (n % (i + 1) == 0)
              return false;
      }
      return true;
  }

  private long nextPrime(long n) {
      if (n < 2)
          return 2;
      if (n == 9223372036854775783L) {
          System.err.println("Next prime number exceeds Long.MAX_VALUE: " + Long.MAX_VALUE);
          return -1;
      }
      for (long i = n + 1;; i++)
          if (isPrime(i))
              return i;
  }
}

class ServerThread implements Runnable {
  private Bpp bpp;
  private int port;
  private Thread t;

  public ServerThread() {
    this.bpp = new Bpp();
    this.port = 13131;
  }

  public void run() {
    // Server will live until Ctrl+C or fatal error
    try (ServerSocket server = new ServerSocket(this.port)) {
      System.out.println(String.format("Listening for connection on port %d ....", this.port));
      while (true) {

        try (Socket socket = server.accept()) {
          // Get number of digit position in Pi after comma
          int position = this.getPositionFromHttpRequest(socket);

          // Calculate this value (do not try to find errors here, everything is ok)
          int value = this.bpp.getDecimal(position);

          // Send response to client
          this.sendHttpResponse(socket, position, value);
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
      return;
    }
  }

  public void start() {
    if (t == null) {
      t = new Thread(this, String.format("Thread for consuming"));
      t.start();
    }
  }

  private int getPositionFromHttpRequest(Socket socket) throws IOException {
    int position = 0;

    // Pattern for GET request parsing: "GET /1 HTTP/1.1"
    String patternStr = "^GET\\s+/([0-9]+)\\s+HTTP/1.1$";
    Pattern pattern = Pattern.compile(patternStr);

    InputStreamReader isr = new InputStreamReader(socket.getInputStream());
    BufferedReader reader = new BufferedReader(isr);
    String line = reader.readLine();

    System.out.println("\r\nReceived request:");

    // Reading line by line in aim to find one with 'GET ...' body
    while (line != null && !line.isEmpty()) {
      System.out.println(line);
      Matcher m = pattern.matcher(line);
      if(m.matches()) {
        System.out.println(line);
        try {
          position = Integer.parseInt(m.group(1)); //Place for error
        } catch (NumberFormatException e) {
          line = reader.readLine();
          continue;
        }
      }
      line = reader.readLine();
    }

    return position;
  }

  private void sendHttpResponse(Socket socket, int position, int value) throws IOException {
    System.out.println("Value = " + value);
    String response = String.format("%d - %d", position, value);
    int length = response.getBytes("UTF-8").length;
    String httpResponse = String.format("HTTP/1.1 200 OK\r\nContent-Length: %d\r\nContent-Type: text/html\r\nConnection: Closed\r\n\r\n%s", length, response);

    System.out.println("Sending 200 response:");
    System.out.println(httpResponse);

    socket.getOutputStream().write(httpResponse.getBytes("UTF-8"));
  }

}

public class BppServer {
  private int threadCount;

  public BppServer() {
    this.threadCount = 1;
  }

  private void createWorkerThreads(int amount) {
    for (int i = 0; i < amount; ++i) {
      ServerThread thread = new ServerThread();
      thread.start();
    }
  }

  private void runHttpServer() {
    // Create server socket with 'try-with-resources' approach
    this.createWorkerThreads(this.threadCount);
  }

  /**
   * Runs the program
   * @param args
   */
  public static void main(String args[]) {

      BppServer server = new BppServer();

      server.runHttpServer();
  }

}
