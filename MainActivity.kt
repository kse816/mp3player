package com.example.chap17_mp3project

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chap17_mp3project.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    companion object{
        val DB_NAME = "musicDB"
        val VERSION = 1
    }
    lateinit var binding: ActivityMainBinding

    //승인 받을 퍼미션 항목 요청
    val permission = arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE)
    val REQUEST_READ = 10

    //데이터베이스 객체화
    val dbHelper : DBHelper by lazy { DBHelper(this, DB_NAME, VERSION) }

    var playMusicList : MutableList<Music>? = mutableListOf<Music>()

    //리사이클러

    lateinit var musicRecyclerAdapter: MusicRecyclerAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //승인 됐으면 음악파일 가져옴, 승인 안됐으면 재요청
        if (isPermitted() == true) {
            //승인 됐으니 실행. 외부파일 가져와서 컬렉션프레임워크에 저장하고 어뎁터 부르기
            startProcess()

        } else {
            // 승인 재 요청
            // 요청 승인이 되면 콜백함수onRequestPermissionsResult로 승인 결과값 알려줌
            ActivityCompat.requestPermissions(this, permission, REQUEST_READ)
        }

    }

    //승인 요청 했을 때 승인 결과에 대한 콜백함수
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_READ) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //승인 됐으니 실행. 외부파일 가져와서 컬렉션프레임워크에 저장하고 어뎁터 부르기
                startProcess()
            } else {
                Toast.makeText(this, "권한 요청 승인 바람", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    //외부파일 읽기 승인 요청
    fun isPermitted(): Boolean {
        //승인 요청 확인
        if (ContextCompat.checkSelfPermission(
                this,
                permission[0]
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return false
        } else {
            return true
        }
    }
    private fun startProcess() {

        //데이터베이스 기능 설정
        //music 테이블에서 자료 가져올 때 자료가 있으면 리사이클러뷰 보여주고
        //music 테이블에 없으면 getMusicList 가져온 후 music 테이블에 저장해 리사이클러뷰 보여줌

        playMusicList = dbHelper.selectMusicAll()

        //테이블에 없으면
        if(playMusicList == null || playMusicList!!.size <= 0){
            //getMusicList 가져온 후
            playMusicList = getMusicList()
            //music 테이블에 저장
            for(i in 0..(playMusicList!!.size - 1)){
                val music = playMusicList!!.get(i)
                if(dbHelper.insertMusic(music) ==false){
                    Log.d("kim", "삽입 오류 : ${music.toString()}")
                }
            }
            Log.d("kim", "테이블에 없어 getMusicList()")
        }else{

            Log.d("kim", "테이블에 있어 내용 보여줌")
        }

        //리사이클러뷰 보여줌
        musicRecyclerAdapter = MusicRecyclerAdapter(this, playMusicList)
        //어뎁터 만들고 Mutablelist 제공
        binding.recyclerview.adapter = musicRecyclerAdapter

        //화면 출력
        binding.recyclerview.layoutManager = LinearLayoutManager(this)

        //데코할 곳


    }

    private fun getMusicList(): MutableList<Music> {
        //음악 정보 주소
        val listUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

        val projection = arrayOf(
            MediaStore.Audio.Media._ID, //음악 정보 아이디, class Music 에 멤버변수 id 에 넣을 정보
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DURATION


        )
        //컨텐트 리졸버 쿼리에 uri, 요청 음원 정보 컬럼 요구, 결과값 cursor로 반환
        val cursor = contentResolver.query(listUri, projection, null, null, null)
        //Music : mp3 정보 5가지 기억, mp3 파일 경로, mp3 이미지 경로, 이미지 경로를 통해 원하는 사이즈 비트맵 변경
        val musicList = mutableListOf<Music>()
        while(cursor?.moveToNext()==true){
            var id = cursor.getString(0)
            var title = cursor.getString(1).replace("'","")
            var artist = cursor.getString(2).replace("'","")
            var albumId = cursor.getString(3)
            var duration = cursor.getInt(4)
//            var likes = cursor.getInt(5)

            val music = Music(id, title, artist, albumId, duration, 0)
            musicList?.add(music)

        }
        cursor?.close()
        return musicList
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)

        val searchMenu = menu?.findItem(R.id.menu_search)
        val searchView = searchMenu?.actionView as SearchView

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(query: String?): Boolean {
                if(!query.isNullOrBlank()){
                    playMusicList = dbHelper.searchMusic(query)
                    binding.recyclerview.adapter = MusicRecyclerAdapter(this@MainActivity, playMusicList)

//                    playMusicList?.clear()
//                    dbHelper.searchMusic(query)?.let { playMusicList?.addAll(it) }
//                    musicRecyclerAdapter.notifyDataSetChanged()
                }else{
                    playMusicList?.clear()
                    dbHelper.selectMusicAll()?.let { playMusicList?.addAll(it) }
                    musicRecyclerAdapter.notifyDataSetChanged()

                }
                return true
            }
        })
        return super.onCreateOptionsMenu(menu)
    }

    @SuppressLint("ResourceAsColor")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.menu_likes -> {
                val likeMusicList: MutableList<Music>? = dbHelper.selectLike()
                binding.recyclerview.adapter = MusicRecyclerAdapter(this, likeMusicList)
            }
            R.id.main_back->{
                playMusicList = dbHelper.selectMusicAll()
                binding.recyclerview.adapter = MusicRecyclerAdapter(this, playMusicList)
            }
        }
        return super.onOptionsItemSelected(item)
    }

}
