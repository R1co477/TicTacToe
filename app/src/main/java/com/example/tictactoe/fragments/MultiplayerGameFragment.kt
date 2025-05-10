package com.example.tictactoe.fragments

import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.tictactoe.App
import com.example.tictactoe.R
import com.example.tictactoe.ai.Mark
import com.example.tictactoe.contract.navigator
import com.example.tictactoe.customViews.GameState
import com.example.tictactoe.data.EntityCard
import com.example.tictactoe.data.MakeMoveRequest
import com.example.tictactoe.data.Profile
import com.example.tictactoe.data.ResultGame
import com.example.tictactoe.data.RoomRepository
import com.example.tictactoe.databinding.FragmentLocalMultiplayerBinding
import com.example.tictactoe.utils.AvatarManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.absoluteValue
import kotlin.random.Random

class MultiplayerGameFragment : Fragment() {
    private lateinit var binding: FragmentLocalMultiplayerBinding
    private lateinit var roomRepository: RoomRepository
    private lateinit var profile: Profile

    private var humanMove: Boolean = false
        set(value) {
            binding.humanEntityCard.active = value
            binding.opponentEntityCard.active = !value
            field = value
        }

    private var refreshJob: Job? = null
    private val refreshIntervalMs = 1_000L

    private lateinit var roomName: String
    private lateinit var secondPlayer: String
    private lateinit var createdBy: String

    private lateinit var humanMark: Mark
    private lateinit var opponentMark: Mark
    private lateinit var opponentProfile: Profile

    private var shouldExitGame = false

    private val appLifecycleObserver = LifecycleEventObserver { _, event ->
        if (event == Lifecycle.Event.ON_STOP && isAdded) {
            deleteRoomOnServer()
            shouldExitGame = true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        roomRepository = RoomRepository()
        profile = (requireContext().applicationContext as App).profile

        ProcessLifecycleOwner.get().lifecycle.addObserver(appLifecycleObserver)

        requireActivity().onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    deleteRoomAndShowWin()
                }
            })
    }

    override fun onDestroy() {
        super.onDestroy()
        ProcessLifecycleOwner.get().lifecycle.removeObserver(appLifecycleObserver)
    }

    override fun onResume() {
        super.onResume()
        if (shouldExitGame && isAdded) {
            parentFragmentManager.popBackStack()
            shouldExitGame = false
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLocalMultiplayerBinding.inflate(inflater, container, false)

        roomName = arguments?.getString(KEY_ROOM_NAME).orEmpty()
        secondPlayer = arguments?.getString(KEY_SECOND_PLAYER).orEmpty()
        createdBy = arguments?.getString(KEY_CREATED_BY).orEmpty()

        setupProfile()
        setupOpponentProfile()
        determinateMove()
        determinateMarks()
        setupBoard()
        return binding.root
    }

    private fun setupProfile() {
        binding.humanEntityCard.apply {
            name = profile.nickname
            AvatarManager(profile).setAvatar(ivAvatar)
        }
    }

    private fun setupOpponentProfile() {
        val randomColor = Color.rgb(
            Random.nextInt(256),
            Random.nextInt(256),
            Random.nextInt(256)
        )
        val csList = ColorStateList.valueOf(randomColor)

        binding.opponentEntityCard.apply {
            if (secondPlayer == profile.nickname) {
                name = createdBy
                opponentProfile = Profile(createdBy, null, csList)
            } else {
                name = secondPlayer
                opponentProfile = Profile(secondPlayer, null, csList)
            }
            avatarBitmap = AvatarManager(opponentProfile).createTextBitmap()
        }
    }

    private fun determinateMove() {
        val creatorStarts = roomName.hashCode().absoluteValue % 2 == 0
        val isCreator = createdBy == profile.nickname
        humanMove = if (creatorStarts) isCreator else !isCreator
    }

    private fun determinateMarks() {
        val creatorsIsTac = roomName.hashCode().absoluteValue % 3 == 0
        val (cMarkD, cMark) = if (creatorsIsTac) R.drawable.tac to Mark.TAC else R.drawable.tic to Mark.TIC
        val (oMarkD, oMark) = if (creatorsIsTac) R.drawable.tic to Mark.TIC else R.drawable.tac to Mark.TAC

        if (createdBy == profile.nickname) {
            binding.humanEntityCard.mark = cMarkD
            binding.opponentEntityCard.mark = oMarkD
            humanMark = cMark; opponentMark = oMark
        } else {
            binding.humanEntityCard.mark = oMarkD
            binding.opponentEntityCard.mark = cMarkD
            humanMark = oMark; opponentMark = cMark
        }
    }

    private fun setupBoard() {
        binding.cvBoard.humanMark =
            if (humanMark == Mark.TIC) R.drawable.cell_tic else R.drawable.cell_tac

        binding.cvBoard.addMoveListener { r, c ->
            if (!humanMove) return@addMoveListener
            binding.cvBoard.setMove(r, c, humanMark)
            lifecycleScope.launch {
                roomRepository.makeMoveRequest(
                    MakeMoveRequest(
                        roomName,
                        createdBy,
                        boardToString()
                    )
                )
            }
            humanMove = false
        }

        binding.cvBoard.addGameStateListener { state ->
            binding.humanEntityCard.active = false
            binding.opponentEntityCard.active = false
            deleteRoomOnServer()
            showGameOverScreen(state)
        }
    }

    override fun onStart() {
        super.onStart()
        refreshJob = lifecycleScope.launch {
            while (isActive) {
                delay(refreshIntervalMs)

                roomRepository.getAllRooms().onSuccess { rooms ->
                    if (rooms.none { it.name == roomName }) {
                        deleteRoomOnServer()
                        showGameOverScreen(GameState.HUMAN_WIN)
                        cancel()
                        return@onSuccess
                    }
                }

                roomRepository.getAllGames().onSuccess { games ->
                    val game = games.filter { it.roomName == roomName }.maxByOrNull { it.id }
                    game?.let {
                        val board = boardToString()
                        for (i in 0 until 9) {
                            if (board[i] != it.board[i] && board[i] == '-') {
                                val (r, c) = convertTo2D(i)
                                binding.cvBoard.setMove(r, c, opponentMark)
                                humanMove = true
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        refreshJob?.cancel()
    }

    private fun deleteRoomOnServer() {
        lifecycleScope.launch {
            roomRepository.deleteRoom(roomName)
        }
    }

    private fun deleteRoomAndShowWin() {
        deleteRoomOnServer()
        showGameOverScreen(GameState.HUMAN_WIN)
    }

    private fun showGameOverScreen(state: GameState) {
        val result = when (state) {
            GameState.HUMAN_WIN -> getString(R.string.you_win)
            GameState.AI_WIN -> getString(R.string.you_lose)
            GameState.DRAW -> getString(R.string.draw)
            GameState.ONGOING -> return
        }

        lifecycleScope.launch {
            val humanAvatar = getHumanAvatar()
            val opponentAvatar = getOpponentAvatar()

            val humanCard = EntityCard(
                name = profile.nickname,
                description = "You",
                mark = if (humanMark == Mark.TIC) R.drawable.tic else R.drawable.tac,
                avatar = humanAvatar
            )
            val opponentCard = EntityCard(
                name = opponentProfile.nickname,
                description = "Opponent",
                mark = if (opponentMark == Mark.TIC) R.drawable.tic else R.drawable.tac,
                avatar = opponentAvatar
            )
            val resultGame = ResultGame(humanAvatar, result)

            navigator().showGameOverScreen(humanCard, opponentCard, resultGame) {
                val fm = requireActivity().supportFragmentManager

                if (fm.backStackEntryCount > 0) {
                    val firstEntry = fm.getBackStackEntryAt(0)
                    fm.popBackStack(firstEntry.id, 0)
                }
            }
        }
    }

    private suspend fun getHumanAvatar(): Bitmap? = withContext(Dispatchers.IO) {
        profile.avatarUri?.let {
            try {
                Glide.with(requireContext()).asBitmap().circleCrop().load(it).submit().get()
            } catch (_: Exception) {
                AvatarManager(profile).createTextBitmap()
            }
        } ?: AvatarManager(profile).createTextBitmap()
    }

    private suspend fun getOpponentAvatar(): Bitmap = withContext(Dispatchers.IO) {
        try {
            AvatarManager(opponentProfile).createTextBitmap()
        } catch (_: Exception) {
            BitmapFactory.decodeResource(resources, R.drawable.profile_avatar)
        }
    }

    private fun convertTo2D(idx: Int): Pair<Int, Int> = idx / 3 to idx % 3

    private fun boardToString(): String {
        val bs = binding.cvBoard.getBoardState()
        return buildString {
            for (r in 0..2) for (c in 0..2) {
                append(
                    when (bs[r][c]) {
                        1 -> CHAR_O
                        -1 -> CHAR_X
                        else -> CHAR_EMPTY
                    }
                )
            }
        }
    }

    companion object {
        private const val CHAR_X = 'X'
        private const val CHAR_O = 'O'
        private const val CHAR_EMPTY = '-'

        const val KEY_ROOM_NAME = "ROOM_NAME"
        const val KEY_SECOND_PLAYER = "SECOND_PLAYER"
        const val KEY_CREATED_BY = "CREATED_BY"

        fun newInstance(roomName: String, secondPlayer: String, createdBy: String) =
            MultiplayerGameFragment().apply {
                arguments = bundleOf(
                    KEY_ROOM_NAME to roomName,
                    KEY_SECOND_PLAYER to secondPlayer,
                    KEY_CREATED_BY to createdBy
                )
            }
    }
}
