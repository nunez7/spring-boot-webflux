package com.vass.webflux.app.models.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.vass.webflux.app.models.document.Categoria;

public interface CategoriaRepository extends ReactiveMongoRepository<Categoria, String>{

}
