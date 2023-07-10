import java.io.*;
import lejos.nxt.*;
import lejos.nxt.comm.*;

public class BTJ {

    public static void main(String[] args) {
        BTConnector connector = new BTConnector();

        System.out.println("0. Auf Signal warten");

        NXTConnection conn = connector.waitForConnection(0, NXTConnection.RAW);

        InputStream is = conn.openInputStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(is), 1);

        String message = "";

        while (true){

            System.out.println("1. Schleife gestartet");
            message = "";

            try {
                message = br.readLine();
                System.out.println("2. Message: " + message);
            } catch (IOException e) {
                e.printStackTrace(System.out);

        }
    }
}