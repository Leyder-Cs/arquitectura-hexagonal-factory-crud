package com.example.productoscrud.domain.model;

import java.util.List;
import java.util.Map;
public record ResultadoFiltro(
        List<Map<String, Object>> resultados, // contiene los datos dinamicos devueltos por el pipeline de agregación
        String pipeline) { // El texto plano del codigo del pipeline que se ejecuta para que el frontend lo imprima
}