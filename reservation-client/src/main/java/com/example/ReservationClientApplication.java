package com.example;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.hateoas.Resources;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

@EnableFeignClients
@EnableZuulProxy
@EnableDiscoveryClient
@EnableCircuitBreaker
@SpringBootApplication
public class ReservationClientApplication {

	public static void main(String[] args) {
		SpringApplication.run(ReservationClientApplication.class, args);
	}

}

//interface ReservationChannels

@FeignClient("reservation-service")
interface ReservationReader {

	@RequestMapping(method = RequestMethod.GET, value = "/reservations")
	Resources<Reservation> read();

}

class Reservation {
	private String reservationName;

	public String getReservationName() {
		return reservationName;
	}

	public void setReservationName(String reservationName) {
		this.reservationName = reservationName;
	}
}

@RestController
@RequestMapping("/reservations")
class ReservationApiGateway {

	private final ReservationReader reservationReader;

	@Autowired
	public ReservationApiGateway(ReservationReader reservationReader){
		this.reservationReader = reservationReader;
	}

	public Collection<String> fallback(){
		return new ArrayList<>();
	}

	@HystrixCommand(fallbackMethod = "fallback")
	@RequestMapping(method = RequestMethod.GET, value = "/names")
	public Collection<String> names(){
		return this.reservationReader.read()
				.getContent()
				.stream()
				.map( r-> r.getReservationName())
				.collect(Collectors.toList());
	}
}