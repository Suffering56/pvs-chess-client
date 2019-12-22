package com.example.chess

import com.example.chess.shared.dto.GameDTO
import com.example.chess.shared.enums.GameMode
import com.example.chess.shared.enums.Side
import java.io.Serializable

class GameState(
    val userId: String,
    val isConstructor: Boolean
) : Serializable {

    constructor(userId: String, joinedGame: GameDTO) : this(userId, false) {
        id = joinedGame.id
        mode = joinedGame.mode
        side = joinedGame.side
        freeSide = joinedGame.freeSide
    }

    var id: Long? = null
    var mode: GameMode = GameMode.UNSELECTED
    var side: Side? = null
    var freeSide: Side? = null

    val isNew get(): Boolean = id == null

    fun getFreeSideSlots(): List<Side> {
        require(side == null) {
            "side must be null for getting free slots"
        }

        if (isNew) {
            return listOf(Side.WHITE, Side.BLACK)
        }

        return freeSide?.let { listOf(it) } ?: emptyList()
    }
}