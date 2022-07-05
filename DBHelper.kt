package com.example.chap17_mp3project

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import java.sql.SQLException

//데이터베이스명은 DBHelper() 결정 됨
//데이터베이스 파일이 있으면 onCreate() 부르지 않음
class DBHelper(context: Context, dbName: String, version: Int) :
    SQLiteOpenHelper(context, dbName, null, version) {
    companion object {
    }

    //DBHelper 생성시 최초 한번만 실행 : DB 명 부여 시점
    //테이블 설계
    override fun onCreate(db: SQLiteDatabase?) {
        //테이블 설계
        val createQuery = """
            create table musicTBL(id TEXT primary key, title TEXT, artist TEXT, albumId TEXT, duration INTEGER, likes INTEGER)
        """.trimIndent()
        db?.execSQL(createQuery)
    }

    //DB 최초 생성 후 버전 변경시 실행
    //테이블 제거
    override fun onUpgrade(db: SQLiteDatabase?, p1: Int, p2: Int) {
        val dropQuery = """
            drop table musicTBL
        """.trimIndent()
        db?.execSQL(dropQuery)
        this.onCreate(db)
    }

    //삽입
    fun insertMusic(music: Music): Boolean {
        var insertFlag = false
        val insertQuery = """
            insert into musicTBL(id , title , artist , albumId , duration, likes) 
            values('${music.id}','${music.title}','${music.artist}','${music.albumId}',${music.duration},'${music.likes}')
        """.trimIndent()
        val db = this.writableDatabase
        try {
            db.execSQL(insertQuery)
            insertFlag = true
        } catch (e: SQLException) {
            Log.d("kim", "${e.printStackTrace()}")
        } finally {
            db.close()
        }

        return insertFlag
    }

    //선택
    fun selectMusicAll(): MutableList<Music>? {
        var musicList: MutableList<Music>? = mutableListOf<Music>()

        val selectQuery = """
            select * from musicTBL
        """.trimIndent()

        val db = this.readableDatabase
        var cursor: Cursor? = null

        try {
            cursor = db.rawQuery(selectQuery, null)
            if (cursor.count > 0) {
                while (cursor.moveToNext()) {
                    val id = cursor.getString(0)
                    val title = cursor.getString(1)
                    val artist = cursor.getString(2)
                    val albumId = cursor.getString(3)
                    val duration = cursor.getInt(4)
                    val likes = cursor.getInt(5)
                    val music = Music(id, title, artist, albumId, duration, likes)
                    musicList?.add(music)
                }
            } else {
                musicList = null
            }
        } catch (e: Exception) {
            Log.d("kim", "${e.printStackTrace()}")
            musicList = null
        } finally {
            cursor?.close()
            db.close()
        }
        return musicList
    }

    //선택 : 조건에 맞는 선택
    fun selectMusic(id: String?): Music? {
        var music: Music? = null
        val selectQuery = """
            select * from musicTBL where id = '${id}'
        """.trimIndent()
        val db = this.readableDatabase
        var cursor: Cursor? = null

        cursor = db.rawQuery(selectQuery, null)

        try {
            if (cursor.count > 0) {
                if (cursor.moveToNext()) {
                    val id = cursor.getString(0)
                    val title = cursor.getString(1)
                    val artist = cursor.getString(2)
                    val albumId = cursor.getString(3)
                    val duration = cursor.getInt(4)
                    val likes = cursor.getInt(5)
                    music = Music(id, title, artist, albumId, duration, likes)
                }
            }
        }catch (e:Exception){
            Log.d("kim", "${e.printStackTrace()}")
            music = null
        }finally {
            cursor.close()
            db.close()
        }
        return music
    }

    //노래 즐겨찾기 db 구현 (좋아요 선택)
    fun updateLike(music: Music): Boolean {

        var flag = false
        val updateQuery = """
            update musicTBL set likes = ${music.likes} where id = '${music.id}'
        """.trimIndent()
        val db = this.writableDatabase

        try {
            db.execSQL(updateQuery)
            flag = true
        }catch (e:Exception){
            Log.d("kim", "${e.printStackTrace()}")
        }finally {
            db.close()
        }
        return flag
    }

    fun selectLike(): MutableList<Music>? {
        var musicList : MutableList<Music> = mutableListOf()

        val searchQuery = """
            select * from musicTBL where likes = 1
        """.trimIndent()
        val db = this.readableDatabase
        var cursor: Cursor? = null

        cursor = db.rawQuery(searchQuery, null)

        try {
            if (cursor.count > 0) {
                while (cursor.moveToNext()) {
                    val id = cursor.getString(0)
                    val title = cursor.getString(1)
                    val artist = cursor.getString(2)
                    val albumId = cursor.getString(3)
                    val duration = cursor.getInt(4)
                    val likes = cursor.getInt(5)

                    musicList?.add(Music(id, title, artist, albumId, duration, likes))
                }
            }
        }catch (e:Exception){
            Log.d("kim", "${e.printStackTrace()}")
        }finally {
            cursor.close()
            db.close()
        }
        return musicList
    }

    //노래 검색해서 해당되는 정보찾아 리턴하는 함수
    fun searchMusic(query: String?): MutableList<Music>? {
        var musicList : MutableList<Music> = mutableListOf()

        val searchQuery = """
            select * from musicTBL where artist like '$query%' or title like '$query%'
        """.trimIndent()
        val db = this.readableDatabase
        var cursor: Cursor? = null
        cursor = db.rawQuery(searchQuery, null)

        try {
            if (cursor.count > 0) {
                while (cursor.moveToNext()) {
                    val id = cursor.getString(0)
                    val title = cursor.getString(1)
                    val artist = cursor.getString(2)
                    val albumId = cursor.getString(3)
                    val duration = cursor.getInt(4)
                    val likes = cursor.getInt(5)
                    musicList?.add(Music(id, title, artist, albumId, duration, likes))
                }
            }
        }catch (e:Exception){
            Log.d("kim", "${e.printStackTrace()}")
        }finally {
            cursor.close()
            db.close()
        }
        return musicList
    }
}