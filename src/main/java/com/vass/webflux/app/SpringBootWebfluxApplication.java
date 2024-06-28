package com.vass.webflux.app;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

import com.vass.webflux.app.models.document.Categoria;
import com.vass.webflux.app.models.document.Producto;
import com.vass.webflux.app.models.service.ProductoService;

import reactor.core.publisher.Flux;

@SpringBootApplication
public class SpringBootWebfluxApplication implements CommandLineRunner {

	@Autowired
	private ProductoService serviceProducto;

	@Autowired
	private ReactiveMongoTemplate mongoTemplate;

	private static final Logger log = LoggerFactory.getLogger(SpringBootWebfluxApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(SpringBootWebfluxApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		mongoTemplate.dropCollection("productos").subscribe();
		mongoTemplate.dropCollection("categorias").subscribe();

		Categoria celulares = new Categoria("Celulares");
		Categoria deportes = new Categoria("Deportes");
		Categoria computacion = new Categoria("Computacion");
		Categoria muebles = new Categoria("Muebles");

		Flux.just(celulares, deportes, computacion, muebles).flatMap(c -> serviceProducto.saveCategorie(c))
				.doOnNext(c -> log.info("Categoria creada" + c.getId()))
				.thenMany(Flux.just(
						new Producto("Impresora Epson", 4000.50, computacion),
						new Producto("Computadora Lenovo", 8000.00, computacion),
						new Producto("Pantalla NOC", 2000.50, computacion),
						new Producto("Pantalla Phillips", 1800.50, computacion),
						new Producto("Celular Samsung S21", 6000.50, celulares),
						new Producto("Celular Samsung S21 Ultra", 8000.50, celulares)).flatMap(producto -> {
							producto.setCreateAt(new Date());
							return serviceProducto.save(producto);
						}))
				.subscribe(producto -> log.info("Insert: " + producto.getId() + " " + producto.getNombre()));
	}

}
