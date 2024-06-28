package com.vass.webflux.app.models.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.vass.webflux.app.models.document.Producto;

public interface ProductoRepository extends ReactiveMongoRepository<Producto, String>{

}
