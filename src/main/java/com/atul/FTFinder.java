package com.atul;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class FTFinder {
    public static final String STARTTIME_KEY = "starttime";
    public static final String DAYOFWEEKSTR_KEY = "dayofweekstr";
    public static final String ENDTIME_KEY = "endtime";

    // Endpoint to fetch the food truck data.
    public static final String ENDPOINT = "https://data.sfgov.org/resource/jjew-r69b.json?";
    public static final String ORDER = "&$order=applicant";

    public static final String FOOTER = "********** End of list **************";
    public static final String PAGE_HEADER = "    NAME                                                                      ADDRESS";
    public static final String PAGE_FOOTER = "\nPress x to exit, Enter for next page :";
    public static final String NAME_OF_TRUCK = "applicant";
    public static final String ADDRESS = "location";

    public static void main(String[] args) {
        FTFinder ftf = new FTFinder();

        // Get the current time in SF time zone (PST)
        String pacific = "America/Los_Angeles";
        Instant now = Instant.now();
        ZoneId zoneId = ZoneId.of( pacific );
        ZonedDateTime zdt = now.atZone(zoneId);

        DayOfWeek dow = zdt.getDayOfWeek();
        String day = dow.getDisplayName(TextStyle.FULL, Locale.US);

        //Environment variable if testing for a day other than today.
        String day_test = System.getProperty("day");

        //STEP 1: Fetch the list of food trucks for a particular day
        JSONArray truck_list;
        if(day_test != null)
            truck_list = ftf.fetchData(day_test);   //If day_test is incorrect, it will result in an empty list
        else
            truck_list = ftf.fetchData(day);

        //STEP 2: Filter the data per the hours of operation and hour requested.
        //Env variable for hour of day, if testing for hour other than now.
        String hours = System.getProperty("hours");
        List<String> lst;
        if(hours !=null ){
            try{
                int hh = Integer.parseInt(hours);
                ZonedDateTime zdt_test = zdt.withHour(hh);
                lst = ftf.filter(truck_list, zdt_test);
            } catch(NumberFormatException nme){
                System.out.println("Incorrect value of hours passed in command line, fetching data for now");
                lst = ftf.filter(truck_list, zdt);
            }
        } else lst = ftf.filter(truck_list, zdt);

        //STEP 3: Display the resulting list in a paginated output on the terminal.
        ftf.render(lst);
    }

    private JSONArray fetchData(String day) {
        JSONArray truck_list = null;
        try {
            URL url = new URL(ENDPOINT + DAYOFWEEKSTR_KEY + "=" + day + ORDER);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("charset", "utf-8");
            connection.connect();
            InputStream inStream = connection.getInputStream();
            String json = new Scanner(inStream, "UTF-8").useDelimiter("\\Z").next();
            truck_list = new JSONArray(json);
            connection.disconnect();
        } catch (JSONException e) {
            System.out.println("Error on parsing data "+ e.toString());
        } catch (IOException ex) {
            System.out.println("Error getting data from endpoint "+ ex.toString());
        }
        return truck_list;
    }

    private List<String> filter(JSONArray truck_list, ZonedDateTime zdt) {
        List<String> lst = new ArrayList<>();
        if(truck_list==null)return lst;

        //Iterate thru today's available trucks and add to the lst, the trucks open now.
        for(int i=0; i<truck_list.length(); i++) {
            try {
                JSONObject truck = truck_list.getJSONObject(i);

                String start = getHH(truck, STARTTIME_KEY);
                if (start == null) continue;

                String end = getHH(truck, ENDTIME_KEY);
                if (end == null) continue;

                String result = LocalTime.parse(start, DateTimeFormatter.ofPattern("hha", Locale.US)).format(DateTimeFormatter.ofPattern("HH"));
                ZonedDateTime zdt_start = zdt.withHour(Integer.parseInt(result)).withMinute(0).withSecond(0);

                result = LocalTime.parse(end, DateTimeFormatter.ofPattern("hha", Locale.US)).format(DateTimeFormatter.ofPattern("HH"));
                ZonedDateTime zdt_end = zdt.withHour(Integer.parseInt(result)).withMinute(0).withSecond(0);

                if ( zdt.isBefore(zdt_end) && zdt.isAfter(zdt_start) ) {
                    String vendor = truck.get(NAME_OF_TRUCK).toString();
                    int vlen = vendor.length();
                    for(int j=0; j<70-vlen; j++)vendor+=" ";    // String with name and address with proper indentation.
                    vendor += truck.get(ADDRESS);
                    lst.add(vendor);
                }
            } catch (NumberFormatException  | JSONException e) {
                System.out.println("Incorrect payload, continuing...");
                continue;
            }
        }
        return lst;
    }

    private String getHH(JSONObject truck, String timeKey) {
        String boundary = truck.get(timeKey).toString();
        if (boundary.length() < 3 || boundary.length() > 4) {   //It can either be something like 8AM (3), or 11PM (4)
            System.out.println("Incorrect payload for: " + truck.get(NAME_OF_TRUCK) + ", continuing...");
            return null;
        }
        if(boundary.length()==3)boundary="0"+boundary; //append a 0 to make it hh format
        return boundary;
    }

    private void render(List<String> list) {
        int len = list.size();
        if(len < 0) {
            System.out.println(FOOTER);
            return;
        }
        int index=0;
        while(index<len) {
            System.out.println(PAGE_HEADER);
            for(int i=0; i<10 && index < len; i++) {
                System.out.println((index+1)+" : "+list.get(index++));
            }
            if(index>=len)
                break;
            Scanner scanner = new Scanner(System.in);
            System.out.println(PAGE_FOOTER);
            String inp = scanner.nextLine();  // Read user input
            if(inp.equals("x"))
                return;
            else continue;
        }
        System.out.println(FOOTER);
    }
}
