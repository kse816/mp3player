package com.example.chap17_mp3project

import android.graphics.Bitmap
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.View
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import com.example.chap17_mp3project.databinding.ActivityPlaymusicBinding
import kotlinx.coroutines.*
import java.text.SimpleDateFormat

class PlaymusicActivity : AppCompatActivity() {
    lateinit var binding: ActivityPlaymusicBinding

    //뮤직 플레이어 변수
    private var mediaPlayer: MediaPlayer? = null

    //음악 정보객체 변수
    private var music: Music? = null
    private var playList: ArrayList<Parcelable>? = null
    private var position: Int = 0
    private val PREVIOUS = 0
    private val NEXT = 1

    //음악앨범 이미지 사이즈
    private val ALBUM_IMAGE_SIZE = 200

    //coroutine scope launch
    private var playerJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlaymusicBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //music = intent.getSerializableExtra("music") as Music
        playList = intent.getParcelableArrayListExtra("playList")
        position = intent.getIntExtra("position", 0)
        music = playList?.get(position) as Music

        startMusic(music)
    }

    fun startMusic(music: Music?){
        if (music != null) {
            binding.tvTitle.text = music?.title
            binding.tvArtist.text = music?.artist
            binding.tvDurationStart.text = "00:00"
            binding.tvDurationEnd.text = SimpleDateFormat("mm:ss").format(music?.duration)
//            binding.ivLike.setImageResource(R.drawable.empty_like)
            val bitmap: Bitmap? = music?.getAlbumImage(this, ALBUM_IMAGE_SIZE)
            if (bitmap != null) {
                binding.ivAlbumArt.setImageBitmap(bitmap)

            } else {
                binding.ivAlbumArt.setImageResource(R.drawable.music_note_24)
            }

            //음원 생성 및 실행
            mediaPlayer = MediaPlayer.create(this, music?.getMusicUri())
            binding.seekBar.max = music?.duration!!.toInt()

            //seekbar event : 노래 시크바가 같이 동기화
            binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {

                //시크바를 터치하고 이동할 때 발생되는 이벤트 (fromUser : user에 의한 터치 확인)
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    if (fromUser) {
                        mediaPlayer?.seekTo(progress)
                    }
                }

                //시크바 터치하는 순간 이벤트 발생
                override fun onStartTrackingTouch(p0: SeekBar?) {
                }

                //시크바 터치 후 손 떼는 순간
                override fun onStopTrackingTouch(p0: SeekBar?) {
                }

            })

        }
    }

    fun onClick(view: View?) {
        when (view?.id) {
            R.id.ivMain -> {
                //음악 정지, 코루틴 취소, 음악 객체 해제, 음악객체 = null
                mediaPlayer?.stop()
                playerJob?.cancel()
                finish()
            }
            R.id.ivPlay -> {
                if (mediaPlayer?.isPlaying == true) {
                    binding.ivPlay.setImageResource(R.drawable.play_arrow_24)
                    //binding.seekBar.progress = mediaPlayer?.currentPosition!!
                    mediaPlayer?.pause()
                } else {
                    mediaPlayer?.start()
                    binding.ivPlay.setImageResource(R.drawable.pause_24)

                    //음악 시작 (시크바, 진행시간 코루틴으로 진행)
                    val backgroundScope = CoroutineScope(Dispatchers.Default + Job())

                    playerJob = backgroundScope.launch {
                        //노래 진행 사항을 시크바와 시작진행시간 값에 넣어주기
                        //사용자가 만든 스레드에서 화면에 뷰 값을 변경하게 되면 오류 발생
                        //해결방법 : 스레드 안에서 뷰 값을 변경하고 싶으면 runOnUiThread{} 사용

                        while (mediaPlayer?.isPlaying == true) {
                            //노래 진행 위치를 시크바에 적용
                            //*******중요
                            runOnUiThread {
                                var currentPosition = mediaPlayer?.currentPosition!!
                                binding.seekBar.progress = currentPosition
                                binding.tvDurationStart.text =
                                    SimpleDateFormat("mm:ss").format(currentPosition)
                            }
                            try {
                                delay(500)
                            } catch (e: Exception) {
                                Log.d("kim", "delay error : ${e.toString()}")
                            }
                        }
//                        binding.seekBar.progress = mediaPlayer?.currentPosition!!
//                        binding.ivPlay.setImageResource(R.drawable.play_arrow_24)

                        runOnUiThread {
                            if (mediaPlayer!!.currentPosition >= binding.seekBar.max - 1000) {
                                binding.seekBar.progress = 0
                                binding.tvDurationStart.text = "00:00"
                            }
                            binding.ivPlay.setImageResource(R.drawable.play_arrow_24)
                        }
                    }
                }
            }
            R.id.ivStop -> {
                mediaPlayer?.stop()
                playerJob?.cancel()
                mediaPlayer = MediaPlayer.create(this, music?.getMusicUri())
                binding.seekBar.progress = 0
                binding.tvDurationStart.text = "00:00"
                binding.ivPlay.setImageResource(R.drawable.play_arrow_24)
            }

            R.id.ivPrevious -> {
                //음악 정지, 코루틴 취소, 음악 객체 해제, 음악객체 = null
                mediaPlayer?.stop()
                playerJob?.cancel()

                position = getPosition(PREVIOUS, position)
                music = playList?.get(position) as Music
                startMusic(music)
            }
            R.id.ivNext -> {
                //음악 정지, 코루틴 취소, 음악 객체 해제, 음악객체 = null
                mediaPlayer?.stop()
                playerJob?.cancel()

                position = getPosition(NEXT, position)
                music = playList?.get(position) as Music
                startMusic(music)
            }
        }

    }

    override fun onStop() {
        mediaPlayer?.stop()
        playerJob?.cancel()
        super.onStop()
    }

    fun getPosition(option: Int, position: Int): Int {
        var newPosition: Int = position
        when (position) {
            0 -> {
                newPosition = if (option == PREVIOUS) playList!!.size - 1 else position + 1
            }
            in 1 until (playList!!.size - 2) -> {
                newPosition = if (option == PREVIOUS) position - 1 else position + 1
            }
            playList!!.size - 1 -> {
                newPosition = if (option == PREVIOUS) position - 1 else 0
            }
        }
        return newPosition
    }

}