package exam.tripapp.controller;

import exam.tripapp.model.NearByTrip;
import exam.tripapp.model.Trip;
import exam.tripapp.model.User;
import exam.tripapp.repository.TripRepository;
import exam.tripapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Set;

/**
 * Controller for operations performed to the trip database.
 *
 * @author Anders Engen Olsen
 */
@RestController
@RequestMapping("/api")
public class TripController {

    @Autowired
    TripRepository tripRepository;
    @Autowired
    UserRepository userRepository;

    /**
     * Creating a new trip
     *
     * @param trip
     * @return 400 Bad Request if not valid
     */
    @PostMapping(value = "/trip", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Trip> createTrip(@Valid @RequestBody Trip trip) {

        User user = userRepository.getOne(trip.getOwner().getId());
        trip.setOwner(user);

        tripRepository.save(trip);

        return ResponseEntity.ok().body(trip);
    }

    /**
     * Returning all trips in the database
     *
     * @return all trips
     */
    @GetMapping("/trip/all")
    public Iterable<Trip> getAllTrips() {
        return tripRepository.findAll();
    }

    /**
     * Returning all passengers on a trip
     */
    @GetMapping("/trip/{id}/passengers")
    public Iterable<User> getAllPassengers(@PathVariable(value = "id") Long id) {

        Trip trip = tripRepository.getOne(id);

        return trip.getPassengers();

    }

    /**
     * Returning a trip with given id
     *
     * @param id tripId
     * @return person object, or 404 not found
     */
    @GetMapping("/trip/{id}")
    public ResponseEntity<Trip> getTripById(@PathVariable(value = "id") Long id) {
        Trip trip = tripRepository.getOne(id);

        // Not found, returning 404 Not Found
        if (trip == null)
            return ResponseEntity.notFound().build();

        // Found
        return ResponseEntity.ok().body(trip);
    }

    /**
     * Updating a trip with the given id
     *
     * @param tripId      The id of the trip to update
     * @param tripDetails The content to be updated
     * @return HTTP status code
     */
    @PutMapping("trip/{id}")
    public ResponseEntity<Trip> updateTrip(@PathVariable(value = "id") Long tripId,
                                           @Valid @RequestBody Trip tripDetails) {
        Trip trip = tripRepository.getOne(tripId);

        // Not found
        if (trip == null) {
            return ResponseEntity.notFound().build();
        }
        // Found
        trip = tripDetails;

        Trip updatedTrip = tripRepository.save(trip);
        return ResponseEntity.ok(updatedTrip);
    }

    /**
     * Deleting a given trip by id
     *
     * @param tripId The id of the trip to delete
     * @return HTTP status code
     */
    @DeleteMapping("trip/{id}")
    public ResponseEntity<Trip> deleteTrip(@PathVariable(value = "id") Long tripId) {
        Trip trip = tripRepository.getOne(tripId);

        if (trip == null) {
            return ResponseEntity.notFound().build();
        }

        tripRepository.delete(trip);
        return ResponseEntity.accepted().body(trip);
    }

    /**
     * Adding a passenger to a trip, if there are available seats.
     *
     * @param tripId id of the trip
     * @param user   user
     * @return user which has been added
     */
    @PostMapping(value = "/trip/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<User> addPassenger(@PathVariable(value = "id") Long tripId, @Valid @RequestBody User user) {

        Trip trip = tripRepository.getOne(tripId);
        User userToAdd = userRepository.getOne(user.getId());

        if (trip == null || userToAdd == null)
            return ResponseEntity.notFound().build();
        else if (trip.getAvailableSeats() <= 0)
            return ResponseEntity.badRequest().build();
        else if (userToAdd.getId().equals(trip.getOwner().getId()))
            return ResponseEntity.badRequest().build();

        Set<User> passengerSet = trip.getPassengers();
        for (User u : passengerSet) {
            if (u.getId().equals(userToAdd.getId()))
                return ResponseEntity.badRequest().build();
        }
        trip.addPassenger(userToAdd);
        trip.setAvailableSeats(trip.getAvailableSeats() - 1);

        tripRepository.save(trip);
        return ResponseEntity.ok().body(userToAdd);
    }

    /**
     * Returning 5 "nearest" trips, based on address
     *
     * @param nearByTrip {@link NearByTrip}
     * @return List of nearby trips
     */
    @PostMapping(value = "/trip/nearby", produces = MediaType.APPLICATION_JSON_VALUE)
    public Iterable<Trip> getNearbyTrips(@Valid @RequestBody NearByTrip nearByTrip) {

        String destinationCity = getCity(nearByTrip.getAddress());
        LocalDateTime departureTime = nearByTrip.getDepartureTime();

        int year = departureTime.getYear();
        int month = departureTime.getMonth().getValue();
        int day = departureTime.getDayOfMonth();

        ArrayList<Trip> trips = (ArrayList<Trip>) tripRepository.findAll();
        ArrayList<Trip> nearByTrips = new ArrayList<>();

        for (Trip t : trips) {
            if (getCity(t.getToDestination()).equals(destinationCity)) {
                LocalDateTime tripTime = t.getDepartureTime();

                int tripYear = tripTime.getYear();
                int tripMonth = tripTime.getMonth().getValue();
                int tripDay = tripTime.getDayOfMonth();

                if (year == tripYear && month == tripMonth && day == tripDay) {
                    if (t.getAvailableSeats() > 0)
                        nearByTrips.add(t);
                }
            }
        }

        return nearByTrips;
    }

    /**
     * Extracting city with postal from address
     *
     * @param address full address String
     * @return postal
     */
    private String getCity(String address) {
        String zipCode = "";
        String[] addressArr = address.split(",");

        for (int i = 0; i < addressArr.length; i++) {
            String part = addressArr[i];
            for (int n = 0; n < part.length(); n++) {
                if (n == 4)
                    zipCode = part;
                if (!Character.isDigit(part.charAt(i)))
                    break;
            }
        }

        return zipCode;
    }

}
