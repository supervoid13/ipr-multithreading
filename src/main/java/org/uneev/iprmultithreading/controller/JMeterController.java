package org.uneev.iprmultithreading.controller;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.uneev.iprmultithreading.dto.SearchRequestDto;
import org.uneev.iprmultithreading.service.FacePhotoExtractorService;
import org.uneev.iprmultithreading.service.FacePhotoSearchService;
import org.uneev.iprmultithreading.service.FingerprintExtractorService;
import org.uneev.iprmultithreading.service.FingerprintSearchService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/jmeter")
class JMeterController {

    private final FingerprintExtractorService fingerprintExtractorService;
    private final FacePhotoExtractorService facePhotoExtractorService;

    private final FingerprintSearchService fingerprintSearchService;
    private final FacePhotoSearchService facePhotoSearchService;

    private static final ExecutorService threadPool;

    static {
        threadPool = Executors.newFixedThreadPool(1000);
    }

    @PostMapping("/sequential")
    public Double sequential(@RequestBody SearchRequestDto request) {
        log.info("Получен запрос на последовательную обработку");

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

        return calculateAvg(fingerprintScore, facePhotoScore);
    }

    @PostMapping("/async-tomcat-blocking")
    public Double asyncTomcatBlocking(@RequestBody SearchRequestDto request) throws ExecutionException, InterruptedException {
        log.info("Получен запрос на асинхронную обработку с блокировкой потока Tomcat");

        CompletableFuture<Integer> fingerprintScoreFuture = CompletableFuture.supplyAsync(() -> {
                    log.debug("Отправка запроса в экстрактор отпечатков пальцев");
                    String fingerprintTemplate = fingerprintExtractorService.extract(request.fingerprints());
                    log.debug("Сгенерированный шаблон отпечатков пальцев: {}", fingerprintTemplate);
                    return fingerprintTemplate;
                })
                .thenApply(fingerprintTemplate -> {
                    log.debug("Отправка запроса на поиск по шаблону отпечатков пальцев");
                    Integer fingerprintScore = fingerprintSearchService.search(fingerprintTemplate);
                    log.debug("Скор поиска по отпечаткам пальцев: {}", fingerprintScore);
                    return fingerprintScore;
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

        return fingerprintScoreFuture.thenCombine(facePhotoScoreFuture, this::calculateAvg).get();
    }

    @PostMapping("/async-tomcat-non-blocking")
    public CompletableFuture<Double> asyncTomcatNonBlocking(@RequestBody SearchRequestDto request) {
        log.info("Получен запрос на асинхронную обработку без блокировки потока Tomcat");

        CompletableFuture<Integer> fingerprintScoreFuture = CompletableFuture.supplyAsync(() -> {
                            log.debug("Отправка запроса в экстрактор отпечатков пальцев");
                            String fingerprintTemplate = fingerprintExtractorService.extract(request.fingerprints());
                            log.debug("Сгенерированный шаблон отпечатков пальцев: {}", fingerprintTemplate);
                            return fingerprintTemplate;
                        },
                        threadPool
                )
                .thenApply(fingerprintTemplate -> {
                    log.debug("Отправка запроса на поиск по шаблону отпечатков пальцев");
                    Integer fingerprintScore = fingerprintSearchService.search(fingerprintTemplate);
                    log.debug("Скор поиска по отпечаткам пальцев: {}", fingerprintScore);
                    return fingerprintScore;
                });

        CompletableFuture<Integer> facePhotoScoreFuture = CompletableFuture.supplyAsync(() -> {
                            log.debug("Отправка запроса в экстрактор набора фото лица");
                            String facePhotoTemplate = facePhotoExtractorService.extract(request.facePhotos());
                            log.debug("Сгенерированный шаблон набора фото лица: {}", facePhotoTemplate);
                            return facePhotoTemplate;
                        },
                        threadPool
                )
                .thenApply(facePhotoTemplate -> {
                    log.debug("Отправка запроса на поиск по шаблону набора фото лица");
                    Integer facePhotoScore = facePhotoSearchService.search(facePhotoTemplate);
                    log.debug("Скор поиска по набору фото лица: {}", facePhotoScore);
                    return facePhotoScore;
                });

        return fingerprintScoreFuture.thenCombine(facePhotoScoreFuture, this::calculateAvg);
    }

    private double calculateAvg(int... scores) {
        return Arrays.stream(scores)
                .average()
                .orElse(Double.NaN);
    }
}