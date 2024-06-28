package com.vass.webflux.app.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.vass.webflux.app.models.document.Producto;
import com.vass.webflux.app.models.repository.ProductoRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/productos")
public class ProductRestController {

	@Autowired
	private ProductoRepository productoRepository;

	private static final Logger log = LoggerFactory.getLogger(ProductoController.class);

	@GetMapping()
	public Flux<Producto> index() {
		Flux<Producto> productos = productoRepository.findAll().map(producto -> {
			producto.setNombre(producto.getNombre().toUpperCase());
			return producto;
		}).doOnNext(prod -> log.info(prod.getNombre()));

		return productos;
	}

	@GetMapping("/{id}")
	public Mono<Producto> showById(@PathVariable String id) {
		Mono<Producto> producto = productoRepository.findById(id);

		if (producto == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found");
		}

		return producto;
	}

}
