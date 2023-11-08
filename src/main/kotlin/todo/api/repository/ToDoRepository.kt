package todo.api.repository

import todo.api.entities.ToDo
import todo.api.entities.ToDoDraft

interface ToDoRepository {

    fun getAllToDos(): List<ToDo>

    fun getToDo(id: Int): ToDo?

    fun addTodo(draft: ToDoDraft): ToDo

    fun removeTodo(id: Int): Boolean

    fun updateTodo(id: Int, draft: ToDoDraft): Boolean

}