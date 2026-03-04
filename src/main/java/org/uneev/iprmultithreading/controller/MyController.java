package org.uneev.iprmultithreading.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.WebAsyncTask;
import org.uneev.iprmultithreading.service.FacePhotoExtractorService;
import org.uneev.iprmultithreading.service.FacePhotoSearchService;
import org.uneev.iprmultithreading.service.FingerprintExtractorService;
import org.uneev.iprmultithreading.dto.SearchRequestDto;
import org.uneev.iprmultithreading.service.FingerprintSearchService;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

@RestController
@RequiredArgsConstructor
@Slf4j
class MyController {

    private final FingerprintExtractorService fingerprintExtractorService;
    private final FacePhotoExtractorService facePhotoExtractorService;

    private final FingerprintSearchService fingerprintSearchService;
    private final FacePhotoSearchService facePhotoSearchService;

    @PostMapping("/sequential")
    public Integer simple(@RequestBody SearchRequestDto request) {
        log.info("Получен запрос на последовательную обработку");

        long start = System.currentTimeMillis();

        log.debug("Отправка запроса в экстрактор отпечатков пальцев");
        String fingerprintTemplate = fingerprintExtractorService.extract(request.fingerprints());
        log.debug("Сгенерированный шаблон отпечатков пальцев: {}", fingerprintTemplate);

        log.debug("Отправка запроса на поиск по шаблону отпечатков пальцев");
        Integer fingerprintScore = fingerprintSearchService.search(fingerprintTemplate);
        log.debug("Скор поиска по отпечаткам пальцев: {}", fingerprintScore);

        log.debug("Отправка запроса в экстрактор набора фото лица");
        String facePhotoTemplate = facePhotoExtractorService.extract(request.facePhotos());
        log.debug("Сгенерированный шаблон набора фото лица: {}", facePhotoTemplate);

        log.debug("Отправка запроса на поиск по шаблону набора фото лица");
        Integer facePhotoScore = facePhotoSearchService.search(facePhotoTemplate);
        log.debug("Скор поиска по набору фото лица: {}", facePhotoScore);

        log.debug("Время выполнения: {}", System.currentTimeMillis() - start);

        return (fingerprintScore + facePhotoScore) / 2;
    }

    @PostMapping("/async")
    public Integer async(@RequestBody SearchRequestDto request) throws ExecutionException, InterruptedException {
        log.info("Получен запрос на последовательную обработку");

        long start = System.currentTimeMillis();

        CompletableFuture<Integer> fingerprintScoreFuture = CompletableFuture.supplyAsync(() -> {
                    log.debug("Отправка запроса в экстрактор отпечатков пальцев");
                    String fingerprintTemplate = fingerprintExtractorService.extract(request.fingerprints());
                    log.debug("Сгенерированный шаблон отпечатков пальцев: {}", fingerprintTemplate);
                    return fingerprintTemplate;
                })
                .thenApply(fingerprintTemplate -> {
                    log.debug("Отправка запроса на поиск по шаблону отпечатков пальцев");
                    throwException();
                    Integer fingerprintScore = fingerprintSearchService.search(fingerprintTemplate);
                    log.debug("Скор поиска по отпечаткам пальцев: {}", fingerprintScore);
                    return fingerprintScore;
                })
                .exceptionally(throwable -> {
                    log.warn("Ошибочка: {}", throwable);
                    return 1000;
                });

        CompletableFuture<Integer> facePhotoScoreFuture = CompletableFuture.supplyAsync(() -> {
                    log.debug("Отправка запроса в экстрактор набора фото лица");
                    String facePhotoTemplate = facePhotoExtractorService.extract(request.facePhotos());
                    log.debug("Сгенерированный шаблон набора фото лица: {}", facePhotoTemplate);
                    return facePhotoTemplate;
                })
                .thenApply(facePhotoTemplate -> {
                    log.debug("Отправка запроса на поиск по шаблону набора фото лица");
                    Integer facePhotoScore = facePhotoSearchService.search(facePhotoTemplate);
                    log.debug("Скор поиска по набору фото лица: {}", facePhotoScore);
                    return facePhotoScore;
                });

        CompletableFuture<Integer> averageScoreFuture = fingerprintScoreFuture.thenCombine(
                facePhotoScoreFuture,
                (fingerprintScore, facePhotoScore) -> (fingerprintScore + facePhotoScore) / 2
        );

        averageScoreFuture.thenRun(() -> log.debug("Время выполнения: {}", System.currentTimeMillis() - start));

        return averageScoreFuture.get();
    }

    @GetMapping("/return-completable-future")
    public CompletableFuture<String> completableFuture() {
        return CompletableFuture.supplyAsync(() -> "Hello World!");
    }

    @GetMapping("/return-future")
    public Future<String> future() {
        return new FutureTask<>(() -> {
            try {
                Thread.sleep(10_000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return "Hello World!";
        });
    }

    @GetMapping("/return-callable")
    public Callable<String> callable() {
        return () -> "Hello World";
    }

    @GetMapping("/long-time")
    public void lockThread() {
        log.info("Получен запрос на долгое выполнение с блокировкой потока");
        try {
            Thread.sleep(10_000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/long-time-completable-future")
    public CompletableFuture<Void> lockThreadCompletableFuture() {
        log.info("Получен запрос на долгое выполнение без блокировки потока (completable future)");
        return CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(10_000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @GetMapping("/long-time-web-async-task")
    public WebAsyncTask<String> lockWebAsyncTask() {
        return new  WebAsyncTask<>(10_000, () -> {
            try {
                Thread.sleep(5_000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return "Hello World";
        });
    }

    private void throwException() {
        throw new RuntimeException("QWERTY123");
    }
}
