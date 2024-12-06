package com.example.springboot.controllers;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.springboot.dtos.ProductRecordDto;
import com.example.springboot.models.ProductModel;
import com.example.springboot.repositories.ProductReposity;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

import jakarta.validation.Valid;

@RestController
public class ProductController {
	
	@Autowired
	ProductReposity productRepository;
	
	
	@PostMapping("/products") //  Método POST (Salvar)
	public ResponseEntity<ProductModel> saveProduct(@RequestBody @Valid ProductRecordDto productRecordDto){
		ProductModel productModel = new ProductModel();
		BeanUtils.copyProperties(productRecordDto, productModel);
		return ResponseEntity.status(HttpStatus.CREATED).body(productRepository.save(productModel));
	}
	
	/*
	@GetMapping("/products") //  Método GET ALL (Consulta TODOS produtos) - sem HATEOAS
	public ResponseEntity<List<ProductModel>> getAllProducts(){
		return ResponseEntity.status(HttpStatus.OK).body(productRepository.findAll());
	}*/
	
	@GetMapping("/products") //  Método GET ALL (Consulta TODOS produtos) - com HATEOAS
	public ResponseEntity<List<ProductModel>> getAllProducts(){
		
		List<ProductModel> productsLists = productRepository.findAll();
		if(!productsLists.isEmpty()) {
			
			for(ProductModel product : productsLists) {
				UUID id = product.getIdProduct();
				product.add(linkTo(methodOn(ProductController.class).getOneProduct(id)).withSelfRel());
				
			}
			
		}
		
		return ResponseEntity.status(HttpStatus.OK).body(productsLists);
	}
	
	@GetMapping("/products/{id}") //  Método GET ONE (Consulta único produto) - com HATEOAS
	public ResponseEntity<Object> getOneProduct(@PathVariable(value="id") UUID id){
		Optional<ProductModel> productO = productRepository.findById(id);
		if(productO.isEmpty()){
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product not found");
		}
		
		productO.get().add(linkTo(methodOn(ProductController.class).getAllProducts()).withRel("Products List"));

		
		return ResponseEntity.status(HttpStatus.OK).body(productO.get());
	}
	
	
	@PutMapping("/products/{id}") //  Método PUT (Atualizar produto)
	public ResponseEntity<Object> updateProduct(@PathVariable(value="id") UUID id
												, @RequestBody @Valid ProductRecordDto productRecordDto){
		
		Optional<ProductModel> productO = productRepository.findById(id);
		if(productO.isEmpty()){
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product not found");
		}
		
		ProductModel productModel = productO.get(); //Não é uma instancia do zero. Considerar o ID que já existe e atualizar somente outros atributos.
		BeanUtils.copyProperties(productRecordDto, productModel);
		return ResponseEntity.status(HttpStatus.OK).body(productRepository.save(productModel));
	}
	
	
	@DeleteMapping("/products/{id}") //  Método DELETE (Deletar produto)
	public ResponseEntity<Object> deleteProduct(@PathVariable(value="id") UUID id){
		
		Optional<ProductModel> productO = productRepository.findById(id);
		if(productO.isEmpty()){
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product not found");
		}
		
		productRepository.delete(productO.get());
		
		return ResponseEntity.status(HttpStatus.OK).body("Product deleted sucessfully");
	}
	
	
}
