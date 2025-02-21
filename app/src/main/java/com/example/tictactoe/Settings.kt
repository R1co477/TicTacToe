package com.example.tictactoe


class Settings {
    constructor(
        isRandomMarkAssignment: Boolean = true,
        isRandomFirstMove: Boolean = true,
        isHumanMoveFirst: Boolean = false,
        isHumanUsingTic: Boolean = false
    ) {
        this.isRandomMarkAssignment = isRandomMarkAssignment
        this.isRandomFirstMove = isRandomFirstMove
        this.isHumanMoveFirst = isHumanMoveFirst
        this.isHumanUsingTic = isHumanUsingTic
    }

    var isRandomMarkAssignment: Boolean
        get() = isRandomMarkAssignment
        set(value) {
            isRandomMarkAssignment = value
            if (value) {
                isHumanUsingTic = false
            }
        }

    var isRandomFirstMove: Boolean
        get() = isRandomFirstMove
        set(value) {
            isRandomFirstMove = value
            if (value) {
                isHumanMoveFirst = false
            }
        }

    var isHumanMoveFirst: Boolean
        get() = isHumanMoveFirst
        set(value) {
            isHumanMoveFirst = value
            if (value) {
                isRandomFirstMove = false
            }
        }

    var isHumanUsingTic: Boolean
        get() = isHumanUsingTic
        set(value) {
            isHumanUsingTic = value
            if (value) {
                isRandomMarkAssignment = false
            }
        }
}
