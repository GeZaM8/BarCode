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
        private const val COLUMN_NIS = "nis"
        private const val COLUMN_NISN = "nisn"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = """
            CREATE TABLE $TABLE_USER (
                $COLUMN_ID TEXT PRIMARY KEY,
                $COLUMN_EMAIL TEXT UNIQUE,
                $COLUMN_PASSWORD TEXT,
                $COLUMN_NAMA TEXT,
                $COLUMN_KELAS TEXT,
                $COLUMN_NIS TEXT,
                $COLUMN_NISN TEXT
            )
        """.trimIndent()

        db.execSQL(createTable)
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
}