package com.vass.webflux.app.models.service;

import com.vass.webflux.app.models.document.Categoria;
import com.vass.webflux.app.models.document.Producto;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ProductoService {
	
	public Flux<Producto> findAll();
	
	public Flux<Producto> findAllWithNamesUppercase();

	public Flux<Producto> findAllWithRepeat();
	
	public Mono<Producto> findById(String id);
	
	public Mono<Producto> save(Producto producto);
	
	public Mono<Void> delete(Producto producto);
	
	public Flux<Categoria> findAllCategories();
	
	public Mono<Categoria> findCategorieById(String id);
	
	public Mono<Categoria> saveCategorie(Categoria categoria);

}
