package com.example.productoscrud.app;

import com.example.productoscrud.application.service.ProductService;
import com.example.productoscrud.infrastructure.persistence.mongodb.ProductRepositoryMongo;
import com.example.productoscrud.infrastructure.persistence.mysql.ProductRepositoryMySQL;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication(scanBasePackages = "com.example.productoscrud")
public class App {
    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }
    /**
    * Define y registra en el contenedor de Spring un servicio para mysql
     * se le asigna el nombre sqlService para diferenciar, inyectandole
     * el repositorio de MySQL
    * */
    @Bean
    public ProductService productServiceSql(ProductRepositoryMySQL repository) {
        return new ProductService(repository);
    }
    /**
     *Define y registra un servicio para MongoDB
     * Se le da el nombre de mongoService y se le inyecta el repositorio
     * que corresponde de mongodb
     */

    @Bean
    public ProductService productServiceMongo(ProductRepositoryMongo repository) {
        return new ProductService(repository
        );
    }
}

// Origen: la JVM llama a App.main() (com.example.productoscrud.app).
// Destino: App.main() inicia Spring (org.springframework.boot); Spring llama a App.productService()
// y conecta ProductService (application.service) con ProductRepository (application.ports).
