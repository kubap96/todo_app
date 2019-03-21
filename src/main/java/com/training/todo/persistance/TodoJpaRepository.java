package com.training.todo.persistance;

import com.training.todo.domain.Todo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TodoJpaRepository extends JpaRepository<Todo, Long> {

    Page<Todo> findByName(String name, Pageable pageRequest);

    Page<Todo> findByPriority(String priority, Pageable pageRequest);

    Page<Todo> findByNameAndPriority(String name, String priority, Pageable pageRequest);

    Page<Todo> findByUsername(String login, Pageable pageable);

    Page<Todo> findByNameAndPriorityAndUsername(String name, String priority, String username, Pageable pageable);

    Page<Todo> findByNameAndUsername(String name, String username, Pageable pageable);

    Page<Todo> findByPriorityAndUsername(String priority, String username, Pageable pageable);

}
