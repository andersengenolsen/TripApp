package exam.tripapp.controller;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import javax.validation.Valid;

import exam.tripapp.model.User;
import exam.tripapp.repository.TripRepository;
import exam.tripapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import exam.tripapp.model.Trip;

/**
 * Controller for operations to the user-database.
 *
 * @author Anders Engen Olsen
 */
@RestController
@RequestMapping("/api")
public class UserController {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TripRepository tripRepository;

    /**
     * Creating a new user
     *
     * @param user
     * @return 400 Bad Request if not valid
     */
    @PostMapping(value = "/user", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<User> createUser(@Valid @RequestBody User user) {

        userRepository.save(user);

        return ResponseEntity.ok().body(user);
    }

    /**
     * Updating a user with the given id
     *
     * @param userId      The id of the user to update
     * @param userDetails New User-object
     * @return Updated user
     */
    @PostMapping("/user/{id}/update")
    public ResponseEntity<User> updateUser(@PathVariable(value = "id") Long userId,
                                           @Valid @RequestBody User userDetails) {
        User user = userRepository.getOne(userId);

        // Not found
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        // Found
        user.setName(userDetails.getName());
        user.setPhone(userDetails.getPhone());
        user.setEmail(userDetails.getEmail());

        userRepository.save(user);

        return ResponseEntity.ok().body(user);
    }

    /**
     * Returning a user with given id
     *
     * @param id user id
     * @return user object, or 404 not found
     */
    @GetMapping("/user/{id}")
    public ResponseEntity getUserById(@PathVariable(value = "id") Long id) {
        User user = userRepository.getOne(id);

        // Not found, returning 404 Not Found
        if (user == null)
            return ResponseEntity.notFound().build();

        // Found
        return ResponseEntity.ok().body(user);
    }

    /**
     * Returning all trips for given user, where the user is a passenger.
     *
     * @param id userId
     * @return all trips for a given user,
     */
    @GetMapping("/user/{id}/passengertrips")
    public ResponseEntity<Set<Trip>> getPassengerTrips(@PathVariable(value = "id") Long id) {

        LocalDateTime now = LocalDateTime.now().atOffset(ZoneOffset.UTC).toLocalDateTime();

        System.out.println(now);

        User user = userRepository.getOne(id);

        if (user == null)
            return ResponseEntity.notFound().build();

        Set<Trip> trips = user.getTrips();
        Iterator<Trip> iterator = trips.iterator();

        while (iterator.hasNext()) {
            LocalDateTime tripTime = iterator.next().getDepartureTime();

            if (tripTime.compareTo(now) < 0)
                iterator.remove();

        }

        return ResponseEntity.ok(user.getTrips());
    }

    /**
     * Deleting a user from a given trip
     *
     * @param id userId
     * @return all trips for a given user,
     */
    @DeleteMapping("/user/{id}/passengertrips/{tripId}")
    public ResponseEntity<Trip> deletePassengerTrips(@PathVariable(value = "id") Long id,
                                                     @PathVariable(value = "tripId") Long tripId) {

        User user = userRepository.getOne(id);
        Trip trip = tripRepository.getOne(tripId);

        if (user == null || trip == null)
            return ResponseEntity.notFound().build();

        Set<User> passengers = trip.getPassengers();

        Iterator<User> iterator = passengers.iterator();

        boolean removed = false;
        while (iterator.hasNext()) {
            if (iterator.next().getId().equals(user.getId())) {
                iterator.remove();
                removed = true;
                break;
            }
        }

        if (!removed)
            return ResponseEntity.notFound().build();

        trip.setPassengers(passengers);
        trip.setAvailableSeats(trip.getAvailableSeats() + 1);
        tripRepository.save(trip);

        return ResponseEntity.ok(trip);
    }

    /**
     * Returning all trips for given user, where the user is a driver.
     *
     * @param id userId
     * @return all trips
     */
    @GetMapping("/user/{id}/drivertrips")
    public ResponseEntity<Iterable<Trip>> getDriverTrips(@PathVariable(value = "id") Long id) {

        User user = userRepository.getOne(id);
        if (user == null)
            return ResponseEntity.notFound().build();

        LocalDateTime now = LocalDateTime.now().atOffset(ZoneOffset.UTC).toLocalDateTime();

        ArrayList<Trip> trips = (ArrayList<Trip>) tripRepository.findAll();
        ArrayList<Trip> driverTrips = new ArrayList<Trip>();

        for (Trip trip : trips) {
            if (trip.getOwner().getId().equals(id)) {
                if (trip.getDepartureTime().compareTo(now) > 0)
                    driverTrips.add(trip);
            }
        }

        return ResponseEntity.ok(driverTrips);
    }
}
