package com.warpedcitadel.filemanagementservice.filemanagement.dto;

import org.springframework.data.domain.Slice;

import java.util.List;

public record SlicedResponse<T>(
        List<T> content,
        boolean first,
        boolean last,
        int number,
        int size,
        boolean hasNext
) {
    public SlicedResponse(Slice<T> slice) {
        this(
                slice.getContent(),
                slice.isFirst(),
                slice.isLast(),
                slice.getNumber(),
                slice.getSize(),
                slice.hasNext()
        );
    }
}
