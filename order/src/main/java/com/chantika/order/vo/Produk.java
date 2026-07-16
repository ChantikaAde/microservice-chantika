package com.chantika.order.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Produk {
    private Long id;
    private String nama;
    private String satuan;
    private double harga;

    // Getter manual agar tidak error di OrderService
    public Long getId() {
        return id;
    }
}