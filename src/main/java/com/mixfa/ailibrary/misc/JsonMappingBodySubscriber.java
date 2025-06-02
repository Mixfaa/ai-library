package com.mixfa.ailibrary.misc;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Flow;

class JsonMappingBodySubscriber<T> implements HttpResponse.BodySubscriber<T> {

    private final TypeReference<T> targetType;
    private final ObjectMapper objectMapper;
    private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    private final CompletableFuture<T> future = new CompletableFuture<>();
    private Flow.Subscription subscription;

    public JsonMappingBodySubscriber(TypeReference<T> targetType, ObjectMapper objectMapper) {
        this.targetType = targetType;
        this.objectMapper = objectMapper;
    }

    @Override
    public void onSubscribe(Flow.Subscription subscription) {
        this.subscription = subscription;
        // Запитуємо необмежену кількість даних.
        subscription.request(Long.MAX_VALUE);
    }

    @Override
    public void onNext(List<ByteBuffer> item) {
        // Удосконалена обробка ByteBuffer:
        // Замість byteBuffer.array() використовуємо byteBuffer.get() для читання байтів,
        // що є більш надійним для різних типів ByteBuffer (прямих або непрямих).
        item.forEach(byteBuffer -> {
            int remaining = byteBuffer.remaining();
            if (remaining > 0) {
                byte[] bytes = new byte[remaining];
                byteBuffer.get(bytes); // Читаємо байти з ByteBuffer
                try {
                    outputStream.write(bytes); // Записуємо їх у ByteArrayOutputStream
                } catch (IOException e) {
                    future.completeExceptionally(e);
                    subscription.cancel(); // Скасовуємо підписку при помилці
                }
            }
        });
        subscription.request(1); // Запитуємо більше даних
    }

    @Override
    public void onError(Throwable throwable) {
        future.completeExceptionally(throwable);
    }

    @Override
    public void onComplete() {
        try {
            // Коли всі дані отримано, десеріалізуємо байти з ByteArrayOutputStream
            // за допомогою ObjectMapper у об'єкт потрібного типу.
            T result = objectMapper.readValue(outputStream.toByteArray(), targetType);
            future.complete(result);
        } catch (IOException e) {
            future.completeExceptionally(e);
        }
    }

    @Override
    public CompletionStage<T> getBody() {
        return future;
    }
}
