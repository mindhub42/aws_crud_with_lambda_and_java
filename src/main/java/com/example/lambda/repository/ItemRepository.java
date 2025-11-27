package com.example.lambda.repository;


import com.example.lambda.model.Item;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.*;
import java.util.stream.Collectors;

public class ItemRepository {

    private final DynamoDbClient dynamo;
    private final String table;

    public ItemRepository(String tableName) {
        this.table = tableName;
        this.dynamo = DynamoDbClient.builder()
                .region(Region.of(System.getenv("AWS_REGION")))
                .build();
    }

    /* -----------------------------
     * CREATE or UPDATE (Upsert)
     * ----------------------------- */
    public void save(Item item) {
        Map<String, AttributeValue> data = new HashMap<>();
        data.put("id", AttributeValue.builder().s(item.id()).build());
        data.put("name", AttributeValue.builder().s(item.name()).build());
        data.put("description", AttributeValue.builder().s(item.description()).build());

        PutItemRequest request = PutItemRequest.builder()
                .tableName(table)
                .item(data)
                .build();

        dynamo.putItem(request);
    }

    /* -----------------------------
     * READ single item
     * ----------------------------- */
    public Optional<Item> get(String id) {
        GetItemRequest request = GetItemRequest.builder()
                .tableName(table)
                .key(Map.of("id", AttributeValue.builder().s(id).build()))
                .build();

        Map<String, AttributeValue> item = dynamo.getItem(request).item();

        if (item == null || item.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(fromDynamo(item));
    }

    /* -----------------------------
     * LIST all items (simple Scan)
     * ----------------------------- */
    public List<Item> list() {
        ScanRequest request = ScanRequest.builder()
                .tableName(table)
                .build();

        ScanResponse response = dynamo.scan(request);

        return response.items().stream()
                .map(this::fromDynamo)
                .collect(Collectors.toList());
    }

    /* -----------------------------
     * DELETE item
     * ----------------------------- */
    public void delete(String id) {
        DeleteItemRequest request = DeleteItemRequest.builder()
                .tableName(table)
                .key(Map.of("id", AttributeValue.builder().s(id).build()))
                .build();

        dynamo.deleteItem(request);
    }

    /* -----------------------------
     * UPDATE item
     * (only name + description here)
     * ----------------------------- */
    public void update(String id, String name, String description) {
        Map<String, AttributeValueUpdate> updates = new HashMap<>();

        updates.put("name", AttributeValueUpdate.builder()
                .value(AttributeValue.builder().s(name).build())
                .action(AttributeAction.PUT)
                .build());

        updates.put("description", AttributeValueUpdate.builder()
                .value(AttributeValue.builder().s(description).build())
                .action(AttributeAction.PUT)
                .build());

        UpdateItemRequest request = UpdateItemRequest.builder()
                .tableName(table)
                .key(Map.of("id", AttributeValue.builder().s(id).build()))
                .attributeUpdates(updates)
                .build();

        dynamo.updateItem(request);
    }

    /* -----------------------------
     * Helper: convert DynamoDB map → Item object
     * ----------------------------- */
    private Item fromDynamo(Map<String, AttributeValue> data) {
        return new Item(
                data.get("id").s(),
                data.get("name").s(),
                data.get("description").s()
        );
    }
}

