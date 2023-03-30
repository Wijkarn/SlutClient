import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.*;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;

public class Main {
    public static void main(String[] args) {
        System.out.println("Client is ready!");

        // Init stuff. Set as null to be initialized as "something"
        Socket socket = null;
        InputStreamReader inputSR = null;
        OutputStreamWriter outputSW = null;
        BufferedReader bReader = null;
        BufferedWriter bWriter = null;

        // Starts the client
        try {
            //Init Socket with specific port
            socket = new Socket("localhost", 42069);

            // Init Reader and Writer och connect them to socket
            inputSR = new InputStreamReader(socket.getInputStream());
            outputSW = new OutputStreamWriter(socket.getOutputStream());
            bReader = new BufferedReader(inputSR);
            bWriter = new BufferedWriter(outputSW);

            // Init Scanner to write in console
            //Scanner scan = new Scanner(System.in);

            while (true) {
                // Calls the menu for user and lets them choose
                // The choice returns as a complete JSON String
                String message = userInput();

                // Close down if the user types "quit"
                if (message.equalsIgnoreCase("{\"httpURL\":\"quit\",\"httpMethod\":\"get\"}")) break;

                // Send message to server
                bWriter.write(message);
                bWriter.newLine();
                bWriter.flush();

                // Get response form server
                String resp = bReader.readLine();

                // Calls openResponse method with server response and client input
                openResponse(resp, message);
            }
        } catch (UnknownHostException e) {
            System.out.println(e);
        } catch (IOException e) {
            System.out.println(e);
        } catch (ParseException e) {
            System.out.println(e);
        } finally {
            try {
                // Close all connections
                if (socket != null) socket.close();
                if (inputSR != null) inputSR.close();
                if (outputSW != null) outputSW.close();
                if (bWriter != null) bWriter.close();
                if (bReader != null) bReader.close();
            } catch (Exception e) {
                System.out.println(e);
            }
            System.out.println("Client quits.");
        }
    }

    static String userInput() {
        // Prints menu for the user
        System.out.println("Who do you want information about?\nTo see all people type \"name\".");

        // Lets the user choose
        Scanner scan = new Scanner(System.in);
        System.out.print("Your choice:");

        String choice = scan.nextLine();
        String choiceSend = choice.toLowerCase();

        // Create JSONObjekt to get data for all people. Stringify object and return it
        JSONObject jsonReturn = new JSONObject();
        jsonReturn.put("httpURL", choiceSend);
        jsonReturn.put("httpMethod", "get");

        // Returns JSONObject
        return jsonReturn.toJSONString();
    }

    static void openResponse(String resp, String message) throws ParseException {
        // Converts client input to string again
        JSONParser parser = new JSONParser();
        JSONObject jsonOb = (JSONObject) parser.parse(message);
        String messageString = jsonOb.get("httpURL").toString();
        String[] cutMsgString = messageString.split("/");
        String userMessage = cutMsgString[0];

        //String stringResponse = "";

        // Creates a JSONObject from server response
        JSONObject serverResponse = (JSONObject) parser.parse(resp);

        // Check if response was successful
        if ("200".equals(serverResponse.get("httpStatusCode").toString())) {
            // Builds a JSONObject of the returned data
            JSONObject data = (JSONObject) parser.parse((String) serverResponse.get("data"));
            //System.out.println(data);

            if ("name".equals(userMessage) || "names".equals(userMessage)) {
                // Gets a list of all keys attribute in data and loops them
                Set<String> keys = data.keySet();
                for (String x : keys) {
                    // Gets all people from server
                    JSONObject person = (JSONObject) data.get(x);

                    // Prints out all names
                    System.out.println(person.get("name"));
                }
            } else {
                JSONObject person = (JSONObject) data.get(userMessage);

                // Prints out the values
                System.out.println("Name: " + person.get("name"));
                System.out.println("Rank: " + person.get("rank"));
                System.out.println("Legion: " + person.get("legion"));
            }
        }

        //return stringResponse;
    }
}