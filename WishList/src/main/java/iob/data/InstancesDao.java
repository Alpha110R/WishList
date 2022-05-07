package iob.data;



import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

public interface InstancesDao extends PagingAndSortingRepository<InstanceEntity, String>{
	
	
	public List<InstanceEntity> findAllByName(
			@Param("name") String name,Pageable pageable);

	
	public List<InstanceEntity> findAllByNameAndActive( 
			@Param("name") String name,@Param("active") boolean active , Pageable pageable);


	public List<InstanceEntity> findAllByType(@Param("type") String type, Pageable pageable);

	
	public List<InstanceEntity> findAllByTypeAndActive(@Param("type") String type,@Param("active") boolean active , Pageable pageable);


	
	
		
	
}			

