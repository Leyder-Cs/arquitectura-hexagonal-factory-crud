package com.example.productoscrud.application.ports;

import com.example.productoscrud.domain.model.Product;
import com.example.productoscrud.domain.model.ResultadoFiltro;

import java.util.List;
import java.util.Optional;

public abstract class ProductRepository {
    public abstract List<Product> listar();
    public abstract Optional<Product> buscarPorId(int id);
    public abstract Product crear(Product product);
    public abstract boolean actualizar(int id, Product product);
    public abstract boolean eliminar(int id);
    /**
     * Filtra y lista los productos cuyo precio sea mayor o igual a un valor mínimo especificado.
     *  Se implementa con un valor por defecto que lanza una excepción (UnsupportedOperationException)
     * para evitar obligar a la persistencia de MySQL a implementar este método,
     * ya que está diseñado solamente para los pipelines de MongoDB.
     */
    public ResultadoFiltro listarPorPrecioMinimo(double minimo) {
        throw new UnsupportedOperationException("Filtro no disponible en esta persistencia");
    }
    /**
     * Obtiene estadisticas
     * */
    public ResultadoFiltro obtenerEstadisticasPrecio() {
        throw new UnsupportedOperationException("Filtro no disponible en esta persistencia");
    }
    /**
     * Lista una cantindad limitada de los productos más costos del catalogo
     * */
    public ResultadoFiltro listarMasCaros(int limite) {
        throw new UnsupportedOperationException("Filtro no disponible en esta persistencia");
    }
    /**
     * Obtiene el los productos mas economicos
     * */
    public ResultadoFiltro listarMasBaratos(int limite) {
        throw new UnsupportedOperationException("Filtro no disponible en esta persistencia");
    }
}

// Origen: ProductService (com.example.productoscrud.application.service) llama a este contrato.
// Destino: Java ejecuta los métodos sobrescritos en ProductRepositoryMySQL
// (com.example.productoscrud.infrastructure.persistence.mysql), que extiende esta clase.
