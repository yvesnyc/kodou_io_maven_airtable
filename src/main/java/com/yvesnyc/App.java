package com.yvesnyc;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.primitives.Ints;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.DefaultRedirectStrategy;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.StringEntity;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


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

        // Test addFormXXJsonToAirtable
        Map<String,String> form = new HashMap<String,String>();
        form.put("ss_name","me");
        form.put("mn_age", "44");
        form.put("notype", "none");

        // test
        ObjectMapper mapper = new ObjectMapper();
        String jsonFormat = "{}";

        try {
           jsonFormat = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(form);

        } catch (com.fasterxml.jackson.core.JsonProcessingException ex) {
            System.exit(-1);
        }

        int test = addFormXXJsonToAirtable("api_id","api_key", "test", jsonFormat);

        System.out.println("Done!");
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

        // The form field names match the parameters, the data from the web form arrives as arguments
        // The data will be checked and converted to an Airtable with column names that differ from the web form fields

        /*
        This is the json representation of the Airtable record we are creating.
        The Airtable Http API request will write to baseTable
        { "records" : [
            {
                "fields" : {
                    "Name" : entryA,
                    "Age" : entryB,
                    "Gender" : entryC
                    }
            }
            ]
        }
        */

        // Validate data. EntryB aka Age should be positive integer
        Integer  validAge = Ints.tryParse(entryB); // returns null if not valid

        if (validAge != null && validAge > 0) {

            // Map Web form entries EntryA, EntryB, EntryC into the Airtable fields Name, Age, Gender
            String name = "\"".concat(entryA).concat("\""); /* name String surrounded by quotes */
            String age = entryB; /* An integer */

            // Perform some processing of the data
            String gender = "[\"Male\"]"; // Can be Male or Female in Airtable
            if (entryC.equalsIgnoreCase("Female")) { /* Singe array gender String surrounded by quotes */
                gender = "[\"Female\"]";
            }

            // Turn the data into a Json String for Airtable
            String jsonString = "{ \"records\" : [ { \"fields\" : {"
                    .concat("\"Name\" : ").concat(name).concat(",")
                    .concat("\"Age\" : ").concat(age).concat(",")
                    .concat("\"Gender\" : ").concat(gender)
                    .concat("} } ] }");

            URI uri = URI.create("https://api.airtable.com/v0/".concat(api_id).concat("/").concat(baseTable));

            // Use Java Http Client to send data to Airtable API
            try (CloseableHttpClient httpclient = HttpClients.custom().setRedirectStrategy(new DefaultRedirectStrategy()).build()) {

                /* http request to Airtable  */
                HttpPost httpPost = new HttpPost(uri);
                httpPost.addHeader("Authorization", "Bearer ".concat(api_key));
                httpPost.addHeader("Content-Type", "application/json");
                httpPost.setEntity(new StringEntity(jsonString, ContentType.APPLICATION_JSON));

                try (CloseableHttpResponse response1 = httpclient.execute(httpPost)) {
                    return response1.getCode();
                }

            } catch (IOException ex) {

                /*
                Return a -1 if something other than an http error code results. Could have been an IO failure
                */

                return -1;
            }

        } else { // if (validAge ..., not valid data
            return -1;
        }

    }

    /**
     *  Convert a tuple into the appropriate representation for Json String, Json Number, or Boolean.
     *  The first code is for singular or multiple, meaning, either a value or an singleton array
     *  example: ms_Name : ["Bob"] or ss_Name "Bob"
     * @param name id of tuple
     * @param value The value
     * @return  Either quoted or unquoted value String
     */
    public static void typeFormatByName(String name, String value, String [] decoded) {
        // If encoded type is String or no type at all, it is surrounded by quotes

        // First decode for single or multiple Airtable types
        if (name.matches("^s(s|n|b)_.+")) { // Single
            decoded[0] = name.substring(3);
            if (name.charAt(1) == 's') // String
                decoded[1] = "\"" + value + "\"";
            else
                decoded[1] = value;
            return;
        } else if (name.matches("^m(s|n|b)_.+")) { // Mulitple
            decoded[0] = name.substring(3);
            if (name.charAt(1) == 's')  // String
                decoded[1] = "[\"" + value + "\"]";
            else
                decoded[1] = "[" + value + "]";
            return;
        } else { // Uncoded, so just a default String
            decoded[0] = name;
            decoded[1] = "\"" + value + "\"";
            return;
        }

    }

    /**
     *
     * @param jsonFormat json String with a 'fields' object of form field names encoded with type info
     * @return json String with field values quote surrounded or not, single or multi. Field names without type codes
     */
    public static String decodeTypeEncodedJson(String jsonFormat) {
        // Convert the json fields to type based on name type hints
        TypeReference<HashMap<String, Object>> typeRef = new TypeReference<HashMap<String, Object>>() {};

        ObjectMapper mapper = new ObjectMapper();

        // Json fields will be assigned to a Map
        Map<String, String> mapOfJson = null;

        try {
            Map<String,Object> map = mapper.readValue(jsonFormat, typeRef);
            mapOfJson = (HashMap<String,String>) map.get("fields"); // The json has an object named 'fields' that has the form fields
        } catch (com.fasterxml.jackson.core.JsonProcessingException ex) {
            return "";
        }

        // Get a List of names
        List<String> nameList = new ArrayList<String>(mapOfJson.keySet());

        // Build the new typed Json string
        StringBuilder recordBuilder = new StringBuilder();

        // Storage for decoded type and name
        String [] newType = new String [2];

        // Add field names to String without type prefix. Set values either with or without quotes
        mapOfJson.forEach((name,value) -> {
            typeFormatByName(name,value, newType);
            String newName = newType[0];

            if (recordBuilder.length() == 0) {// First one
                recordBuilder.append("\"" + newName + "\"" + ":" +  newType[1]);
            } else { // Append another field
                recordBuilder.append("," + "\"" + newName + "\"" + ":" +  newType[1]);
            }
        });

        return recordBuilder.toString();
    }

    /**
     *  Send the Airtable record formatted in a Json String
     * @param api_id Airtable api handle for the account
     * @param api_key Airtable api key
     * @param baseTable Name of base table we want to write to
     * @param record A Map<String,String> representing Airtable field names, values and types, sent from a Html Form.
     *               Names are prefixed by a type indication, i.e., n_age,  s_name, for a number age and string name.
     *               The form values are scalars so we only need string or number type info. The type is needed for
     *               proper Airtable field Json values representation. Json strings are surrounded by quotes, numbers are not.
     *  Return int - Either a http return code from Airtable or -1 if something else went wrong
     */
    public static int addFormXXToAirtable(String api_id, String api_key, String baseTable, Map<String,String> record) {

        // Convert name/value pairs to Json string

        // Get a List of names
        List<String> nameList = new ArrayList<String>(record.keySet());

        // Build the Json string
        StringBuilder recordBuilder = new StringBuilder();

        // Storage for decoded type and name
        String [] newType = new String [2];

        record.forEach((name,value) -> {
            typeFormatByName(name,value, newType);
            String newName = newType[0];

            if (recordBuilder.length() == 0) {// First one
                recordBuilder.append("\"" + newName + "\"" + ":" +  newType[1]);
            } else { // Append another field
                recordBuilder.append("," + "\"" + newName + "\"" + ":" +  newType[1]);
            }
        });

        // Complete the Airtable Json
        String json = "{\"records\" : [ { \"fields\" : { " + recordBuilder.toString() + " } } ] }";


        /* client for http calls */
        try (CloseableHttpClient httpclient = HttpClients.custom().setRedirectStrategy(new DefaultRedirectStrategy()).build()) {

            /* http request to Airtable  */
            HttpPost httpPost = new HttpPost(URI.create("https://api.airtable.com/v0/".concat(api_id).concat("/").concat(baseTable)));
            httpPost.addHeader("Authorization", "Bearer ".concat(api_key));
            httpPost.addHeader("Content-Type", "application/json");
            httpPost.setEntity(new StringEntity(json, ContentType.APPLICATION_JSON));

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
     * @param jsonFormat A JSON String with type encoded field names and String field values for Airtable records
     * @return Either a http return code from Airtable or -1 if something else went wrong
    */
    public static int addFormXXJsonToAirtable(String api_id, String api_key, String baseTable, String jsonFormat) {

        // Transform json String to decode the typed fields
        String record = decodeTypeEncodedJson(jsonFormat);

        if (record == "") return -1;

        // Complete the Airtable Json
        String jsonTyped = "{\"records\" : [ { \"fields\" : { " + record + " } } ] }";


        /* client for http calls */
        try (CloseableHttpClient httpclient = HttpClients.custom().setRedirectStrategy(new DefaultRedirectStrategy()).build()) {

            /* http request to Airtable  */
            HttpPost httpPost = new HttpPost(URI.create("https://api.airtable.com/v0/".concat(api_id).concat("/").concat(baseTable)));
            httpPost.addHeader("Authorization", "Bearer ".concat(api_key));
            httpPost.addHeader("Content-Type", "application/json");
            httpPost.setEntity(new StringEntity(jsonTyped, ContentType.APPLICATION_JSON));

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
