package com.example.productoscrud.infrastructure.persistence.mongodb;

import com.example.productoscrud.application.ports.ProductRepository;
import com.example.productoscrud.domain.model.Product;
import java.math.BigDecimal;
import java.util.*;

import com.example.productoscrud.domain.model.ResultadoFiltro;
import org.bson.Document;
import org.bson.types.Decimal128;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

@Repository
public class ProductRepositoryMongo extends ProductRepository {
    //Constantes que definen los nombre de las colección en MongoDB
    private static final String COLECCION = "products";
    private static final String CONTADORES = "counters";
    //Esta en la herramienta de sprint data para interactuar con mongo de forma flexible
    private final MongoTemplate mongoTemplate;
    //Constructor para la inyección de dependencia de MongoTemplate
    public ProductRepositoryMongo(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }
    //Simula el auto incremental de sql en mongodb
    //usamos una colección separa de counters para saber el ultimo id que fue asignado
    // incrementamos en 1 cada vez que se nesecita
    /**
    se realizo de esta manera ya que lo que estaba realizado se recibia el id de tipo int
    entonces mongo al usar id como objectid, daba inconsistencia, entonces para esta actividad
    se realizo de esta manera
     */
    private int siguienteId() {
        Document contador = mongoTemplate.findAndModify(
                new Query(Criteria.where("_id").is("products")), //Busca el documento contador para productos
                new Update().inc("seq", 1),// Incremente el campo de "seq" en 1
                FindAndModifyOptions.options().returnNew(true).upsert(true),// si no existe, lo cra y devuelve el valor nuevo
                Document.class,
                CONTADORES);
        // Extrae el valor numerico de la secuencia y lo convierte a entero
        return contador.getInteger("seq");
    }
    /**
     * Metodo para transforma un documento BSON
     * en un objeto del dominio product que entienda la aplicación
     * */
    private Product mapear(Document doc) {
        Object precio = doc.get("price");
        // Maneja de forma seguira la conversión de tipo de datos decimal de mongodb a BigDecimal de java
        BigDecimal price = precio instanceof Decimal128 d
                ? d.bigDecimalValue()
                : BigDecimal.valueOf(((Number) precio).doubleValue());
        return new Product(
                doc.getInteger("_id"),
                doc.getString("name"),
                price,
                doc.getString("description"));
    }
    /**
     * obtitene todos los productos de la colección de forma ascendente
     */
    @Override
    public List<Product> listar() {
        Query query = new Query().with(Sort.by(Sort.Direction.ASC, "_id"));
        return mongoTemplate.find(query, Document.class, COLECCION)
                .stream().map(this::mapear).toList();
    }

    /**
     * Consulta por id un producto individual buscando por su identificador
     */
    @Override
    public Optional<Product> buscarPorId(int id) {
        Document doc = mongoTemplate.findById(id, Document.class, COLECCION);
        return Optional.ofNullable(doc).map(this::mapear);
    }
    /**
     * Genera un nuevo id incremental, arma el documento bson con los datos
     * y lo inserta en la colección de mongoDB
     */

    @Override
    public Product crear(Product product) {
        int id = siguienteId();
        Document doc = new Document("_id", id)
                .append("name", product.getName())
                .append("price", new Decimal128(product.getPrice()))
                .append("description", product.getDescription());
        mongoTemplate.insert(doc, COLECCION);
        return new Product(id, product.getName(), product.getPrice(), product.getDescription());
    }
    /**
     * Busca el producto por ID y modifica sus campos
     * devuelve true si encontro y actualizo con exito
     * */
    @Override
    public boolean actualizar(int id, Product product) {
        Update update = new Update()
                .set("name", product.getName())
                .set("price", new Decimal128(product.getPrice()))
                .set("description", product.getDescription());
        return mongoTemplate.updateFirst(
                new Query(Criteria.where("_id").is(id)), update, COLECCION).getMatchedCount() > 0;
    }
    /**
     *Borra el documento de la colección usando el id
     * devuelve true si elimino al menos un registro
     */
    @Override
    public boolean eliminar(int id) {
        return mongoTemplate.remove(
                new Query(Criteria.where("_id").is(id)), COLECCION).getDeletedCount() > 0;
    }
    private Map<String, Object> normalizar(Document doc) {
        Map<String, Object> mapa = new LinkedHashMap<>();
        doc.forEach((clave, valor) ->
                mapa.put(clave, valor instanceof Decimal128 d ? d.bigDecimalValue() : valor));
        return mapa;
    }

    private ResultadoFiltro ejecutar(List<Document> pipeline) {
        List<Map<String, Object>> resultados = mongoTemplate.getCollection(COLECCION)
                .aggregate(pipeline)
                .map(this::normalizar)
                .into(new ArrayList<>());
        String texto = "db.products.aggregate([\n  "
                + String.join(",\n  ", pipeline.stream().map(Document::toJson).toList())
                + "\n])";
        return new ResultadoFiltro(resultados, texto);
    }

    // Filtro 1: $match + $project + $sort (productos con precio >= minimo)
    @Override
    public ResultadoFiltro listarPorPrecioMinimo(double minimo) {
        return ejecutar(List.of(
                new Document("$match", new Document("price", new Document("$gte", minimo))),
                new Document("$project", new Document("_id", 0).append("name", 1).append("price", 1)),
                new Document("$sort", new Document("price", -1))));
    }

    // Filtro 2: $group con $sum, $avg, $min y $max (un único resultado)
    @Override
    public ResultadoFiltro obtenerEstadisticasPrecio() {
        return ejecutar(List.of(
                new Document("$group", new Document("_id", null)
                        .append("total_productos", new Document("$sum", 1))
                        .append("precio_promedio", new Document("$avg", "$price"))
                        .append("precio_minimo", new Document("$min", "$price"))
                        .append("precio_maximo", new Document("$max", "$price")))));
    }

    // Filtro 3: $sort + $limit + $project (los N más caros)
    @Override
    public ResultadoFiltro listarMasCaros(int limite) {
        return ejecutar(List.of(
                new Document("$sort", new Document("price", -1)),
                new Document("$limit", limite),
                new Document("$project", new Document("_id", 0).append("name", 1).append("price", 1))));
    }

    // Filtro 4: $sort ascendente + $limit + $project (los N más baratos)
    @Override
    public ResultadoFiltro listarMasBaratos(int limite) {
        return ejecutar(List.of(
                new Document("$sort", new Document("price", 1)),
                new Document("$limit", limite),
                new Document("$project", new Document("_id", 0).append("name", 1).append("price", 1))));
    }
}