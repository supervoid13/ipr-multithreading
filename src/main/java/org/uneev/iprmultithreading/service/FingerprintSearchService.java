package org.uneev.iprmultithreading.service;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

import java.util.Collection;

@HttpExchange("/fingerprint-searcher")
public interface FingerprintSearchService {

    @PostExchange("/search-by-fingerprint-template")
    Integer search(@RequestBody String fingerprintTemplate);
}
