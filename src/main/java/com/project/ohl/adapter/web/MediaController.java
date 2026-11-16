package com.project.ohl.adapter.web;

import com.project.ohl.application.model.MediaDownload;
import com.project.ohl.application.port.in.MediaService;
import java.io.IOException;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/media/{type}")
public class MediaController {
    private final MediaService media;

    public MediaController(MediaService media) {
        this.media = media;
    }

    @GetMapping(produces = "application/json")
    public ResponseEntity<MediaDownload> downloadMedia(@PathVariable String type, @RequestParam String mediaKey) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(media.download(type, mediaKey));
    }

    @PutMapping(consumes = "multipart/form-data")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> uploadMedia(@PathVariable String type, @RequestParam String mediaKey,
            @RequestPart("file") MultipartFile file) throws IOException {
        try (var content = file.getInputStream()) {
            media.upload(type, mediaKey, file.getContentType(), file.getSize(), content);
        }
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteMedia(@PathVariable String type, @RequestParam String mediaKey) {
        media.delete(type, mediaKey);
        return ResponseEntity.noContent().build();
    }
}
