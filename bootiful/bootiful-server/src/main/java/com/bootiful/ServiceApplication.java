package com.bootiful;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

/**
 * https://www.youtube.com/watch?v=FvDSL3pSKNQ
 */
@SpringBootApplication
public class ServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServiceApplication.class, args);
    }
}

@RestController
class CustomerRestController {
    private final CustomerRepository customerRepository;
    private final ObservationRegistry observationRegistry;

    CustomerRestController(CustomerRepository customerRepository, ObservationRegistry observationRegistry) {
        this.customerRepository = customerRepository;
        this.observationRegistry = observationRegistry;
    }

    @GetMapping("/customers/{name}")
    Iterable<Customer> getCustomer(@PathVariable String name) {
        Assert.state(Character.isUpperCase(name.charAt(0)), "The name must start with a capital letter");

        return Observation.createNotStarted("by-name", observationRegistry).observe(() -> customerRepository.findByName(name));
        //    return customerRepository.findByName(name);
    }

    @GetMapping("/customers")
    Iterable<Customer> getCustomers() {
        return customerRepository.findAll();
    }
}

@ControllerAdvice
class ErrorHandlingControllerAdvice {
    //  @ExceptionHandler
    //  static ProblemDetail handleIllegalStateException(IllegalStateException exception) {
    //    ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
    //    problemDetail.setDetail(exception.getLocalizedMessage());
    //
    //    return problemDetail;
    //  }

    @ExceptionHandler
    static ProblemDetail handleIllegalStateException(IllegalStateException exception, HttpServletRequest httpServletRequest) {
        httpServletRequest.getHeaderNames().asIterator().forEachRemaining(System.out::println);
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problemDetail.setDetail(exception.getLocalizedMessage());

        return problemDetail;
    }
}
