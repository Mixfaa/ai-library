package com.mixfa.ailibrary.model.filestorage;

import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

public record FileResponse(
        String filename,
        StreamingResponseBody streamingResponse
) {
}
