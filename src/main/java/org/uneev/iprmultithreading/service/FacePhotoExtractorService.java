package org.uneev.iprmultithreading.service;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

import java.util.Collection;

@HttpExchange("/face-photo-extractor")
public interface FacePhotoExtractorService {

    @PostExchange("/extract-face-photos")
    String extract(@RequestBody Collection<String> facePhotos);
}
