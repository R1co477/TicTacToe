package com.example.tictactoe.data

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.tictactoe.R
import com.example.tictactoe.databinding.ItemRoomBinding
import com.example.tictactoe.utils.AvatarManager
import kotlin.random.Random

interface RoomActionListener {
    fun onRoomPlay(roomResponse: RoomResponse)
    fun onRoom(roomResponse: RoomResponse)
}

class RoomsAdapter(private val roomActionListener: RoomActionListener) : RecyclerView.Adapter<RoomsAdapter.RoomsViewHolder>(), View.OnClickListener {

    var rooms = emptyList<RoomResponse>()
        set(newValue) {
            val roomDiffCallBack = RoomDiffCallBack(field, newValue)
            val diffResult = DiffUtil.calculateDiff(roomDiffCallBack)
            field = newValue
            diffResult.dispatchUpdatesTo(this)
        }

    override fun onClick(v: View) {
        val roomResponse = v.tag as RoomResponse

        when(v.id) {
            R.id.btn_play -> {
                roomActionListener.onRoomPlay(roomResponse)
            }
            else -> {
                roomActionListener.onRoom(roomResponse)
            }
        }
    }

    override fun getItemCount(): Int = rooms.size

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RoomsViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemRoomBinding.inflate(inflater, parent, false)

        binding.root.setOnClickListener(this)
        binding.btnPlay.setOnClickListener(this)

        return RoomsViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: RoomsViewHolder,
        position: Int
    ) {
        val room = rooms[position]
        with(holder.binding) {
            holder.itemView.tag = room
            btnPlay.tag = room

           tvGame.text = room.name
           tvNickname.text = room.createdBy

            val randomColor = android.graphics.Color.rgb(
                Random.nextInt(256),
                Random.nextInt(256),
                Random.nextInt(256)
            )

            val profile = Profile(
                nickname = room.createdBy,
                avatarUri = null,
                selectedColor = ColorStateList.valueOf(randomColor)
            )
            val avatarManager = AvatarManager(profile)
            avatarManager.setAvatar(ivAvatar)

            if (room.open) {
                ivLock.setImageResource(R.drawable.ic_lock_open)
            } else {
                ivLock.setImageResource(R.drawable.ic_lock_outline)
            }
        }
    }

    class RoomsViewHolder(
        val binding: ItemRoomBinding
    ) : RecyclerView.ViewHolder(binding.root)
}

class RoomDiffCallBack(private val oldList: List<RoomResponse>, private val newList: List<RoomResponse>) : DiffUtil.Callback() {
    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(
        oldItemPosition: Int,
        newItemPosition: Int
    ): Boolean {
        val oldRoomResponse = oldList[oldItemPosition]
        val newRoomResponse = newList[newItemPosition]
        return oldRoomResponse.id == newRoomResponse.id
    }

    override fun areContentsTheSame(
        oldItemPosition: Int,
        newItemPosition: Int
    ): Boolean {
        val oldRoomResponse = oldList[oldItemPosition]
        val newRoomResponse = newList[newItemPosition]
        return oldRoomResponse == newRoomResponse
    }

}