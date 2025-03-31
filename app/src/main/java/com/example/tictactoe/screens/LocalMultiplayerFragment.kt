package com.example.tictactoe.screens

import android.content.Context
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.tictactoe.EntityCard
import com.example.tictactoe.GameState
import com.example.tictactoe.Profile
import com.example.tictactoe.R
import com.example.tictactoe.ResultGame
import com.example.tictactoe.ai.Mark
import com.example.tictactoe.contract.HasCustomTitle
import com.example.tictactoe.contract.navigator
import com.example.tictactoe.databinding.FragmentLocalMultiplayerBinding
import com.example.tictactoe.extensions.getObject
import com.example.tictactoe.utils.AvatarManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random

class LocalMultiplayerFragment : Fragment(), HasCustomTitle {
    private lateinit var binding: FragmentLocalMultiplayerBinding


    private lateinit var profile: Profile
    private lateinit var opponentProfile: Profile
    private lateinit var sharedPref: SharedPreferences
    private var humanMove = Random.nextBoolean()
    private lateinit var humanMark: Mark
    private lateinit var opponentMark: Mark

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPref = requireContext().getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE)
        profile = sharedPref.getObject(KEY_PROFILE, Profile.default(requireContext()))
        opponentProfile = Profile(
            "Opponent",
            null,
            ColorStateList.valueOf(requireContext().getColor(R.color.background_red))
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentLocalMultiplayerBinding.inflate(inflater, container, false)
        setupProfile()
        setupOpponentProfile()
        determinateMark()
        binding.humanEntityCard.mark = if (humanMark == Mark.TIC) R.drawable.tic else R.drawable.tac
        binding.opponentEntityCard.mark =
            if (opponentMark == Mark.TIC) R.drawable.tic else R.drawable.tac
        setupBoard()
        startGame()
        return binding.root
    }

    private fun setupProfile() {
        binding.humanEntityCard.name = profile.nickname
        AvatarManager(profile).setAvatar(binding.humanEntityCard.ivAvatar)
    }

    private fun setupOpponentProfile() {
        binding.opponentEntityCard.name = getString(R.string.opponent)
        AvatarManager(opponentProfile).setAvatar(binding.opponentEntityCard.ivAvatar)
    }

    private fun determinateMark() {
        if (Random.nextBoolean()) {
            humanMark = Mark.TIC
            opponentMark = Mark.TAC
        } else {
            humanMark = Mark.TAC
            opponentMark = Mark.TIC
        }
    }

    private fun setupBoard() {
        with(binding) {
            cvBoard.humanMark =
                if (humanMark == Mark.TIC) R.drawable.cell_tic else R.drawable.cell_tac
            cvBoard.addMoveListener { r, c ->
                cvBoard.setMove(r, c, if (humanMove) humanMark else opponentMark)
                humanMove = !humanMove
                if (humanMove) {
                    humanTurn()
                } else {
                    opponentTurn()
                }
            }

            cvBoard.addGameStateListener { state ->
                showGameOverScreen(state)
                binding.humanEntityCard.active = false
                binding.opponentEntityCard.active = false
            }
        }
    }

    private fun humanTurn() {
        binding.humanEntityCard.active = true
        binding.opponentEntityCard.active = false
    }

    private fun opponentTurn() {
        binding.humanEntityCard.active = false
        binding.opponentEntityCard.active = true
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

            val humanEntityCard = EntityCard(
                name = profile.nickname,
                description = "You",
                mark = if (humanMark == Mark.TIC) R.drawable.tic else R.drawable.tac,
                avatar = humanAvatar
            )

            val opponentEntityCard = EntityCard(
                name = getString(R.string.opponent),
                description = "Opponent",
                mark = if (opponentMark == Mark.TIC) R.drawable.tic else R.drawable.tac,
                avatar = opponentAvatar
            )

            val resultGame = ResultGame(avatarBitmap = humanAvatar, result = result)

            navigator().showGameOverScreen(
                humanEntityCard,
                opponentEntityCard,
                resultGame
            ) {
                navigator().showLocalMultiplayerScreen()
            }
        }
    }

    private suspend fun getHumanAvatar(): Bitmap? = withContext(Dispatchers.IO) {
        return@withContext if (profile.avatarUri != null) {
            try {
                Glide.with(requireContext())
                    .asBitmap()
                    .circleCrop()
                    .load(profile.avatarUri)
                    .submit()
                    .get()
            } catch (_: Exception) {
                AvatarManager(profile).createTextBitmap()
            }
        } else {
            AvatarManager(profile).createTextBitmap()
        }
    }

    private suspend fun getOpponentAvatar(): Bitmap = withContext(Dispatchers.IO) {
        try {
            AvatarManager(opponentProfile)
                .createTextBitmap()
        } catch (_: Exception) {
            BitmapFactory.decodeResource(resources, R.drawable.profile_avatar)
        }
    }

    private fun startGame() {
        if (humanMove) {
            humanTurn()
        } else {
            opponentTurn()
        }
    }

    override fun getTitleRes(): Int = R.string.toolbar_local_multiplayer
}