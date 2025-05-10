package com.example.tictactoe.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class RoomRepository {
    private val client = OkHttpClient()

    suspend fun getAllRooms(): Result<List<RoomResponse>> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("http://89.116.24.95:6514/api/v1/tic-tac-toe/room/getAllRooms")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext Result.failure(Exception("Server error: ${response.code}"))
                }

                val body = response.body?.string()
                    ?: return@withContext Result.failure(Exception("Empty body"))

                val rooms = Json.decodeFromString<List<RoomResponse>>(body)
                return@withContext Result.success(rooms)
            }
        } catch (e: Exception) {
            return@withContext Result.failure(e)
        }
    }

    suspend fun createRoom(room: RoomRequest): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val jsonBody = Json.encodeToString(room)
            val body = jsonBody.toRequestBody("application/json".toMediaType())

            val request = Request.Builder()
                .url("http://89.116.24.95:6514/api/v1/tic-tac-toe/room/createRoom")
                .post(body)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext Result.failure(
                        Exception("Server error: ${response.code}")
                    )
                }
                return@withContext Result.success(Unit)
            }
        } catch (e: Exception) {
            return@withContext Result.failure(e)
        }
    }

    suspend fun deleteRoom(id: ULong): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("http://89.116.24.95:6514/api/v1/tic-tac-toe/room/deleteRoom/$id")
                .delete()
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext Result.failure(
                        Exception("Server error: ${response.code}")
                    )
                }
                return@withContext Result.success(Unit)
            }
        } catch (e: Exception) {
            return@withContext Result.failure(e)
        }
    }

    suspend fun deleteRoom(name: String) : Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val allRoomsResult = getAllRooms()
            if (allRoomsResult.isFailure) {
                return@withContext Result.failure(allRoomsResult.exceptionOrNull() ?: Exception("Невдалося отримати всі кімнати"))
            }
            val rooms = allRoomsResult.getOrNull()!!
            val roomToDelete = rooms.firstOrNull { it.name == name }
                ?: return@withContext Result.failure(Exception("Room '$name' not found"))
            return@withContext deleteRoom(roomToDelete.id.toULong())
        } catch (e: Exception) {
            return@withContext Result.failure(e)
        }
    }

    suspend fun signInRoom(signInRoomRequest: SignInRoomRequest): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val jsonBody = Json.encodeToString(signInRoomRequest)
                val body = jsonBody.toRequestBody("application/json".toMediaType())
                val request = Request.Builder()
                    .url("http://89.116.24.95:6514/api/v1/tic-tac-toe/room/signInRoom")
                    .put(body)
                    .build()

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        return@withContext Result.failure(
                            Exception("Server error: ${response.code}")
                        )
                    }
                    return@withContext Result.success(Unit)
                }
            } catch (e: Exception) {
                return@withContext Result.failure(e)
            }
        }

    suspend fun makeMoveRequest(makeMoveRequest: MakeMoveRequest): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val jsonBody = Json.encodeToString(makeMoveRequest)
                val body = jsonBody.toRequestBody("application/json".toMediaType())
                val request = Request.Builder()
                    .url("http://89.116.24.95:6514/api/v1/tic-tac-toe/game/makeMove")
                    .post(body)
                    .build()

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        return@withContext Result.failure(
                            Exception("Server error: ${response.code}")
                        )
                    }
                    return@withContext Result.success(Unit)
                }
            } catch (e: Exception) {
                return@withContext Result.failure(e)
            }
        }

    suspend fun getGame(id: Long): Result<GameResponse> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("http://89.116.24.95:6514/api/v1/tic-tac-toe/game/getGameById/$id")
                .get()
                .build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext Result.failure(
                        Exception("Server error: ${response.code}")
                    )
                }
                val body = response.body?.string()
                    ?: return@withContext Result.failure(Exception("Empty body"))
                val gameResponse = Json.decodeFromString<GameResponse>(body)
                return@withContext Result.success(gameResponse)
            }
        } catch (e: Exception) {
            return@withContext Result.failure(e)
        }
    }

    suspend fun getAllGames(): Result<List<GameResponse>> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("http://89.116.24.95:6514/api/v1/tic-tac-toe/game/getAllGames")
                .get()
                .build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext Result.failure(Exception("Server error: ${response.code}"))
                }
                val body = response.body?.string()
                    ?: return@withContext Result.failure(Exception("Empty body"))
                val games = Json.decodeFromString<List<GameResponse>>(body)
                return@withContext Result.success(games)
            }
        } catch (e: Exception) {
            return@withContext Result.failure(e)
        }
    }
}