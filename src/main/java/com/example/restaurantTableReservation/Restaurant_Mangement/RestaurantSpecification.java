package com.example.restaurantTableReservation.Restaurant_Mangement;

import com.example.restaurantTableReservation.Restaurant_Mangement.model.RestaurantModel;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

/**
 * Utility class providing JPA `Specification` instances to filter `RestaurantModel` entities.
 * Methods return `conjunction()` (no-op predicate) when the corresponding filter is not provided,
 * making them safe to combine with `Specification.where(...).and(...)`.
 */
public class RestaurantSpecification {

    /**
     * Returns a specification matching restaurants whose `cuisine` is in the provided list.
     * If `cuisines` is null or empty, returns a no-op predicate so this filter is ignored.
     */
    public static Specification<RestaurantModel> hasCuisine(List<String> cuisines){
        return ((root, query, criteriaBuilder) ->
                cuisines == null || cuisines.isEmpty() ? criteriaBuilder.conjunction() : root.get("cuisine").in(cuisines)
        );
    }

    /**
     * Returns a specification that filters restaurants with `overallRating >= rating`.
     * A null or zero `rating` causes this specification to be ignored (conjunction).
     */
    public static Specification<RestaurantModel> hasMinRating(Double rating){
        return ((root, query, criteriaBuilder) ->(
                rating == null || rating == 0.0 ? criteriaBuilder.conjunction(): criteriaBuilder.greaterThanOrEqualTo(root.get("overallRating"), rating))
        );
    }

    /**
     * Returns a specification matching restaurants with the exact `priceRange` value.
     * If `priceRange` is null or equals the sentinel "any_range", the filter is ignored.
     */
    public static Specification<RestaurantModel> hasPriceRange(String priceRange){
        return ((root, query, criteriaBuilder) ->
                priceRange == null || "any_range".equals(priceRange) ? criteriaBuilder.conjunction() : criteriaBuilder.equal(root.get("priceRange"), priceRange)
        );
    }
}
