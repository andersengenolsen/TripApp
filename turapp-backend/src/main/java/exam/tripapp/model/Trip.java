package exam.tripapp.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Entity class for the trip database.
 * 
 * @author anders
 *
 */
@Entity
@Table(name = "trip")
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class Trip {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    @Column(name = "from_destination")
    private String fromDestination;

    @Column(name = "to_destination")
    private String toDestination;

    @Column(precision = 11, scale = 8)
    @NotNull
    private BigDecimal longitudeFrom;

    @Column(precision = 11, scale = 8)
    @NotNull
    private BigDecimal longitudeTo;

    @Column(precision = 10, scale = 8)
    @NotNull
    private BigDecimal latitudeFrom;

    @Column(precision = 10, scale = 8)
    @NotNull
    private BigDecimal latitudeTo;

    @JsonFormat(shape = JsonFormat.Shape.ANY, pattern = "yyyy-MM-dd'T'HH:mm", timezone = "GMT")
    @Column(name = "departure_time")
    private LocalDateTime departureTime;

    @Column(name = "available_seats")
    @NotNull
    private Integer availableSeats;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User owner;

    @JsonIgnore
    @ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @JoinTable(name = "is_passenger", joinColumns = @JoinColumn(name = "trip_id"), inverseJoinColumns = @JoinColumn(name = "person_id"))
    private Set<User> passengers = new HashSet<>();

    public Trip() {

    }

    public Trip(long id) {
	this.id = id;
    }

    public BigDecimal getLongitudeFrom() {
	return longitudeFrom;
    }

    public void setLongitudeFrom(BigDecimal longitudeFrom) {
	this.longitudeFrom = longitudeFrom;
    }

    public BigDecimal getLongitudeTo() {
	return longitudeTo;
    }

    public void setLongitudeTo(BigDecimal longitudeTo) {
	this.longitudeTo = longitudeTo;
    }

    public BigDecimal getLatitudeFrom() {
	return latitudeFrom;
    }

    public void setLatitudeFrom(BigDecimal latitudeFrom) {
	this.latitudeFrom = latitudeFrom;
    }

    public BigDecimal getLatitudeTo() {
	return latitudeTo;
    }

    public void setLatitudeTo(BigDecimal latitudeTo) {
	this.latitudeTo = latitudeTo;
    }

    public User getOwner() {
	return owner;
    }

    public void setOwner(User owner) {
	this.owner = owner;
    }

    public Long getId() {
	return id;
    }

    public void setId(Long id) {
	this.id = id;
    }

    public String getFromDestination() {
	return fromDestination;
    }

    public void setFromDestination(String from) {
	this.fromDestination = from;
    }

    public String getToDestination() {
	return toDestination;
    }

    public void setToDestination(String to) {
	this.toDestination = to;
    }

    public LocalDateTime getDepartureTime() {
	return departureTime;
    }

    public void setDepartureTime(LocalDateTime departureTime) {
	this.departureTime = departureTime;
    }

    public Integer getAvailableSeats() {
	return availableSeats;
    }

    public void setAvailableSeats(Integer availableSeats) {
	this.availableSeats = availableSeats;
    }

    public Set<User> getPassengers() {
	return passengers;
    }

    public void setPassengers(Set<User> passengers) {
	this.passengers = passengers;
    }

    public void addPassenger(User person) {
	passengers.add(person);
    }

}
