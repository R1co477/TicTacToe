package com.example.tictactoe.screens

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import com.example.tictactoe.Profile
import com.example.tictactoe.R
import com.example.tictactoe.Settings
import com.example.tictactoe.ai.Board
import com.example.tictactoe.ai.Mark
import com.example.tictactoe.ai.Minimax
import com.example.tictactoe.contract.HasCustomTitle
import com.example.tictactoe.databinding.FragmentBotGameBinding
import com.example.tictactoe.extensions.getObject
import com.example.tictactoe.utils.AvatarManager
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
        // remove listener
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

            minimax = Minimax(board, computerMark, levelDifficulty * 4)
            cvBoard.addListener { r, c ->
                board[r, c] = humanMark
                computerTurn()
            }

            cvBoard.humanMark =
                if (humanMark == Mark.TIC) R.drawable.cell_tic else R.drawable.cell_tac
        }
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
    }

    private fun computerTurn() {
        binding.humanEntityCard.active = false
        val (r, c) = minimax.findBestMove()
        board[r, c] = computerMark
        binding.cvBoard.setMove(r, c, computerMark)
        humanTurn()
    }

    private fun setImage(@DrawableRes imageResId: Int) {
        binding.botEntityCard.avatarResId = imageResId
    }

    private fun setInfo(@StringRes stringResId: Int) {
        binding.botEntityCard.description = getString(stringResId)
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
