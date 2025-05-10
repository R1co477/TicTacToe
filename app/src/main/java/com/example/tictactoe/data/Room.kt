package com.example.tictactoe.data

import kotlinx.serialization.Serializable

@Serializable
data class RoomRequest(
    val name: String,
    val is_open: Boolean,
    val password: String,
    val created_by: String,
    val second_player: String
)

@Serializable
data class RoomResponse(
    val id: Int,
    val name: String,
    val password: String,
    val createdBy: String,
    val secondPlayer: String,
    val open: Boolean
)

@Serializable
data class SignInRoomRequest(
    val name: String,
    val password: String?,
    val second_player: String
)

@Serializable
data class MakeMoveRequest(
    val room_name: String,
    val username: String,
    val board: String
)

@Serializable
data class GameResponse(
    val id: ULong,
    val roomName: String,
    val username: String,
    val board: String,
    val winner: String,
    val currentTurn: String
)