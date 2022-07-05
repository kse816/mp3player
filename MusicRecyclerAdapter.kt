package com.example.chap17_mp3project

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.chap17_mp3project.databinding.ItemRecyclerBinding
import java.text.SimpleDateFormat

//매개변수 ( context, collectionFramework)
class MusicRecyclerAdapter(val context: Context, val musicList: MutableList<Music>?):RecyclerView.Adapter<MusicRecyclerAdapter.CustomViewHolder>() {
    var ALBUM_IMAGE_SIZE = 80

    val dbHelper:DBHelper = DBHelper(context, dbName = "musicDB", version = 1)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val binding = ItemRecyclerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CustomViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        val binding = (holder as CustomViewHolder).binding
        val music = musicList?.get(position)

        binding.tvArtist.text = music?.artist  //가수명
        binding.tvTitle.text = music?.title    //노래명
        binding.tvDuration.text = SimpleDateFormat("mm:ss").format(music?.duration)  //경과 시간
        val bitmap: Bitmap? = music?.getAlbumImage(context, ALBUM_IMAGE_SIZE)
        if(bitmap != null){
            binding.ivAlbumArt.setImageBitmap(bitmap)
        }else{
            //앨범 이미지가 없을 때 music_note_24를 넣음
            binding.ivAlbumArt.setImageResource(R.drawable.music_note_24)
        }

        //item_recycler.xml 의 ConstraintLayout 자체가 binding.root임
        //해당되는 아이템뷰를 클릭하면 PkayActivity에 musicList -> parcelable 변환, ArrayList와 해당뷰 위치값을 전송
        binding.root.setOnClickListener {
            //musicList 를 인텐트로 전달하기 위해 Parcelable ArrayList 에 저장하는 것
            val playList: ArrayList<Parcelable>? = musicList as ArrayList<Parcelable>
//            //화면 이동
            val intent = Intent(binding.root.context, PlaymusicActivity::class.java)
            //intent.putExtra("music", music)
            intent.putExtra("playList", playList)
            //위에서 음악 리스트 가져오면서 음악 순서(position)도 같이 가져옴
            intent.putExtra("position", position)
            binding.root.context.startActivity(intent)
        }

        when(music?.likes){
            0->{
                binding.ivItemLike.setImageResource(R.drawable.empty_like)
            }
            1->{
                binding.ivItemLike.setImageResource(R.drawable.full_like)
            }

        }

        binding.ivItemLike.setOnClickListener {
            if(music?.likes == 1){
                binding.ivItemLike.setImageResource(R.drawable.full_like)
                music?.likes = 0
                if (music != null) {
                    dbHelper.updateLike(music)
                }
                notifyDataSetChanged()
            }else{
                binding.ivItemLike.setImageResource(R.drawable.empty_like)
                music?.likes = 1
                if (music != null) {
                    dbHelper.updateLike(music)
                }
                notifyDataSetChanged()
            }

        }
    }
    override fun getItemCount(): Int {
        return musicList?.size?:0
    }

    //뷰홀더 내부 선언(바인딩)
    class CustomViewHolder(val binding:ItemRecyclerBinding):RecyclerView.ViewHolder(binding.root)

}