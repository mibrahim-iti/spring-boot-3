package com.bootiful.client;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.BatchMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@SpringBootApplication
public class ServiceClientApplication {

	public static void main(String[] args) {
		SpringApplication.run(ServiceClientApplication.class, args);
	}

	@Bean
	ApplicationRunner applicationRunner(CustomerRestClient customerRestClient) {
		return arguments -> {
			customerRestClient.byName("Mohamed").subscribe(System.out::println);
			customerRestClient.all().subscribe(System.out::println);
		};
	}

	@Bean
	CustomerRestClient createCustomerRestClient(WebClient.Builder webClientBuilder) {
		WebClient webClient = webClientBuilder.baseUrl("http://localhost:8080/").build();
		WebClientAdapter webClientAdapter = WebClientAdapter.forClient(webClient);

		return HttpServiceProxyFactory.builder()
				.clientAdapter(webClientAdapter)
				.build()
				.createClient(CustomerRestClient.class);
	}
}

@Controller
class CustomerGraphqlController {
	private final CustomerRestClient customerRestClient;
	CustomerGraphqlController(CustomerRestClient customerRestClient) {
		this.customerRestClient = customerRestClient;
	}

	// Resolver, this way it will do query for each profile inside the cutomer
//	@SchemaMapping(typeName = "Customer")
//	Profile profile(Customer customer) {
//		return new Profile(customer.id());
//	}

	@BatchMapping
	Map<Customer, Profile> profile(List<Customer> customers) {
		return customers.stream()
				.collect(Collectors
						.toMap(Function.identity(), customer -> new Profile(customer.id())));
	}

	@QueryMapping
//	@SchemaMapping(typeName = "Query", field = "customers")
	Flux<Customer> customers() {
		return customerRestClient.all();
	}

	@QueryMapping
//	@SchemaMapping(typeName = "Query", field = "customer")
	Flux<Customer> customer(@Argument String name) {
		return customerRestClient.byName(name);
	}
}

interface CustomerRestClient {
	@GetExchange("/customers")
	Flux<Customer> all();

	@GetExchange(value = "/customers/{name}")
	Flux<Customer> byName(@PathVariable String name);
}

record Profile(Integer id) {}

record Customer(Integer id, String name, Profile profile) {}
