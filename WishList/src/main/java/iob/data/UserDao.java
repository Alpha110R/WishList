package iob.data;

import org.springframework.data.repository.PagingAndSortingRepository;

//import org.springframework.data.repository.CrudRepository;

public interface UserDao extends PagingAndSortingRepository<UserEntity, String> {
//CrudRepository<UserEntity, String>
	
}
