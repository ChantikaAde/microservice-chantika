package com.chantika.order.service;

import java.util.List;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.chantika.order.model.Order;
import com.chantika.order.repository.OrderRepository;
import com.chantika.order.vo.Produk;
import com.chantika.order.vo.ResponseTemplate;


@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private DiscoveryClient discoveryClient;

    private RestTemplate restTemplate = new RestTemplate();

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Order getOrderById(Long id) {
        return orderRepository.findById(id).orElse(null);
    }

    /**
     * Method untuk membuat order dan mengirim notifikasi ke RabbitMQ
     */
    public Order createOrder(Order order) {
        // 1. Simpan ke Database
        Order savedOrder = orderRepository.save(order);

        // 2. Siapkan Pesan Notifikasi dalam format JSON
        String jsonMessage = String.format(
            "{\"idOrder\":%d,\"idPelanggan\":%d,\"idProduk\":%d,\"jumlah\":%d,\"harga\":%.2f,\"totalHarga\":%.2f}",
            savedOrder.getId(),
            savedOrder.getIdPelanggan() != null ? savedOrder.getIdPelanggan() : 0,
            savedOrder.getIdProduk() != null ? savedOrder.getIdProduk() : 0,
            savedOrder.getJumlah(),
            savedOrder.getHarga(),
            savedOrder.getTotalHarga()
        );

        // 3. Kirim ke RabbitMQ (Queue: myQueue)
        try {
            rabbitTemplate.convertAndSend("myQueue", jsonMessage);
            System.out.println("LOG: Pesan berhasil dikirim ke RabbitMQ untuk Order ID: " + savedOrder.getId());
        } catch (Exception e) {
            System.err.println("LOG ERROR: Gagal kirim ke RabbitMQ: " + e.getMessage());
        }

        return savedOrder;
    }

    public ResponseTemplate getOrderWithProdukById(Long id) {
        Order order = getOrderById(id);
        if (order == null) return null;

        ResponseTemplate vo = new ResponseTemplate();
        vo.setOrder(order); 
        vo.setProduk(getProdukById(order.getIdProduk()));
        return vo;
    }

    public void deleteOrder(Long id) {
        orderRepository.deleteById(id);
    }

    private Produk getProdukById(Long idProduk) {
        if (idProduk == null) return null;
        
        try {
            List<ServiceInstance> productInstances = discoveryClient.getInstances("PRODUK");
            if (productInstances == null || productInstances.isEmpty()) {
                return null;
            }

            String produkUrl = productInstances.get(0).getUri() + "/api/produk/" + idProduk;
            return restTemplate.getForObject(produkUrl, Produk.class);
        } catch (RestClientException exception) {
            System.err.println("LOG ERROR: Gagal mengambil data Produk: " + exception.getMessage());
            return null;
        }
    }
}