package com.vass.webflux.app.models.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vass.webflux.app.models.document.Categoria;
import com.vass.webflux.app.models.document.Producto;
import com.vass.webflux.app.models.repository.CategoriaRepository;
import com.vass.webflux.app.models.repository.ProductoRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class ProductoServiceImpl implements ProductoService{
	
	@Autowired
	private ProductoRepository productoRepository;
	
	@Autowired
	private CategoriaRepository categoriaRepository;

	@Override
	@Transactional(readOnly = true)
	public Flux<Producto> findAll() {
		return productoRepository.findAll();
	}

	@Override
	@Transactional(readOnly = true)
	public Mono<Producto> findById(String id) {
		return productoRepository.findById(id);
	}

	@Override
	@Transactional
	public Mono<Producto> save(Producto producto) {
		return productoRepository.save(producto);
	}

	@Override
	@Transactional
	public Mono<Void> delete(Producto producto) {
		return productoRepository.delete(producto);
	}

	@Override
	public Flux<Producto> findAllWithNamesUppercase() {
		return productoRepository.findAll().map(producto ->{
			producto.setNombre(producto.getNombre().toUpperCase());
			return producto;
		});
	}

	@Override
	public Flux<Producto> findAllWithRepeat() {
		return findAllWithNamesUppercase().repeat(5000);
	}

	@Override
	public Flux<Categoria> findAllCategories() {
		return categoriaRepository.findAll();
	}

	@Override
	public Mono<Categoria> findCategorieById(String id) {
		return categoriaRepository.findById(id);
	}

	@Override
	public Mono<Categoria> saveCategorie(Categoria categoria) {
		return categoriaRepository.save(categoria);
	}

}
