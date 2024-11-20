package com.br.shortener;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Main implements RequestHandler<Map<String, Object>, Map<String, String>> {

    private final static String BUCKET_NAME = "url-shortener-study2-danilo";

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final S3Client s3Client = S3Client.builder().build();

    @Override
    public Map<String, String> handleRequest(Map<String, Object> input, Context context) {
        String body = input.get("body").toString();
        Map<String, String> bodyMap;

        /**
         * Exemplo de requisição
         *
         * input {
         *    body: "{\"originalUrl\":\"https://rocketseat.com.br\"}"
         *    headers: {......}
         * }
         */
        try {
            bodyMap = objectMapper.readValue(body, Map.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error parsing JSON body: " +e.getMessage(), e);
        }
        String originalUrl = bodyMap.get("originalUrl");
        String expirationTime = bodyMap.get("expirationTime");
        String shorUrlCode = UUID.randomUUID().toString().substring(0,8);
        long expirationTimeInSeconds = Long.parseLong(expirationTime);
        UrlData urlData = new UrlData(originalUrl, expirationTimeInSeconds);
        try {
            String urlDataJson = objectMapper.writeValueAsString(urlData);
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(BUCKET_NAME)
                    .key(shorUrlCode + ".json")
                    .build();
            s3Client.putObject(request, RequestBody.fromString(urlDataJson));
        } catch (Exception e) {
            throw new RuntimeException("Error saving URL Data to Json:"+ e.getMessage(), e);
        }

        Map<String, String> response = new HashMap<>();
        response.put("code", shorUrlCode);
        return response;
    }
}
