package com.example.tictactoe.screens

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.tictactoe.EntityCard
import com.example.tictactoe.GameState
import com.example.tictactoe.Profile
import com.example.tictactoe.R
import com.example.tictactoe.ResultGame
import com.example.tictactoe.Settings
import com.example.tictactoe.ai.Board
import com.example.tictactoe.ai.Mark
import com.example.tictactoe.ai.Minimax
import com.example.tictactoe.contract.HasCustomTitle
import com.example.tictactoe.contract.navigator
import com.example.tictactoe.databinding.FragmentBotGameBinding
import com.example.tictactoe.extensions.getObject
import com.example.tictactoe.utils.AvatarManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.properties.Delegates
import kotlin.random.Random

class BotGameFragment : Fragment(), HasCustomTitle {

    private lateinit var binding: FragmentBotGameBinding
    private var levelDifficulty: Int by Delegates.notNull()
    private lateinit var profile: Profile
    private lateinit var settings: Settings
    private lateinit var sharedPref: SharedPreferences
    private lateinit var minimax: Minimax
    private val board: Board = Board.empty()
    private lateinit var humanMark: Mark
    private lateinit var computerMark: Mark
    private var isComputerThinking = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPref = requireContext().getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE)
        profile = sharedPref.getObject(KEY_PROFILE, Profile.default(requireContext()))
        settings = sharedPref.getObject(KEY_SETTINGS, Settings())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentBotGameBinding.inflate(inflater, container, false)
        levelDifficulty = arguments?.getInt(ARG_LEVEL) ?: 1
        setupBotAppearance()
        setupProfile()
        setupSettings()
        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        isComputerThinking = false
    }

    private fun setupBotAppearance() {
        val (imageRes, infoRes) = when (levelDifficulty) {
            1 -> R.drawable.easy_bot to R.string.easy_bot
            2 -> R.drawable.medium_bot to R.string.medium_bot
            3 -> R.drawable.difficult_bot to R.string.difficult_bot
            else -> R.drawable.easy_bot to R.string.easy_bot
        }
        setImage(imageRes)
        setInfo(infoRes)
    }

    private fun setupProfile() {
        binding.humanEntityCard.name = profile.nickname
        AvatarManager(profile).setAvatar(binding.humanEntityCard.ivAvatar)
    }

    private fun setupSettings() {
        humanMark = determineHumanMark()
        computerMark = if (humanMark == Mark.TIC) Mark.TAC else Mark.TIC
        setupBoard()
        startGame()
    }

    private fun determineHumanMark(): Mark {
        return when {
            settings.randomMark -> if (Random.nextBoolean()) Mark.TIC else Mark.TAC
            settings.humanTic -> Mark.TIC
            else -> Mark.TAC
        }
    }

    private fun setupBoard() {
        val tic = R.drawable.tic
        val tac = R.drawable.tac

        with(binding) {
            if (humanMark == Mark.TIC) {
                humanEntityCard.mark = tic
                botEntityCard.mark = tac
            } else {
                humanEntityCard.mark = tac
                botEntityCard.mark = tic
            }

            cvBoard.humanMark =
                if (humanMark == Mark.TIC) R.drawable.cell_tic else R.drawable.cell_tac
            minimax = Minimax(board, computerMark, levelDifficulty * 4)

            cvBoard.addMoveListener { r, c ->
                if (!isComputerThinking) {
                    board[r, c] = humanMark
                    cvBoard.setMove(r, c, humanMark)
                    computerTurn()
                }
            }

            cvBoard.addGameStateListener { state ->
                showGameOverScreen(state)
                binding.humanEntityCard.active = false
                binding.botEntityCard.active = false
            }
        }
    }

    private fun showGameOverScreen(state: GameState) {
        val result = when (state) {
            GameState.HUMAN_WIN -> getString(R.string.you_win)
            GameState.AI_WIN -> getString(R.string.you_lose)
            GameState.DRAW -> getString(R.string.draw)
            GameState.ONGOING -> return
        }

        if (profile.avatarUri != null) {
            Glide.with(this)
                .asBitmap()
                .load(profile.avatarUri)
                .circleCrop()
                .into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(
                        resource: Bitmap,
                        transition: Transition<in Bitmap>?
                    ) {
                        navigateToGameOverFragment(resource, result)
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {}
                    override fun onLoadFailed(errorDrawable: Drawable?) {
                        val avatarManager = AvatarManager(profile)
                        navigateToGameOverFragment(
                            avatarManager.createTextBitmap(requireContext()),
                            result
                        )
                    }
                })
        } else {
            val avatarManager = AvatarManager(profile)
            navigateToGameOverFragment(
                avatarManager.createTextBitmap(requireContext()),
                result
            )
        }
    }

    private fun navigateToGameOverFragment(humanAvatar: Bitmap?, result: String) {
        val botAvatar =
            BitmapFactory.decodeResource(requireContext().resources, R.drawable.easy_bot)
        val fragment = GameOverFragment.newInstance(
            EntityCard(
                name = profile.nickname,
                description = "You",
                mark = if (humanMark == Mark.TIC) R.drawable.tic else R.drawable.tac,
                avatar = humanAvatar
            ),
            EntityCard(
                name = "Bot",
                description = "Opponent",
                mark = if (computerMark == Mark.TIC) R.drawable.tic else R.drawable.tac,
                avatar = botAvatar
            ),
            ResultGame(avatarBitmap = humanAvatar, result = result)
        )

        fragment.onRefreshClick = {
            navigator().showBotGameScreen(levelDifficulty)
        }

        parentFragmentManager.beginTransaction()
            .setCustomAnimations(
                R.anim.slide_in,
                R.anim.fade_out,
                R.anim.fade_in,
                R.anim.slide_out
            )
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun startGame() {
        if (getHumanMove()) {
            humanTurn()
        } else {
            computerTurn()
        }
    }

    private fun getHumanMove(): Boolean {
        return when {
            settings.randomMove -> Random.nextBoolean()
            settings.humanMove -> true
            else -> false
        }
    }

    private fun humanTurn() {
        binding.humanEntityCard.active = true
        binding.botEntityCard.active = false
    }

    private fun computerTurn() {
        if (isComputerThinking || isBoardFull()) return
        isComputerThinking = true
        binding.humanEntityCard.active = false
        binding.botEntityCard.active = true

        lifecycleScope.launch {
            val move = withContext(Dispatchers.Default) {
                minimax.findBestMove()
            }

            if (!isComputerThinking) return@launch

            board[move.first, move.second] = computerMark
            binding.cvBoard.setMove(move.first, move.second, computerMark)
            binding.botEntityCard.active = false
            isComputerThinking = false

            if (binding.cvBoard.currentGameState == GameState.ONGOING) {
                humanTurn()
            }
        }
    }

    private fun isBoardFull(): Boolean {
        for (i in 0 until 3) {
            for (j in 0 until 3) {
                if (board[i, j] == Mark.EMPTY) {
                    return false
                }
            }
        }
        return true
    }

    private fun setImage(@DrawableRes imageResId: Int) {
        binding.botEntityCard.avatarResId = imageResId
    }

    private fun setInfo(@StringRes stringResId: Int) {
        binding.botEntityCard.name = getString(stringResId)
    }

    override fun getTitleRes(): Int = R.string.toolbar_bot

    companion object {
        private const val ARG_LEVEL = "levelDifficulty"

        fun newInstance(level: Int): BotGameFragment {
            return BotGameFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_LEVEL, level)
                }
            }
        }
    }
}