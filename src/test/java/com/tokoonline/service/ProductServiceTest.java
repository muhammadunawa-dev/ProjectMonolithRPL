package com.tokoonline.service;

import com.tokoonline.dto.ProductRequest;
import com.tokoonline.dto.ProductResponse;
import com.tokoonline.exception.ResourceNotFoundException;
import com.tokoonline.model.Product;
import com.tokoonline.repository.ProductRepository;
import com.tokoonline.service.impl.ProductServiceImpl;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product product;
    private ProductRequest request;

    @BeforeEach
    void setUp() {
        product = Product.builder()
                .id(1L).name("Laptop").description("Laptop Gaming")
                .price(new BigDecimal("15000000")).stock(10).category("Elektronik").build();
        request = ProductRequest.builder()
                .name("Laptop").description("Laptop Gaming")
                .price(new BigDecimal("15000000")).stock(10).category("Elektronik").build();
    }

    // TC-01: Buat produk dengan data valid
    @Test
    @DisplayName("TC-01: createProduct dengan data valid berhasil")
    void createProduct_ValidData_Success() {
        when(productRepository.save(any(Product.class))).thenReturn(product);
        ProductResponse response = productService.createProduct(request);
        assertNotNull(response);
        assertEquals("Laptop", response.getName());
        assertEquals(new BigDecimal("15000000"), response.getPrice());
        verify(productRepository, times(1)).save(any(Product.class));
    }

    // TC-02: Buat produk stok negatif
    @Test
    @DisplayName("TC-02: createProduct stok negatif melempar IllegalArgumentException")
    void createProduct_NegativeStock_ThrowsException() {
        request.setStock(-1);
        assertThrows(IllegalArgumentException.class, () -> productService.createProduct(request));
        verify(productRepository, never()).save(any());
    }

    // TC-03: Get produk by ID valid
    @Test
    @DisplayName("TC-03: getProductById ID valid mengembalikan produk")
    void getProductById_ValidId_ReturnsProduct() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        ProductResponse response = productService.getProductById(1L);
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Laptop", response.getName());
    }

    // TC-04: Get produk by ID tidak ditemukan
    @Test
    @DisplayName("TC-04: getProductById ID tidak ditemukan melempar ResourceNotFoundException")
    void getProductById_InvalidId_ThrowsNotFoundException() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> productService.getProductById(99L));
    }

    // TC-05: Get semua produk
    @Test
    @DisplayName("TC-05: getAllProducts mengembalikan list produk")
    void getAllProducts_ReturnsAllProducts() {
        when(productRepository.findAll()).thenReturn(List.of(product));
        List<ProductResponse> responses = productService.getAllProducts();
        assertEquals(1, responses.size());
        assertEquals("Laptop", responses.get(0).getName());
    }

    // TC-06: Update produk berhasil
    @Test
    @DisplayName("TC-06: updateProduct data valid berhasil diupdate")
    void updateProduct_ValidData_Success() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        request.setName("Laptop Pro");
        Product updated = Product.builder().id(1L).name("Laptop Pro")
                .price(new BigDecimal("15000000")).stock(10).category("Elektronik").build();
        when(productRepository.save(any(Product.class))).thenReturn(updated);
        ProductResponse response = productService.updateProduct(1L, request);
        assertEquals("Laptop Pro", response.getName());
    }

    // TC-07: Update produk ID tidak ditemukan
    @Test
    @DisplayName("TC-07: updateProduct ID tidak ditemukan melempar exception")
    void updateProduct_InvalidId_ThrowsException() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> productService.updateProduct(99L, request));
    }

    // TC-08: Delete produk berhasil
    @Test
    @DisplayName("TC-08: deleteProduct ID valid berhasil dihapus")
    void deleteProduct_ValidId_Success() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        doNothing().when(productRepository).delete(any(Product.class));
        assertDoesNotThrow(() -> productService.deleteProduct(1L));
        verify(productRepository, times(1)).delete(product);
    }

    // TC-09: Update stock berhasil
    @Test
    @DisplayName("TC-09: updateStock penambahan stok berhasil")
    void updateStock_AddStock_Success() {
        product.setStock(10);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);
        ProductResponse response = productService.updateStock(1L, 5);
        assertEquals(15, response.getStock());
    }
    // TC-10: Update stock hingga negatif
    @Test
    @DisplayName("TC-10: updateStock stok tidak mencukupi melempar exception")
    void updateStock_InsufficientStock_ThrowsException() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        assertThrows(IllegalArgumentException.class, () -> productService.updateStock(1L, -100));
    }
}
