package com.example.productoscrud.infrastructure.web.controllers;

import com.example.productoscrud.domain.model.ResultadoFiltro;
import org.springframework.beans.factory.annotation.Qualifier;
import com.example.productoscrud.application.service.ProductService;
import com.example.productoscrud.domain.model.Product;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

//NUEVA RUTA
@RestController
@RequestMapping("/nosql/productos")
@CrossOrigin(origins = "*")
public class ProductMongoController {
    private final ProductService productService;
    //Se le indicuo usar el servicio de Mongo
    public ProductMongoController(@Qualifier("productServiceMongo") ProductService productService) {
        this.productService = productService;
    }

    @GetMapping({"", "/"})
    public List<Product> listar() {
        return productService.listar();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> buscarPorId(@PathVariable int id) {
        return productService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping({"", "/"})
    public ResponseEntity<?> crear(@RequestBody Product product) {
        if (!product.esValido()) {
            return ResponseEntity.badRequest().body("Nombre, precio o descripción inválidos");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.crear(product));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable int id, @RequestBody Product product) {
        if (!product.esValido()) {
            return ResponseEntity.badRequest().body("Nombre, precio o descripción inválidos");
        }
        return productService.actualizar(id, product)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable int id) {
        return productService.eliminar(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
    // Se definen las rutas para hacer la consulta
    @GetMapping("/filtros/precio-minimo")
    public ResultadoFiltro precioMinimo(@RequestParam double minimo) {
        return productService.listarPorPrecioMinimo(minimo);
    }

    @GetMapping("/filtros/estadisticas")
    public ResultadoFiltro estadisticas() {
        return productService.obtenerEstadisticasPrecio();
    }

    @GetMapping("/filtros/mas-caros")
    public ResultadoFiltro masCaros(@RequestParam(defaultValue = "3") int limite) {
        return productService.listarMasCaros(limite);
    }

    @GetMapping("/filtros/mas-baratos")
    public ResultadoFiltro masBaratos(@RequestParam(defaultValue = "3") int limite) {
        return productService.listarMasBaratos(limite);
    }
}

// Origen: Spring MVC (org.springframework.web.servlet) invoca los métodos de ProductController
// (com.example.productoscrud.infrastructure.web.controllers) según la petición HTTP.
// Destino: ProductController llama a ProductService (com.example.productoscrud.application.service)
// y recibe o devuelve Product (com.example.productoscrud.domain.model) como JSON.
