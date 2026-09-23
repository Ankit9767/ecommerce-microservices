package com.example.product_service.storage;

import com.example.product_service.exception.ProductImageStorageException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Component
public class LocalProductImageStorage implements ProductImageStorage {

    private final Path rootLocation;

    public LocalProductImageStorage(
            @Value("${app.storage.product-images-dir:./storage/product-images}")
            String storageDirectory) {

        this.rootLocation = Path.of(storageDirectory)
                .toAbsolutePath()
                .normalize();

        try {

            Files.createDirectories(this.rootLocation);

        } catch (IOException ex) {

            throw new ProductImageStorageException(
                    "Failed to initialize product image storage",
                    ex
            );
        }
    }

    @Override
    public StoredProductImage store(Long productId, MultipartFile file) {

        String extension = getFileExtension(file.getOriginalFilename());

        String filename = UUID.randomUUID() + extension;

        String relativeKey = "products/"
                + productId
                + "/"
                + filename;

        Path productDirectory = rootLocation
                .resolve("products")
                .resolve(productId.toString())
                .normalize();

        Path targetPath = productDirectory
                .resolve(filename)
                .normalize();

        if (!targetPath.startsWith(productDirectory)) {

            throw new ProductImageStorageException(
                    "Invalid product image storage path"
            );
        }

        try {

            Files.createDirectories(productDirectory);

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(
                        inputStream,
                        targetPath,
                        StandardCopyOption.REPLACE_EXISTING
                );
            }

            return new StoredProductImage(
                    relativeKey,
                    file.getContentType(),
                    file.getSize()
            );

        } catch (IOException ex) {

            throw new ProductImageStorageException(
                    "Failed to store product image",
                    ex
            );
        }
    }

    @Override
    public void delete(String storageKey) {

        Path filePath = resolveStoragePath(storageKey);

        try {

            Files.deleteIfExists(filePath);

        } catch (IOException ex) {

            throw new ProductImageStorageException(
                    "Failed to delete product image",
                    ex
            );
        }
    }

    @Override
    public byte[] load(String storageKey) {

        Path filePath = resolveStoragePath(storageKey);

        try {

            return Files.readAllBytes(filePath);

        } catch (IOException ex) {

            throw new ProductImageStorageException(
                    "Failed to load product image",
                    ex
            );
        }
    }

    @Override
    public String getContentType(String storageKey) {

        Path filePath = resolveStoragePath(storageKey);

        try {

            String contentType = Files.probeContentType(filePath);

            return contentType != null
                    ? contentType
                    : "application/octet-stream";

        } catch (IOException ex) {

            throw new ProductImageStorageException(
                    "Failed to determine product image content type",
                    ex
            );
        }
    }

    private Path resolveStoragePath(String storageKey) {

        Path resolvedPath = rootLocation
                .resolve(storageKey)
                .normalize();

        if (!resolvedPath.startsWith(rootLocation)) {

            throw new ProductImageStorageException(
                    "Invalid product image storage path"
            );
        }

        return resolvedPath;
    }

    private String getFileExtension(String originalFilename) {

        if (originalFilename == null || originalFilename.isBlank()) {
            return "";
        }

        int lastDot = originalFilename.lastIndexOf('.');

        if (lastDot < 0 || lastDot == originalFilename.length() - 1) {
            return "";
        }

        return originalFilename.substring(lastDot).toLowerCase();
    }
}