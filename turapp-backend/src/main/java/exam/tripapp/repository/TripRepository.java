package exam.tripapp.repository;

import exam.tripapp.controller.TripController;
import org.springframework.data.jpa.repository.JpaRepository;

import exam.tripapp.model.Trip;

/**
 * Repository for the trip database.
 * 
 * @author Anders Engen Olsen
 * @see TripController
 */
public interface TripRepository extends JpaRepository<Trip, Long> {

}
