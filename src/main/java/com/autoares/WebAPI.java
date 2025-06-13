package com.autoares;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class WebAPI {

    public WebAPI(Controller controller, boolean status) {
        this.controller = controller;
        webgetter(status);
    }
    private Controller controller;
    private final HttpClient CLIENT = HttpClient.newHttpClient();

    private final Thread GETTER = new Thread(() -> {

        while (true) {
            for (String variable : DATA_Web.VARIABLES) {
                String url = DATA_Web.BASE_URL + variable;

                try {
                    HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
                    HttpResponse<String> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
                    DATA_Web.table.put(variable, response.body().strip());

                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }
            try {
                controller.process();
            } catch (NumberFormatException e) {
                System.err.println(e.getMessage());
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                System.err.println(e.getMessage());
            }
        }
    });
    public void postVariable(String variable, String value) {
        String url = DATA_Web.BASE_URL + variable + "&value=" + value;

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .POST(HttpRequest.BodyPublishers.noBody()) // pas de corps, les données sont dans l'URL
                    .build();

            HttpResponse<String> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("POST: " + variable + " = " + value + " | Response: " + response.body());

        } catch (Exception e) {
            System.err.println("POST error for " + variable + ": " + e.getMessage());
        }
    }
    public void webgetter(boolean status) {
        if (status) {
            GETTER.start();
        } else {
            GETTER.interrupt();
            controller.process();
        }
    }
}
