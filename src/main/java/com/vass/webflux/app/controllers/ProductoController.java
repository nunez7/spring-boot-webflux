package com.vass.webflux.app.controllers;

import java.io.File;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Date;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.thymeleaf.spring6.context.webflux.ReactiveDataDriverContextVariable;

import com.vass.webflux.app.models.document.Categoria;
import com.vass.webflux.app.models.document.Producto;
import com.vass.webflux.app.models.service.ProductoService;

import jakarta.validation.Valid;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@SessionAttributes("producto")
@Controller
public class ProductoController {

	@Autowired
	private ProductoService serviceProducto;

	@Value("${config.uploads.path}")
	private String pathUploadImages;

	private static final Logger log = LoggerFactory.getLogger(ProductoController.class);

	@ModelAttribute("categorias")
	public Flux<Categoria> categorias() {
		return serviceProducto.findAllCategories();
	}

	@GetMapping("/uploads/img/{nombreFoto:.+}")
	public Mono<ResponseEntity<Resource>> verFoto(@PathVariable String nombreFoto) throws MalformedURLException {
		Path ruta = Paths.get(pathUploadImages).resolve(nombreFoto).toAbsolutePath();

		Resource imagen = new UrlResource(ruta.toUri());

		return Mono.just(ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + imagen.getFilename() + "\"")
				.body(imagen));
	}

	@GetMapping({ "/listar", "/" })
	public Mono<String> listar(Model model) {
		Flux<Producto> productos = serviceProducto.findAllWithNamesUppercase();
		productos.subscribe(prod -> log.info(prod.getNombre()));
		model.addAttribute("productos", productos);
		model.addAttribute("titulo", "Listado de productos");
		return Mono.just("listar");
	}

	@GetMapping("/ver/{id}")
	public Mono<String> verDetalle(Model model, @PathVariable String id) {

		return serviceProducto.findById(id).doOnNext(p -> {
			model.addAttribute("producto", p);
			model.addAttribute("titulo", "Detalle del producto");
		}).switchIfEmpty(Mono.just(new Producto())).flatMap(p -> {
			if (p.getId() == null) {
				return Mono.error(new InterruptedException("No existe el producto"));
			}
			return Mono.just(p);
		}).then(Mono.just("ver")).onErrorResume(ex -> Mono.just("redirect:/listar?error=No+existe+el+producto"));
	}

	@GetMapping("/form")
	public Mono<String> crear(Model model) {
		model.addAttribute("producto", new Producto());
		model.addAttribute("titulo", "Formulario de producto");
		model.addAttribute("boton", "Crear");
		return Mono.just("form");
	}

	@PostMapping("/form")
	public Mono<String> guardar(@Valid Producto producto, BindingResult result, Model model, @RequestPart FilePart file,
			SessionStatus status) {
		if (result.hasErrors()) {
			model.addAttribute("titulo", "Errores en el formulario de producto");
			model.addAttribute("boton", "Guardar");
			return Mono.just("form");
		} else {
			Mono<Categoria> categoria = serviceProducto.findCategorieById(producto.getCategoria().getId());
			return categoria.flatMap(c -> {
				if (producto.getCreateAt() == null) {
					producto.setCreateAt(new Date());
				}
				if (!file.filename().isEmpty()) {
					producto.setFoto(UUID.randomUUID().toString() + "-"
							+ file.filename().replace(" ", "_").replace(":", "_").replace("\\", "_"));
				}
				producto.setCategoria(c);
				return serviceProducto.save(producto);
			}).doOnNext(p -> {
				log.info("categoria asignada " + p.getCategoria().getId());
				log.info("Producto guardado: " + p.getNombre() + " Id: " + producto.getId());
			}).flatMap(p -> {
				if (!file.filename().isEmpty()) {
					return file.transferTo(new File(pathUploadImages + p.getFoto()));
				}
				return Mono.empty();
			}).thenReturn("redirect:/listar?success=Producto+guardado+con+exito");
		}
	}

	@GetMapping("/form-v2/{id}")
	public Mono<String> editarV2(@PathVariable String id, Model model) {
		return serviceProducto.findById(id).doOnNext(p -> {
			log.info("Producto" + p.getId());
			model.addAttribute("boton", "Modificar");
			model.addAttribute("titulo", "Editar Producto");
			model.addAttribute("producto", p);
		}).defaultIfEmpty(new Producto()).flatMap(p -> {
			if (p.getId() == null) {
				return Mono.error(new InterruptedException("No existe el producto"));
			}
			return Mono.just(p);
		}).then(Mono.just("form")).onErrorResume(ex -> Mono.just("redirect:/listar?error=No+existe+el+producto"));

	}

	@GetMapping("/form/{id}")
	public Mono<String> editar(@PathVariable String id, Model model) {
		Mono<Producto> mono = serviceProducto.findById(id).defaultIfEmpty(new Producto());

		model.addAttribute("titulo", "Editar Producto");
		model.addAttribute("producto", mono);
		model.addAttribute("boton", "Editar");
		return Mono.just("form");
	}

	@GetMapping("/eliminar/{id}")
	public Mono<String> eliminar(@PathVariable String id) {
		return serviceProducto.findById(id).defaultIfEmpty(new Producto()).flatMap(p -> {
			if (p.getId() == null) {
				return Mono.error(new InterruptedException("No existe el producto a eliminar"));
			}
			return Mono.just(p);
		}).flatMap(p -> {
			log.info("Eliminando producto " + p.getId());
			return serviceProducto.delete(p);
		}).then(Mono.just("redirect:/listar?success=Producto+eliminado+con+exito"))
				.onErrorResume(ex -> Mono.just("redirect:/listar?error=No+existe+el+producto+a+eliminar"));
	}

	@GetMapping("/listar-datadriver")
	public String listarDataDriver(Model model) {
		Flux<Producto> productos = serviceProducto.findAllWithNamesUppercase().delayElements(Duration.ofSeconds(1));
		productos.subscribe(prod -> log.info(prod.getNombre()));
		model.addAttribute("productos", new ReactiveDataDriverContextVariable(productos, 2));
		model.addAttribute("titulo", "Listado de productos");
		return "listar";
	}

	@GetMapping("/listar-full")
	public String listarFull(Model model) {
		Flux<Producto> productos = serviceProducto.findAllWithRepeat();
		model.addAttribute("productos", productos);
		model.addAttribute("titulo", "Listado de productos");
		return "listar";
	}

	@GetMapping("/listar-chunked")
	public String listarChunked(Model model) {
		Flux<Producto> productos = serviceProducto.findAllWithRepeat();
		model.addAttribute("productos", productos);
		model.addAttribute("titulo", "Listado de productos");
		return "listar-chunked";
	}

}
