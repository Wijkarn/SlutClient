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

        //Init stuff. Set as null to be initialized as "something"
        Socket socket = null;
        InputStreamReader inputSR = null;
        OutputStreamWriter outputSW = null;
        BufferedReader bReader = null;
        BufferedWriter bWriter = null;

        //Starta Klienten
        try {
            //Init Socket med specifik port
            socket = new Socket("localhost", 42069);

            //Initiera Reader och Writer och koppla dem till socket
            inputSR = new InputStreamReader(socket.getInputStream());
            outputSW = new OutputStreamWriter(socket.getOutputStream());
            bReader = new BufferedReader(inputSR);
            bWriter = new BufferedWriter(outputSW);

            //Initiera Scanner för att skriva i konsol
            Scanner scan = new Scanner(System.in);

            while (true) {
                //Anroppar meny för användare, låter dem göra ett val.
                //Valet returneras som ett färdigt JSON string
                String message = userInput();

                //Skicka meddelande till server
                bWriter.write(message);
                bWriter.newLine();
                bWriter.flush();

                //Hämta response från server
                String resp = bReader.readLine();

                //Anropa openResponse metod med server response
                openResponse(resp, message);

                //Avsluta om QUIT
                if (message.equalsIgnoreCase("quit")) break;
            }
        } catch (UnknownHostException e) {
            System.out.println(e);
        } catch (IOException e) {
            System.out.println(e);
        } catch (ParseException e) {
            System.out.println(e);
        } finally {
            try {
                //Stäng kopplingar
                if (socket != null ) socket.close();
                if (inputSR != null ) inputSR.close();
                if (outputSW != null ) outputSW.close();
                if (bWriter != null ) bWriter.close();
                if (bReader != null ) bReader.close();
            } catch (Exception e) {
                System.out.println(e);
            }
            System.out.println("Client quits.");
        }
    }

    static String userInput() {
        //Steg 1. Skriv ut en meny för användaren
        System.out.println("Who do you want information about?\nTo name all people type \"name\".");

        //Steg 2. Låta användaren göra ett val
        Scanner scan = new Scanner(System.in);
        System.out.print("Your choice:");

        String choice = scan.nextLine();
        String choiceSend = choice.toLowerCase();

        //Steg 3. Bearbeta användarens val
        //Skapa JSON objekt för att hämta data om alla personer. Stringifiera objeketet och returnera det
        JSONObject jsonReturn = new JSONObject();
        jsonReturn.put("httpURL", choiceSend);
        jsonReturn.put("httpMethod", "get");

        //System.out.println(jsonReturn.toJSONString());

        //Returnera JSON objekt
        return jsonReturn.toJSONString();

        //return "error";
    }

    static String openResponse(String resp, String message) throws ParseException {
        //System.out.println(message);

        JSONParser parser = new JSONParser();
        JSONObject jsonOb = (JSONObject) parser.parse(message);
        String messageString = jsonOb.get("httpURL").toString();
        String[] cutMsgString = messageString.split("/");
        String who = cutMsgString[0];
        //System.out.println(who);

        String stringResponse = "";
        //Init Parser för att parsa till JSON Objekt
        //JSONParser parser = new JSONParser();

        //Skapar ett JSON objekt från server respons
        JSONObject serverResponse = (JSONObject) parser.parse(resp);

        //Kollar om respons lyckas
        if ("200".equals(serverResponse.get("httpStatusCode").toString())) {

            //Bygger upp ett JSONObjekt av den returnerade datan
            JSONObject data = (JSONObject) parser.parse((String) serverResponse.get("data"));
            System.out.println(data);

            if("name".equals(who)) {
                //Hämtar en lista av alla nycklar attribut i data och loopar sedan igenom dem
                Set<String> keys = data.keySet();
                for (String x : keys) {
                    //Hämtar varje person object som finns i data
                    JSONObject person = (JSONObject) data.get(x);

                    //Skriv ut namnet på person
                    System.out.println(person.get(who));
                }
            }
            else{
                JSONObject person = (JSONObject) data.get(who);

                //Skriv ut namnet på person
                System.out.println(person.get("name"));
                System.out.println(person.get("rank"));
                System.out.println(person.get("legion"));
            }
        }

        return stringResponse;
    }
}