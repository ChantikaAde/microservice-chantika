package com.chantika.order.vo;

import com.chantika.order.model.Order;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResponseTemplate {
    private Order order;
    private Produk produk;

    // Tambahkan Setter Manual karena Lombok sedang bermasalah di IDE kamu
    public void setOrder(Order order) {
        this.order = order;
    }

    public void setProduk(Produk produk) {
        this.produk = produk;
    }

    // Tambahkan Getter Manual untuk jaga-jaga
    public Order getOrder() {
        return order;
    }

    public Produk getProduk() {
        return produk;
    }
}