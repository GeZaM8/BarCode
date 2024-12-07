package com.barjek.barcode.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.barjek.barcode.model.User


class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "UserDB"
        private const val DATABASE_VERSION = 1
        private const val TABLE_USER = "user"

        private const val COLUMN_ID = "id"
        private const val COLUMN_EMAIL = "email"
        private const val COLUMN_PASSWORD = "password"
        private const val COLUMN_NAMA = "nama"
        private const val COLUMN_KELAS = "kelas"
        private const val COLUMN_JURUSAN = "jurusan"
        private const val COLUMN_NIS = "nis"
        private const val COLUMN_NISN = "nisn"

        private const val TABLE_HADIR = "hadir"
        private const val COLUMN_HADIR_ID = "hadir_id"
        private const val COLUMN_USER_ID = "user_id"
        private const val COLUMN_DATE = "date"
        private const val COLUMN_STATUS = "status"
        private const val COLUMN_TIMESTAMP = "timestamp"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = """
            CREATE TABLE $TABLE_USER (
                $COLUMN_ID TEXT PRIMARY KEY,
                $COLUMN_EMAIL TEXT UNIQUE,
                $COLUMN_PASSWORD TEXT,
                $COLUMN_NAMA TEXT,
                $COLUMN_KELAS TEXT,
                $COLUMN_JURUSAN TEXT,
                $COLUMN_NIS TEXT,
                $COLUMN_NISN TEXT
            )
        """.trimIndent()

        db.execSQL(createTable)

        val createTableHadir = """
            CREATE TABLE $TABLE_HADIR (
                $COLUMN_HADIR_ID TEXT PRIMARY KEY,
                $COLUMN_USER_ID TEXT,
                $COLUMN_DATE TEXT,
                $COLUMN_STATUS TEXT,
                $COLUMN_TIMESTAMP TEXT,
                FOREIGN KEY($COLUMN_USER_ID) REFERENCES $TABLE_USER($COLUMN_ID)
            )
        """.trimIndent()

        db.execSQL(createTableHadir)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USER")
        onCreate(db)
    }

    fun insertUser(user: User): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_ID, user.id)
            put(COLUMN_EMAIL, user.email)
            put(COLUMN_PASSWORD, user.password)
            put(COLUMN_NAMA, user.nama)
            put(COLUMN_KELAS, user.kelas)
            put(COLUMN_JURUSAN, user.jurusan)
            put(COLUMN_NIS, user.nis)
            put(COLUMN_NISN, user.nisn)
        }
        return db.insert(TABLE_USER, null, values)
    }

    fun checkUser(email: String, password: String): Boolean {
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_USER,
            arrayOf(COLUMN_ID),
            "$COLUMN_EMAIL = ? AND $COLUMN_PASSWORD = ?",
            arrayOf(email, password),
            null, null, null
        )
        val result = cursor.count > 0
        cursor.close()
        return result
    }

    fun getUserByEmail(email: String): User? {
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_USER,
            null,
            "$COLUMN_EMAIL = ?",
            arrayOf(email),
            null, null, null
        )

        return if (cursor.moveToFirst()) {
            val user = User(
                id = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                email = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL)),
                password = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PASSWORD)),
                nama = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAMA)),
                kelas = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_KELAS)),
                jurusan = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_JURUSAN)),
                nis = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NIS)),
                nisn = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NISN))
            )
            cursor.close()
            user
        } else {
            cursor.close()
            null
        }
    }

    fun updateUser(user: User): Int {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_EMAIL, user.email)
            put(COLUMN_PASSWORD, user.password)
            put(COLUMN_NAMA, user.nama)
            put(COLUMN_KELAS, user.kelas)
            put(COLUMN_JURUSAN, user.jurusan)
            put(COLUMN_NIS, user.nis)
            put(COLUMN_NISN, user.nisn)
        }
        return db.update(TABLE_USER, values, "$COLUMN_ID = ?", arrayOf(user.id))
    }

    fun deleteUser(id: String): Int {
        val db = this.writableDatabase
        return db.delete(TABLE_USER, "$COLUMN_ID = ?", arrayOf(id))
    }

    fun getUsersByKelas(kelas: String): List<User> {
        val userList = mutableListOf<User>()
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_USER,
            null,
            "$COLUMN_KELAS = ?",
            arrayOf(kelas),
            null, null,
            "$COLUMN_NAMA ASC"
        )

        if (cursor.moveToFirst()) {
            do {
                val user = User(
                    id = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                    email = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL)),
                    password = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PASSWORD)),
                    nama = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAMA)),
                    kelas = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_KELAS)),
                    jurusan = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_JURUSAN)),
                    nis = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NIS)),
                    nisn = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NISN))
                )
                userList.add(user)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return userList
    }
}