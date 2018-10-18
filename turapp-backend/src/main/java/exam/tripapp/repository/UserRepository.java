package exam.tripapp.repository;

import exam.tripapp.controller.UserController;
import org.springframework.data.jpa.repository.JpaRepository;

import exam.tripapp.model.User;

/**
 * Repository for the user database.
 * 
 * @author Anders Engen Olsen
 * @see UserController
 */
public interface UserRepository extends JpaRepository<User, Long> {

}
