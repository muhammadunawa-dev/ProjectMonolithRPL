package com.tokoonline.service;

import com.tokoonline.dto.OrderItemRequest;
import com.tokoonline.dto.OrderRequest;
import com.tokoonline.dto.OrderResponse;
import com.tokoonline.exception.ResourceNotFoundException;
import com.tokoonline.model.Order;
import com.tokoonline.model.OrderItem;
import com.tokoonline.model.Product;
import com.tokoonline.model.User;
import com.tokoonline.repository.OrderItemRepository;
import com.tokoonline.repository.OrderRepository;
import com.tokoonline.repository.ProductRepository;
import com.tokoonline.repository.UserRepository;
import com.tokoonline.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * ============================================================
 *  Unit Test — OrderServiceImpl.createOrder()
 * ============================================================
 *
 *  Control Flow Graph (CFG) — 10 Node, 15 Edge
 *  -----------------------------------------------
 *  N1  START  — createOrder(userId, request)
 *  N2         — userRepository.findById(userId)
 *  N3  [cond] — User ditemukan?
 *  N3a [err]  — throw ResourceNotFoundException  (user tdk ada)
 *  N4         — Buat Order baru & save
 *  N5  [cond] — for each OrderItem (masih ada item?)
 *  N6         — productRepository.findById(productId)
 *  N7  [cond] — Stok >= quantity?
 *  N7a [err]  — throw IllegalArgumentException   (stok kurang)
 *  N8         — Hitung subtotal, kurangi stok, simpan product
 *  N9  [cond] — Item berikutnya? (loop kembali ke N5 atau lanjut)
 *  N10        — Simpan total, return OrderResponse
 *
 *  Cyclomatic Complexity:
 *  V(G) = E - N + 2P = 15 - 13 + 2(1) = 4
 *
 *  4 Jalur Independen (Basis Path):
 *  -----------------------------------------------
 *  Jalur 1 (Normal — stok cukup):
 *    N1→N2→N3→N4→N5→N6→N7→N8→N9→N5→N10
 *  Jalur 2 (Error — user tidak ditemukan):
 *    N1→N2→N3→N3a
 *  Jalur 3 (Error — stok tidak mencukupi):
 *    N1→N2→N3→N4→N5→N6→N7→N7a
 *  Jalur 4 (Normal — order dengan banyak item):
 *    N1→N2→N3→N4→N5→N6→N7→N8→N9→(loop)→N5→N6→N7→N8→N9→N10
 *
 *  Tools: JUnit 5 + Mockito
 * ============================================================
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("OrderServiceImpl — createOrder() Unit Tests")
class OrderServiceTest {

    // ── Mocks ────────────────────────────────────────────────────────────
    @Mock private OrderRepository     orderRepository;
    @Mock private UserRepository      userRepository;
    @Mock private ProductRepository   productRepository;
    @Mock private OrderItemRepository orderItemRepository;

    @InjectMocks
    private OrderServiceImpl orderService;

    // ── Fixtures ─────────────────────────────────────────────────────────
    private User    user;
    private Product productA;
    private Product productB;
    private Order   savedOrder;

    @BeforeEach
    void setUp() {
        // User
        user = User.builder()
                .id(1L)
                .username("budi")
                .email("budi@mail.com")
                .role(User.Role.USER)
                .build();

        // Produk A — stok 10
        productA = Product.builder()
                .id(10L)
                .name("Laptop Gaming ASUS")
                .price(new BigDecimal("18500000"))
                .stock(10)
                .category("Elektronik")
                .build();

        // Produk B — stok 5
        productB = Product.builder()
                .id(20L)
                .name("Mouse Logitech")
                .price(new BigDecimal("350000"))
                .stock(5)
                .category("Aksesoris")
                .build();

        // Order yang sudah disimpan (sebelum item diisi)
        savedOrder = Order.builder()
                .id(100L)
                .user(user)
                .status(Order.OrderStatus.PENDING)
                .shippingAddress("Jl. Merdeka No. 1, Madiun")
                .totalAmount(BigDecimal.ZERO)
                .orderItems(List.of())
                .build();
    }

    // ════════════════════════════════════════════════════════════════════
    //  JALUR 1 — Normal: satu produk, stok cukup
    //  N1→N2→N3→N4→N5→N6→N7→N8→N9→N10
    // ════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("Jalur 1 — TC-01: createOrder stok cukup, satu produk, berhasil")
    void createOrder_StokCukup_SatuProduk_Berhasil() {
        // ── Arrange ──────────────────────────────────────────
        OrderRequest request = OrderRequest.builder()
                .shippingAddress("Jl. Merdeka No. 1, Madiun")
                .items(List.of(
                        OrderItemRequest.builder().productId(10L).quantity(3).build()
                ))
                .build();

        Order orderDenganItem = Order.builder()
                .id(100L)
                .user(user)
                .status(Order.OrderStatus.PENDING)
                .shippingAddress("Jl. Merdeka No. 1, Madiun")
                .totalAmount(new BigDecimal("55500000"))
                .orderItems(List.of(
                        OrderItem.builder()
                                .id(1L)
                                .order(savedOrder)
                                .product(productA)
                                .quantity(3)
                                .unitPrice(new BigDecimal("18500000"))
                                .subtotal(new BigDecimal("55500000"))
                                .build()
                ))
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder).thenReturn(orderDenganItem);
        when(productRepository.findById(10L)).thenReturn(Optional.of(productA));
        when(orderItemRepository.saveAll(anyList())).thenReturn(List.of());
        when(productRepository.save(any(Product.class))).thenReturn(productA);

        // ── Act ──────────────────────────────────────────────
        OrderResponse response = orderService.createOrder(1L, request);

        // ── Assert ───────────────────────────────────────────
        assertNotNull(response, "Response tidak boleh null");
        assertEquals(100L, response.getId(), "ID order harus 100");
        assertEquals("budi", response.getUsername(), "Username harus budi");
        assertEquals(Order.OrderStatus.PENDING, response.getStatus(), "Status harus PENDING");
        assertEquals(new BigDecimal("55500000"), response.getTotalAmount(), "Total harus 55.500.000");

        // Verifikasi stok dikurangi
        assertEquals(7, productA.getStock(),
                "Stok produk A harus berkurang dari 10 menjadi 7 (dikurangi qty=3)");

        // Verifikasi interaksi repository
        verify(userRepository).findById(1L);
        verify(productRepository).findById(10L);
        verify(productRepository).save(productA);
        verify(orderItemRepository).saveAll(anyList());
        verify(orderRepository, times(2)).save(any(Order.class));
    }

    @Test
    @DisplayName("Jalur 3 — TC-03: createOrder stok kurang dari qty, lempar IllegalArgumentException")
    void createOrder_StokKurang_LemparException() {
        // ── Arrange ──────────────────────────────────────────
        // Stok productA = 10, request qty = 15 → stok tidak cukup
        OrderRequest request = OrderRequest.builder()
                .shippingAddress("Jl. Merdeka No. 1, Madiun")
                .items(List.of(
                        OrderItemRequest.builder().productId(10L).quantity(15).build()
                ))
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
        when(productRepository.findById(10L)).thenReturn(Optional.of(productA));

        // ── Act & Assert ─────────────────────────────────────
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> orderService.createOrder(1L, request),
                "Harus melempar IllegalArgumentException jika stok tidak mencukupi"
        );

        assertTrue(ex.getMessage().toLowerCase().contains("stok") ||
                        ex.getMessage().toLowerCase().contains("mencukupi") ||
                        ex.getMessage().toLowerCase().contains("insufficient"),
                "Pesan error harus menyebut masalah stok");

        // Verifikasi: stok tidak berubah
        assertEquals(10, productA.getStock(), "Stok tidak boleh berubah saat exception");

        // Verifikasi: order item tidak disimpan
        verify(orderItemRepository, never()).saveAll(anyList());
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("Jalur 2 — TC-02: createOrder stok = 0 (habis), qty = 1, lempar exception")
    void createOrder_StokNol_LemparException() {
        // Paksa stok menjadi 0
        productA.setStock(0);

        OrderRequest request = OrderRequest.builder()
                .shippingAddress("Jl. Test No. 1")
                .items(List.of(
                        OrderItemRequest.builder().productId(10L).quantity(1).build()
                ))
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
        when(productRepository.findById(10L)).thenReturn(Optional.of(productA));

        assertThrows(
                IllegalArgumentException.class,
                () -> orderService.createOrder(1L, request),
                "Stok 0 dengan qty 1 harus melempar exception"
        );

        assertEquals(0, productA.getStock(), "Stok tetap 0, tidak boleh berubah");
        verify(orderItemRepository, never()).saveAll(any());
    }












}