package com.example.tictactoe.fragments

import android.content.Context
import android.content.SharedPreferences
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.tictactoe.Profile
import com.example.tictactoe.R
import com.example.tictactoe.ai.Board
import com.example.tictactoe.contract.HasCustomTitle
import com.example.tictactoe.databinding.FragmentBotGameBinding
import com.example.tictactoe.extensions.getObject
import com.example.tictactoe.utils.AvatarManager
import kotlin.properties.Delegates

class BotGameFragment : Fragment(), HasCustomTitle {
    private lateinit var binding: FragmentBotGameBinding
    private var levelDifficulty: Int by Delegates.notNull<Int>()
    private lateinit var profile: Profile
    private lateinit var sharedPref: SharedPreferences
    private val board: Board = Board.empty()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPref = requireContext().getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE)
        profile = sharedPref.getObject(
            KEY_PROFILE, Profile.default(requireContext())
        )
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBotGameBinding.inflate(inflater, container, false)

        levelDifficulty = arguments?.getInt(ARG_LEVEL)!!
        when (levelDifficulty) {
            1 -> setImage(R.drawable.easy_bot).also { setInfo(R.string.easy_bot) }
            2 -> setImage(R.drawable.medium_bot).also { setInfo(R.string.medium_bot) }
            3 -> setImage(R.drawable.difficult_bot).also { setInfo(R.string.difficult_bot) }
        }
        setupProfile()
        return binding.root
    }

    private fun setupProfile() {
        val nickname = profile.nickname
        binding.playerTextview.text = nickname
        AvatarManager(profile).setAvatar(binding.playerAvatar, binding.firstLetterTextView)
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

        fun newInstance(level: Int): BotGameFragment {
            val args = Bundle()
            args.putInt(ARG_LEVEL, level)
            val fragment = BotGameFragment()
            fragment.arguments = args
            return fragment
        }
    }
}