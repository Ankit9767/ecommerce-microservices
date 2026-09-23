package com.example.product_service.service.impl;

import com.example.product_service.dto.ProductImageResponse;
import com.example.product_service.entity.Product;
import com.example.product_service.entity.ProductImage;
import com.example.product_service.exception.InvalidProductImageException;
import com.example.product_service.exception.ProductImageNotFoundException;
import com.example.product_service.exception.ProductImageStorageException;
import com.example.product_service.exception.ProductNotFoundException;
import com.example.product_service.repository.ProductImageRepository;
import com.example.product_service.repository.ProductRepository;
import com.example.product_service.service.ProductImageService;
import com.example.product_service.storage.ProductImageStorage;
import com.example.product_service.storage.StoredProductImage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@Service
public class ProductImageServiceImpl implements ProductImageService {

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    private static final List<String> ALLOWED_CONTENT_TYPES = List.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );

    private final ProductRepository productRepository;

    private final ProductImageRepository productImageRepository;

    private final ProductImageStorage productImageStorage;

    public ProductImageServiceImpl(
            ProductRepository productRepository,
            ProductImageRepository productImageRepository,
            ProductImageStorage productImageStorage) {

        this.productRepository = productRepository;
        this.productImageRepository = productImageRepository;
        this.productImageStorage = productImageStorage;
    }

    @Override
    @Transactional
    public ProductImageResponse uploadImage(Long productId, MultipartFile file,
                                            Integer displayOrder,
                                            Boolean primaryImage) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        validateImage(file);

        long existingImageCount =
                productImageRepository.countByProductId(productId);

        boolean makePrimary =
                Boolean.TRUE.equals(primaryImage)
                        || existingImageCount == 0;

        int order = displayOrder != null
                ? displayOrder
                : productImageRepository
                .findMaxDisplayOrderByProductId(productId) + 1;

        if (makePrimary) {
            productImageRepository.clearPrimaryImages(productId);
        }

        StoredProductImage storedImage = null;

        try {
            storedImage = productImageStorage.store(productId, file);

            ProductImage productImage = ProductImage.builder()
                    .product(product)
                    .storageKey(storedImage.storageKey())
                    .contentType(storedImage.contentType())
                    .fileSize(storedImage.fileSize())
                    .originalFilename(sanitizeFilename(file.getOriginalFilename()))
                    .displayOrder(order)
                    .primaryImage(makePrimary)
                    .build();

            ProductImage saved =
                    productImageRepository.save(productImage);

            return toResponse(saved);

        } catch (RuntimeException ex) {

            if (storedImage != null) {

                try {

                    productImageStorage.delete(storedImage.storageKey());

                } catch (RuntimeException cleanupException) {

                    log.error(
                            "Failed to clean up stored product image after database failure. storageKey={}",
                            storedImage.storageKey(),
                            cleanupException
                    );
                }
            }

            throw ex;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductImageResponse> getProductImages(Long productId) {

        ensureProductExists(productId);

        return productImageRepository
                .findAllByProductIdOrderByDisplayOrderAscIdAsc(productId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ProductImageResponse getProductImage(Long productId,
                                                Long imageId) {

        ProductImage image = findImage(productId, imageId);

        return toResponse(image);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] getImageContent(Long productId, Long imageId) {

        ProductImage image = findImage(productId, imageId);

        try {

            return productImageStorage.load(image.getStorageKey());

        } catch (ProductImageStorageException ex) {

            throw ex;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public String getImageContentType(Long productId,
                                      Long imageId) {

        ProductImage image = findImage(productId, imageId);

        return image.getContentType();
    }

    @Override
    @Transactional
    public void deleteImage(Long productId, Long imageId) {

        ProductImage image = findImage(productId, imageId);

        boolean wasPrimary = Boolean.TRUE.equals(image.getPrimaryImage());

        String storageKey = image.getStorageKey();

        productImageRepository.delete(image);

        try {

            productImageStorage.delete(storageKey);

        } catch (RuntimeException ex) {

            log.error(
                    "Failed to delete product image from storage. productId={}, imageId={}, storageKey={}",
                    productId,
                    imageId,
                    storageKey,
                    ex
            );

            throw ex;
        }

        if (wasPrimary) {
            productImageRepository
                    .findFirstByProductIdOrderByDisplayOrderAscIdAsc(productId)
                    .ifPresent(nextPrimary -> {
                        nextPrimary.setPrimaryImage(true);
                        productImageRepository.save(nextPrimary);
                    });
        }
    }

    @Override
    @Transactional
    public ProductImageResponse setPrimaryImage(Long productId,
                                                Long imageId) {

        ProductImage image = findImage(productId, imageId);

        productImageRepository.clearPrimaryImages(productId);

        image.setPrimaryImage(true);

        return toResponse(productImageRepository.save(image));
    }

    private ProductImage findImage(Long productId, Long imageId) {

        ensureProductExists(productId);

        return productImageRepository
                .findByIdAndProductId(imageId, productId)
                .orElseThrow(() ->
                        new ProductImageNotFoundException(
                                productId,
                                imageId
                        ));
    }

    private void ensureProductExists(Long productId) {

        if (!productRepository.existsById(productId)) {

            throw new ProductNotFoundException(productId);
        }
    }

    private void validateImage(MultipartFile file) {

        if (file == null || file.isEmpty()) {

            throw new InvalidProductImageException(
                    "Product image is required"
            );
        }

        if (file.getSize() > MAX_FILE_SIZE) {

            throw new InvalidProductImageException(
                    "Product image must not exceed 5 MB"
            );
        }

        String contentType = file.getContentType();

        if (contentType == null
                || !ALLOWED_CONTENT_TYPES.contains(
                contentType.toLowerCase())) {

            throw new InvalidProductImageException(
                    "Unsupported image type. Allowed types: JPEG, PNG, WEBP"
            );
        }

        String extension = getFileExtension(
                file.getOriginalFilename()
        );

        if (!isValidExtensionForContentType(
                extension,
                contentType)) {

            throw new InvalidProductImageException(
                    "Image file extension does not match its content type"
            );
        }
    }

    private String getFileExtension(String filename) {

        if (filename == null || filename.isBlank()) {
            return "";
        }

        String sanitized = filename.replace("\\", "/");

        int lastSlash = sanitized.lastIndexOf('/');

        if (lastSlash >= 0) {
            sanitized = sanitized.substring(lastSlash + 1);
        }

        int lastDot = sanitized.lastIndexOf('.');

        if (lastDot < 0 || lastDot == sanitized.length() - 1) {
            return "";
        }

        return sanitized
                .substring(lastDot + 1)
                .toLowerCase();
    }

    private boolean isValidExtensionForContentType(String extension,
                                                   String contentType) {

        return switch (contentType.toLowerCase()) {

            case "image/jpeg" ->
                    extension.equals("jpg")
                            || extension.equals("jpeg");

            case "image/png" ->
                    extension.equals("png");

            case "image/webp" ->
                    extension.equals("webp");

            default ->
                    false;
        };
    }

    private String sanitizeFilename(String filename) {

        if (filename == null || filename.isBlank()) {
            return null;
        }

        String sanitized = filename.replace("\\", "/");

        int lastSlash = sanitized.lastIndexOf('/');

        if (lastSlash >= 0) {
            sanitized = sanitized.substring(lastSlash + 1);
        }

        return sanitized.length() > 255
                ? sanitized.substring(0, 255)
                : sanitized;
    }

    private ProductImageResponse toResponse(ProductImage image) {

        return ProductImageResponse.builder()
                .id(image.getId())
                .imageUrl(
                        "/api/products/"
                                + image.getProduct().getId()
                                + "/images/"
                                + image.getId()
                                + "/content"
                )
                .contentType(image.getContentType())
                .fileSize(image.getFileSize())
                .originalFilename(image.getOriginalFilename())
                .displayOrder(image.getDisplayOrder())
                .primaryImage(image.getPrimaryImage())
                .build();
    }
}