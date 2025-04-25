package com.mixfa.ailibrary.controller;

import com.mixfa.ailibrary.model.FileData;
import com.mixfa.ailibrary.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@RestController
@RequestMapping("/file-storage")
@RequiredArgsConstructor
public class FileStorageContoller {
    private final FileStorageService fileStorageService;

    @GetMapping("/file/{id}")
    public StreamingResponseBody file(@PathVariable String id) throws Exception {
        return fileStorageService.read(id).streamingResponse();
    }

    public static String makeFileStaticURL(FileData fileData) {
        return "/file-storage/file/" + fileData.id();
    }
}
