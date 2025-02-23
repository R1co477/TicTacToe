package com.example.tictactoe

class Settings {
    private var _randomMark: Boolean
    private var _randomMove: Boolean
    private var _humanMove: Boolean
    private var _humanTic: Boolean

    constructor() {
        _randomMark = true
        _randomMove = true
        _humanMove = false
        _humanTic = false
    }

    constructor(
        isRandomMarkAssignment: Boolean,
        isRandomFirstMove: Boolean,
        isHumanMoveFirst: Boolean,
        isHumanUsingTic: Boolean
    ) {
        _randomMark = isRandomMarkAssignment
        _randomMove = isRandomFirstMove
        _humanMove = isHumanMoveFirst
        _humanTic = isHumanUsingTic
    }

    var randomMark: Boolean
        get() = _randomMark
        set(value) {
            _randomMark = value
            _humanTic = false
        }

    var randomMove: Boolean
        get() = _randomMove
        set(value) {
            _randomMove = value
            _humanMove = false

        }

    var humanMove: Boolean
        get() = _humanMove
        set(value) {
            _humanMove = value
            _randomMove = false
        }

    var humanTic: Boolean
        get() = _humanTic
        set(value) {
            _humanTic = value
            _randomMark = false
        }
}