package org.uneev.iprmultithreading.service;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

import java.util.Collection;

@HttpExchange("/face-photo-searcher")
public interface FacePhotoSearchService {

    @PostExchange("/search-by-face-photo-template")
    Integer search(@RequestBody String facePhotoTemplate);
}
