package com.example.amazons3.controller;

import com.example.amazons3.payload.Attachment;
import com.example.amazons3.service.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.io.IOException;


@RestController
@RequestMapping("/api/file")
public class StorageController {

    private final StorageService service;

    @Autowired
    public StorageController(StorageService service) {
        this.service = service;
    }

    @PostMapping("/upload")
    public HttpEntity<?> uploadFile(
            MultipartHttpServletRequest request
    ) {
        String message = service.uploadFile(request);
        return ResponseEntity.ok(message);
    }

    @GetMapping("/download")
    public HttpEntity<?> downloadFile(
            @RequestParam(name = "name") String name
    ) throws IOException {
        return service.downloadFile(name);
    }

    @DeleteMapping("/delete")
    public HttpEntity<?> deleteFile(
            @RequestParam(name = "name") String name
    ) {
        String message = service.deleteFile(name);
        return ResponseEntity.ok(message);
    }

    @GetMapping("/getValue")
    public String getValueFromFile(@RequestParam String name) throws IOException {
        return service.getValueFromFile(name);
    }

}
