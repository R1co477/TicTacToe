package com.example.tictactoe.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.tictactoe.R
import com.example.tictactoe.contract.HasCustomTitle
import com.example.tictactoe.databinding.FragmentBotGameBinding
import kotlin.properties.Delegates

class BotGameFragment : Fragment(), HasCustomTitle {
    private lateinit var binding: FragmentBotGameBinding
    private var levelDifficulty: Int by Delegates.notNull<Int>()
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) : View {
        binding = FragmentBotGameBinding.inflate(inflater, container, false)
    val fragment = BoardFragment()
    childFragmentManager
        .beginTransaction()
        .add(R.id.fragment_board, fragment)
        .commit()

        levelDifficulty = arguments?.getInt(ARG_LEVEL)!!
        when (levelDifficulty) {
            1 -> setImage(R.drawable.easy_bot).also { setInfo(R.string.easy_bot) }
            2 -> setImage(R.drawable.medium_bot).also { setInfo(R.string.medium_bot) }
            3 -> setImage(R.drawable.difficult_bot).also { setInfo(R.string.difficult_bot) }
        }
        return binding.root
    }
    private fun setImage(@DrawableRes imageResId: Int) {
        val drawable = ContextCompat.getDrawable(requireContext(), imageResId)
        binding.botAvatar.setImageDrawable(drawable)
    }
    private fun setInfo(@StringRes stringResId: Int) {
        binding.botTextView.text = getString(stringResId)
    }

    override fun getTitleRes(): Int = R.string.toolbar_bot
    companion object {
        private const val ARG_LEVEL = "levelDifficulty"

        fun newInstance(level: Int) : BotGameFragment {
            val args = Bundle()
            args.putInt(ARG_LEVEL, level)
            val fragment = BotGameFragment()
            fragment.arguments = args
            return fragment
        }
    }
}