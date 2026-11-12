package com.alejandro.expense_tracker.movement;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


public class MovementSpecifications {

    public static Specification<Movement> withFilters(
            // se crean los filtros opcionales
            Long userId,
            MovementType type,
            Long categoryId,
            LocalDate fromDate,
            LocalDate toDate
    ) {
        // movement es la entidad dentro de la consulta
        // query es la consulta completa
        // builder permite construir comparaciones y combinarlas
        return (movement, query, builder) ->{

            List<Predicate> conditions = new ArrayList<>();

            // devuelve los movimientos del user autenticado
            conditions.add(builder.equal(movement.get("user").get("id"), userId));

            // si el tipo no es nulo devuelve el tipo de movimiento
            if (type != null) {
                conditions.add(builder.equal(movement.get("type"), type));
            }

            // si el id de la categoria no es nulo se indica una categoria
            if (categoryId != null) {
                conditions.add(builder.equal(movement.get("category").get("id"), categoryId));
            }

            // si hay fecha inicial se indica los movimientos de esa fecha
            if (fromDate != null) {
                conditions.add(builder.greaterThanOrEqualTo(movement.get("date"), fromDate));
            }

            // si hay fecha final se indica los movimientos de esa fecha
            if (toDate != null) {
                conditions.add(builder.lessThanOrEqualTo(movement.get("date"), toDate));
            }

            // Convertimos la lista en un array y unimos las condiciones con AND.
            // El movimiento debe cumplir todas las condiciones añadidas.
            return builder.and(conditions.toArray(new Predicate[0]));
        };

    }
}
