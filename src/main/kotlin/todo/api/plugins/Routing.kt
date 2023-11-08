package todo.api.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import todo.api.authentication.JwtConfig
import todo.api.entities.LoginBody
import todo.api.entities.ToDoDraft
import todo.api.repository.InMemoryUserRepository
import todo.api.repository.MySQLTodoRepository
import todo.api.repository.ToDoRepository
import todo.api.repository.UserRepository

val jwtConfig = JwtConfig(System.getenv("KTOR_TODOLIST_JWT_SECRET"))

fun Application.configureRouting() {
    routing {

        val repository : ToDoRepository = MySQLTodoRepository()
        val userRepository: UserRepository = InMemoryUserRepository()

        post("/login"){
            val loginBody = call.receive<LoginBody>()

            val user = userRepository.getUser(loginBody.username, loginBody.password)

            if (user == null) {
                call.respond(HttpStatusCode.Unauthorized, "Invalid Credentials")
                return@post
            }

            val token = jwtConfig.generateToken(JwtConfig.JwtUser(user.userId, user.username))
            call.respond(token)
        }

        authenticate {

            get("/me"){
                val user = call.authentication.principal as JwtConfig.JwtUser
                call.respond(user)
            }

            get("/todos") {
                call.respond(repository.getAllToDos())
            }

            get("/todos/{id}"){
                val id = call.parameters["id"]?.toIntOrNull()

                if(id == null) {
                    call.respond(HttpStatusCode.BadRequest, "$id has to be a number")
                    return@get
                }
                val todo = repository.getToDo(id)

                if(todo == null){
                    call.respond(HttpStatusCode.NotFound, "$id Not Found")
                }else {
                    call.respond(todo)
                }
                call.respondText("TodoList Details for ToDO Item #$id")
            }

            post("/todos"){
                val toDoDraft = call.receive<ToDoDraft>()
                val todo = repository.addTodo(toDoDraft)
                call.respond(todo)
            }

            put("/todos/{id}"){
                val toDoDraft = call.receive<ToDoDraft>()
                val todoId = call.parameters["id"]?.toIntOrNull()

                if(todoId == null){
                    call.respond(HttpStatusCode.BadRequest,
                        "$todoId has to be a number")
                    return@put
                }

                val updated = repository.updateTodo(todoId, toDoDraft)
                if(updated){
                    call.respond(HttpStatusCode.OK)
                }
                else{
                    call.respond(HttpStatusCode.NotFound,
                        "$todoId Not Found")
                }
            }

            delete("/todos/{id}"){
                val todoId = call.parameters["id"]?.toIntOrNull()

                if(todoId == null){
                    call.respond(HttpStatusCode.BadRequest,
                        "$todoId has to be a number")
                    return@delete
                }

                val removed = repository.removeTodo(todoId)
                if (removed){
                    call.respond(HttpStatusCode.OK)
                }
                else{
                    call.respond(HttpStatusCode.NotFound,
                        "$todoId Not Found")
                }

            }
        }

    }
}
