package com.example.demo;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class HttpClientApp {
    private static final String API_URL = "http://localhost/";
    private static final AtomicInteger NEXT_ID = new AtomicInteger(1);

    public static void main(String[] args) {
        if (args.length < 3) {
            System.out.println("Usage: java HttpClientApp <numberOfRequests> <threadPoolSize> <requestFlag>" +
                    "\n-h for hello world" +
                    "\n-i for insert emails" +
                    "\n-g for get users" +
                    "\n-u for update the first numberOfRequests emails to timestamp emails" +
                    "\n-d delete the first numberOfRequests emails");
            return;
        }
        int numberOfRequests = Integer.parseInt(args[0]);
        int threadPoolSize = Integer.parseInt(args[1]);
        String requestFlag = args[2];

        ExecutorService executor =
                Executors.newFixedThreadPool(threadPoolSize);
        Runnable requestType;

        switch (requestFlag) {
            case "-h":
                requestType = () -> helloRequest();
                break;
            case "-i":
                requestType = ()-> insertRequest();
                break;
            case "-g":
                requestType = ()-> getRequest();
                break;
            case "-u":
                //modifys the first n requests to a timestamp email
                requestType = ()-> updateRequest();
                break;
            case "-d":
                //deletes the first n requests to a timestamp email
                requestType = ()-> deleteRequest();
                break;
            default:
                System.out.println("Unknown request flag: " + requestFlag);
                return;
        }
        for (int i = 0; i < numberOfRequests; i++) {
            executor.submit(requestType);
        }
        executor.shutdown();
    }

    private static void requestWithPayload(HttpURLConnection conn, String payload) throws IOException {
        try(OutputStream os = conn.getOutputStream()) {
            byte[] input = payload.getBytes("utf-8");
            os.write(input, 0, input.length);
        }
        getAndPrintResult(conn.getInputStream());
    }

    private static void getAndPrintResult(InputStream connIn) throws IOException {
        BufferedReader in = new BufferedReader(new
                InputStreamReader(connIn));
        String inputLine;
        StringBuilder response = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }

        in.close();
        System.out.println("Response: " + response.toString());
    }

    private static void helloRequest() {
        try {
            URL url = new URL(API_URL + "hello");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            getAndPrintResult(conn.getInputStream());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void getRequest() {
        try {
            URL url = new URL(API_URL + "users");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            getAndPrintResult(conn.getInputStream());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void insertRequest() {
        try {
            URL url = new URL(API_URL + "user");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            final int id = HttpClientApp.NEXT_ID.getAndIncrement();

            conn.setRequestProperty("Content-Type", "application/json");

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
            Date dateTime = new Date();
            String formattedDate = sdf.format(dateTime);//to make the emails unique
            String jsonInputString = String.format("{\"email\": \"%s_%d@gmail.com\"}",formattedDate, id);
            requestWithPayload(conn, jsonInputString);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void updateRequest() {
        try {
            URL url = new URL(API_URL + "user");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("PUT");
            conn.setDoOutput(true);
            final int id = HttpClientApp.NEXT_ID.getAndIncrement();

            conn.setRequestProperty("Content-Type", "application/json");

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
            Date dateTime = new Date();
            String formattedDate = sdf.format(dateTime);//to make the emails unique
            String jsonInputString = String.format("{\"id\": \"%d\",\"email\": \"%s_%d@gmail.com\"}",id,formattedDate, id);

            requestWithPayload(conn, jsonInputString);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void deleteRequest() {
        try {
            URL url = new URL(API_URL + "user");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("DELETE");
            conn.setDoOutput(true);
            final int id = HttpClientApp.NEXT_ID.getAndIncrement();

            conn.setRequestProperty("Content-Type", "application/json");
            String jsonInputString = String.format("{\"id\": \"%d\"}",id);

            requestWithPayload(conn, jsonInputString);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
