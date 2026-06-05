package com.example.redyseguridad.network

import com.example.redyseguridad.model.Post
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

/**
 * Singleton para manejar las comunicaciones HTTP de forma nativa.
 * Usa solo HttpURLConnection sin librerías de terceros (sin Retrofit, OkHttp, Volley, etc).
 * Todos los métodos son suspend functions que corren en Dispatchers.IO
 */
object HttpClient {

    private const val BASE_URL = "https://jsonplaceholder.typicode.com"
    private const val CONNECT_TIMEOUT = 10000  // 10 segundos
    private const val READ_TIMEOUT = 10000     // 10 segundos

    /**
     * Obtiene un post específico desde JSONPlaceholder.
     * Abre una conexión GET a /posts/{id}, lee la respuesta JSON y la parsea manualmente.
     *
     * @param id El ID del post a obtener
     * @return Data class Post con los datos parseados
     * @throws Exception si el servidor devuelve un código distinto a 200
     */
    suspend fun getPost(id: Int): Post = withContext(Dispatchers.IO) {
        val urlString = "$BASE_URL/posts/$id"
        val url = URL(urlString)

        // Abrir conexión HTTP
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.connectTimeout = CONNECT_TIMEOUT
        connection.readTimeout = READ_TIMEOUT

        try {
            val responseCode = connection.responseCode

            // Verificar que la respuesta sea exitosa (200 OK)
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw Exception("Error HTTP $responseCode del servidor")
            }

            // Leer la respuesta del servidor con BufferedReader
            val inputStream = connection.inputStream
            val reader = BufferedReader(InputStreamReader(inputStream))
            val responseBody = reader.use { it.readText() }
            reader.close()

            // Parsear manualmente la respuesta JSON sin usar Gson ni Moshi
            val jsonObject = JSONObject(responseBody)
            val post = Post(
                id = jsonObject.getInt("id"),
                userId = jsonObject.getInt("userId"),
                title = jsonObject.getString("title"),
                body = jsonObject.getString("body")
            )
            post
        } finally {
            connection.disconnect()
        }
    }

    /**
     * Actualiza un post existente mediante una solicitud PUT.
     * Construye manualmente el JSON de envío, lo serializa y lo escribe en el OutputStream.
     *
     * @param id El ID del post a actualizar
     * @param title El nuevo título del post
     * @param body El nuevo cuerpo del post
     * @return El código de respuesta HTTP (usualmente 200 para éxito)
     */
    suspend fun putPost(id: Int, title: String, body: String): Int =
        withContext(Dispatchers.IO) {
            val urlString = "$BASE_URL/posts/$id"
            val url = URL(urlString)

            // Abrir conexión HTTP para PUT
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "PUT"
            connection.connectTimeout = CONNECT_TIMEOUT
            connection.readTimeout = READ_TIMEOUT

            // Configurar headers para indicar que enviaremos JSON
            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")
            connection.doOutput = true

            try {
                // Construir manualmente el JSON de envío sin librerías
                val jsonBody = """{"id":$id,"title":"$title","body":"$body","userId":1}"""

                // Escribir el JSON en el OutputStream de la conexión
                val outputStream = connection.outputStream
                outputStream.write(jsonBody.toByteArray(Charsets.UTF_8))
                outputStream.flush()
                outputStream.close()

                // Obtener y retornar el código de respuesta
                connection.responseCode
            } finally {
                connection.disconnect()
            }
        }
}

