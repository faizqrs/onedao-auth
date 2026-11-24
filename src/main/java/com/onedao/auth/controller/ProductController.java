package com.onedao.auth.controller;

import com.onedao.auth.dto.ProductRequest;
import com.onedao.auth.entity.Product;
import com.onedao.auth.repository.ProductRepository;
import jakarta.validation.Valid;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    private static final Set<String> SORTABLE = Set.of("id", "name", "price", "quantity", "createdAt");
    private final ProductRepository repo;

    public ProductController(ProductRepository repo) { this.repo = repo; }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Product create(@Valid @RequestBody ProductRequest r) {
        return repo.save(apply(new Product(), r));
    }

    /** Pagination + sorting + name filter: /api/products?page=0&size=10&sortBy=price&direction=desc&name=lap */
    @GetMapping
    public Page<Product> list(@RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "10") int size,
                              @RequestParam(defaultValue = "id") String sortBy,
                              @RequestParam(defaultValue = "asc") String direction,
                              @RequestParam(required = false) String name) {
        if (!SORTABLE.contains(sortBy)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid sortBy field");
        size = Math.min(Math.max(size, 1), 100);
        Sort sort = direction.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable p = PageRequest.of(Math.max(page, 0), size, sort);
        return (name == null || name.isBlank()) ? repo.findAll(p) : repo.findByNameContainingIgnoreCase(name, p);
    }

    @GetMapping("/{id}")
    public Product get(@PathVariable Long id) { return find(id); }

    @PutMapping("/{id}")
    public Product update(@PathVariable Long id, @Valid @RequestBody ProductRequest r) {
        return repo.save(apply(find(id), r));
    }

    @DeleteMapping("/{id}")
    public Map<String, String> delete(@PathVariable Long id) {
        repo.delete(find(id));
        return Map.of("message", "Product deleted");
    }

    private Product find(Long id) {
        return repo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
    }

    private Product apply(Product p, ProductRequest r) {
        p.setName(r.name());
        p.setDescription(r.description());
        p.setPrice(r.price());
        p.setQuantity(r.quantity());
        return p;
    }
}
