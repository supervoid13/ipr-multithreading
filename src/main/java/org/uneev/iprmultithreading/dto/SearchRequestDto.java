package org.uneev.iprmultithreading.dto;

import java.util.Collection;

public record SearchRequestDto(
        Collection<String> fingerprints,
        Collection<String> facePhotos
) {
}
