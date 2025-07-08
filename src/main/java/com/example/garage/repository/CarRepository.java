package com.example.garage.repository;

import com.example.garage.model.Car;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class CarRepository {

    private final Map<String, Car> cars = new ConcurrentHashMap<>();

    public Car save(Car car) {
        cars.put(car.licensePlate(), car);
        return car;
    }

    public Optional<Car> findByLicensePlate(String licensePlate) {
        return Optional.ofNullable(cars.get(licensePlate));
    }

    public void deleteByLicensePlate(String licensePlate) {
        cars.remove(licensePlate);
    }
}
