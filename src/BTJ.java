import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import lejos.remote.nxt.BTConnector;
import lejos.remote.nxt.NXTConnection;

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
}