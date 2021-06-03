package com.yvesnyc;


import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.DefaultRedirectStrategy;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.client5.http.entity.mime.StringBody;
import org.apache.hc.core5.http.io.entity.StringEntity;

import java.io.IOException;
import java.net.URI;


/**
 * kodou.io example Java functions for writing to Airtable 
 * Created using VSCode
 */
public final class App {
    private App() {
    }

    /**
     * Says hello to the world. This is a kodou.io functions library so main() is not expected to be called
     * @param args The arguments of the program.
     */
    public static void main(String[] args) {
        System.out.println("Hello World!");
    }

    /** 
     * The text entered into the form will be converted to a positive Integer, if valid
     * @param textEntry  The text entry value from the form
     */
    public static int checkPositiveInt(String textEntry) {
        int number = -1;

        try {
            number = Integer.parseInt(textEntry);
        } catch (Exception e) {
            // Text isnt a valid Integer so our rules set it to negative
            // number is still -1
        }

        return number;
    }

    /** 
     * Form #47 has 3 entries that should be converted to positive integers.
     * Convert entries into positive integers, if possible, otherwise send an empty array of ints
     * @param entryA First form entry of web page
     * @param entryB Second form entry of web page
     * @param entryC Third form entry of web page
     *  Return int [] - The three form entries validated as integers
    */
    public static int [] processForm47(String entryA, String entryB, String entryC) {

        int valueA, valueB, valueC;

        // Check 
        valueA = checkPositiveInt(entryA);
        valueB = checkPositiveInt(entryB);
        valueC = checkPositiveInt(entryC);

        if (valueA >= 0 && valueB >= 0 && valueC >= 0) {
            int [] arr = new int[3];
            arr[0]= valueA; arr[1] = valueB; arr[2] = valueC;   
            return arr;
        } else {
            int [] arr = new int[0];
            return arr;
        }
    }

    /** 
     *   Function to write (fictitious) form 47 contents to an Airtable with a supplied Airtable api id and key 
     * @param api_id Airtable api handle for the account
     * @param api_key Airtable api key
     * @param baseTable Name of base table we want to write to
     * @param entryA First form entry of web page
     * @param entryB Second form entry of web page
     * @param entryC Third form entry of web page
     * Return int - Either a http return code from Airtable or -1 if something else went wrong
    */
    public static int addForm47ToAirtable(String api_id, String api_key, String baseTable, String entryA, String entryB, String entryC) {


        /*
        This is the json representation of the Airtable record we are creating.
        The Airtable Http API request will write to baseTable
        { "records" : [
            {
                "fields" : {
                    "name" : entryA, 
                    "age" : entryB, 
                    "gender" : entryC
                    }
            }
            ]
        }

        */

        // Map Form entries EntryA, EntryB, EntryC into the Airtable fields Name, Age, Gender
        String name = "\"".concat(entryA).concat("\""); /* name String surrounded by quotes */
        String age = entryB; /* An integer */
        String gender = "[\"Male\"]"; // Can be Male or Female in Airtable

        if (entryC.equalsIgnoreCase("Female")) { /* Singe array gender String surrounded by quotes */
            gender = "[\"Female\"]";
        }

        // "[\"".concat(entryC).concat("\"]");

        String jsonString = "{ \"records\" : [ { \"fields\" : {"
        .concat("\"name\" : ").concat(name).concat(",")
        .concat("\"age\" : ").concat(age).concat(",")
        .concat("\"gender\" : ").concat(gender)
        .concat("} } ] }");

        try (CloseableHttpClient httpclient = HttpClients.custom().setRedirectStrategy(new DefaultRedirectStrategy()).build()){

            /* http request to Airtable  */
            HttpPost httpPost= new HttpPost(URI.create("https://api.airtable.com/v0/".concat(api_id)));
            httpPost.addHeader("Authorization", "Bearer ".concat(api_key));
            httpPost.addHeader("Content-Type","application/json");
            httpPost.setEntity(new StringEntity( jsonString,ContentType.APPLICATION_JSON));

            try (CloseableHttpResponse response1 = httpclient.execute(httpPost)) {
                return response1.getCode();
            }

        } catch (IOException ex) {
            /*
            Return a -1 if something other than an http error code results. Could have been an IO failure
            */

            return -1;
        }


    }

    /** 
     *  Send the Airtable record formatted in a Json String
     * @param api_id Airtable api handle for the account
     * @param api_key Airtable api key
     * @param baseTable Name of base table we want to write to
     * @param jsonFormat A JSON String with the record to be written to Airtable
     *  Return int - Either a http return code from Airtable or -1 if something else went wrong
    */
    public static int addFormXXToAirtable(String api_id, String api_key, String baseTable, String jsonFormat) {

        /* client for http calls */
        try (CloseableHttpClient httpclient = HttpClients.custom().setRedirectStrategy(new DefaultRedirectStrategy()).build()) {

            /* http request to Airtable  */
            HttpPost httpPost = new HttpPost(URI.create("https://api.airtable.com/v0/".concat(api_id)));
            httpPost.addHeader("Authorization", "Bearer ".concat(api_key));
            httpPost.addHeader("Content-Type", "application/json");
            httpPost.setEntity(new StringEntity(jsonFormat, ContentType.APPLICATION_JSON));

            try (CloseableHttpResponse response1 = httpclient.execute(httpPost)) {
                return response1.getCode();
            }

        } catch (IOException ex) {
            /*
            Return a -1 if something other than an http error code results. Could have been an IO failure
            */

            return -1;
        }
    }

}
