package com.example.lambda.controller;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.example.lambda.model.Item;
import com.example.lambda.repository.ItemRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ApiHandler implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

    private final ObjectMapper mapper = new ObjectMapper();
    private final ItemRepository repo;

    public ApiHandler() {
        String table = System.getenv("TABLE_NAME");
        this.repo = new ItemRepository(table);
    }

    @Override
    public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent event,
                                                      Context context) {
        context.getLogger().log("Event: " + event);
        String method = event.getRequestContext().getHttp().getMethod();
        String path = event.getRequestContext().getHttp().getPath();

        context.getLogger().log("Method: " + method + " Path: " + path);

        try {
            // ----- ROUTING -----
            if (method.equalsIgnoreCase("GET") && path.equals("/items")) {
                return handleList();
            }

            if (method.equalsIgnoreCase("POST") && path.equals("/items")) {
                return handleCreate(event.getBody());
            }

            if (method.equalsIgnoreCase("GET") && path.startsWith("/items/")) {
                String id = extractId(path);
                return handleGet(id);
            }

            if (method.equalsIgnoreCase("PUT") && path.startsWith("/items/")) {
                String id = extractId(path);
                return handleUpdate(id, event.getBody());
            }

            if (method.equalsIgnoreCase("DELETE") && path.startsWith("/items/")) {
                String id = extractId(path);
                return handleDelete(id);
            }

            return respond(404, "{\"message\": \"Not found\"}");

        } catch (Exception e) {
            context.getLogger().log("ERROR: " + e.getMessage());
            return respond(500, "{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    // -----------------------------
    // HANDLERS
    // -----------------------------

    private APIGatewayV2HTTPResponse handleList() throws Exception {
        List<Item> items = repo.list();
        return respondJson(200, items);
    }

    private APIGatewayV2HTTPResponse handleGet(String id) throws Exception {
        Optional<Item> item = repo.get(id);
        if (item.isEmpty()) {
            return respond(404, "{\"message\": \"Item not found\"}");
        }
        return respondJson(200, item.get());
    }

    private APIGatewayV2HTTPResponse handleCreate(String jsonBody) throws Exception {
        Item item = mapper.readValue(jsonBody, Item.class);

        if (item.id() == null || item.id().isBlank()) {
            return respond(400, "{\"message\": \"id is required\"}");
        }

        repo.save(item);

        return respondJson(201, item);
    }

    private APIGatewayV2HTTPResponse handleUpdate(String id, String jsonBody) throws Exception {
        Item incoming = mapper.readValue(jsonBody, Item.class);

        if (incoming.name() == null && incoming.description() == null) {
            return respond(403, "Nothing to update");
        }

        Optional<Item> existing = repo.get(id);
        if (existing.isEmpty()) {
            return respond(404, "{\"message\": \"Item not found\"}");
        }

        Item updated = existing.get();

        Item newItem = new Item(
                updated.id(),
                incoming.name() == null ? updated.name() : incoming.name(),
                incoming.description() == null ? updated.description() : incoming.description()
        );

        repo.save(newItem);

        return respondJson(200, updated);
    }

    private APIGatewayV2HTTPResponse handleDelete(String id) {
        repo.delete(id);
        return respond(200, "{\"deleted\": true}");
    }


    // -----------------------------
    // HELPERS
    // -----------------------------

    private String extractId(String path) {
        return path.substring(path.lastIndexOf("/") + 1);
    }

    private APIGatewayV2HTTPResponse respond(int status, String body) {
        return APIGatewayV2HTTPResponse.builder()
                .withStatusCode(status)
                .withHeaders(Map.of(
                        "Content-Type", "application/json",
                        "Access-Control-Allow-Origin", "*"
                ))
                .withBody(body)
                .build();
    }

    private APIGatewayV2HTTPResponse respondJson(int status, Object obj) throws Exception {
        String json = mapper.writeValueAsString(obj);
        return respond(status, json);
    }
}

