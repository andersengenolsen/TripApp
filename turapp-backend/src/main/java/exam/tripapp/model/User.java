package exam.tripapp.model;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import exam.tripapp.repository.UserRepository;

/**
 * Entity class for the user database.
 * 
 * TODO: ADD AUTHENTICATION
 * 
 * @author Anders Engen Olsen
 * 
 * @see UserRepository
 *
 */
@Entity
@Table(name = "user")
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class User {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    @Column
    @NotBlank
    private String name;

    @Column
    @NotBlank
    private String email;

    @Column
    @NotBlank
    private String phone;

    /**
     * Empty default constructor
     */
    public User() {

    }

    /**
     * Constructing with id.
     * 
     * @param id user id
     */
    public User(Long id) {
	this.id = id;
    }

    /**
     * JsonIgnore, not returned through JSON without explicitly called for.
     */
    @JsonIgnore
    @ManyToMany(mappedBy = "passengers")
    private Set<Trip> passengerTrips = new HashSet<>();

    public void setId(Long id) {
	this.id = id;
    }

    /**
     * JsonIgnore, not returned through JSON without explicitly called for.
     * 
     * @return
     */
    @JsonIgnore
    public Set<Trip> getTrips() {
	return passengerTrips;
    }

    public void setTrips(Set<Trip> trips) {
	this.passengerTrips = trips;
    }

    public String getName() {
	return name;
    }

    public void setName(String name) {
	this.name = name;
    }

    public String getEmail() {
	return email;
    }

    public void setEmail(String email) {
	this.email = email;
    }

    public String getPhone() {
	return phone;
    }

    public void setPhone(String phone) {
	this.phone = phone;
    }

    public Long getId() {
	return id;
    }

}
