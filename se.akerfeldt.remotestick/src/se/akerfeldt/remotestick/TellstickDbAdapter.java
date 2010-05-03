package se.akerfeldt.remotestick;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.util.Log;

public class TellstickDbAdapter {
  private static final String DATABASE_NAME = "tellremote.db";
  private static final String DATABASE_TABLE = "controllers";
  private static final int DATABASE_VERSION = 1;
 
  public static final String KEY_ID = "_id";
  public static final String KEY_NAME = "name";
  public static final String KEY_URI = "uri";
  public static final String KEY_APIKEY = "apikey";
  
  private SQLiteDatabase db;
  private final Context context;
  private TellstickDbOpenHelper dbHelper;

  public TellstickDbAdapter(Context _context) {
    this.context = _context;
    dbHelper = new TellstickDbOpenHelper(context, DATABASE_NAME, 
                                    null, DATABASE_VERSION);
  }
  
  public void close() {
    db.close();
  }
  
  public void open() throws SQLiteException {  
    try {
      db = dbHelper.getWritableDatabase();
    } catch (SQLiteException ex) {
      db = dbHelper.getReadableDatabase();
    }
  }  
  
  public long insertController(String name, String uri, String apiKey) {
    ContentValues newTellstickValues = new ContentValues();
    newTellstickValues.put(KEY_NAME, name);
    newTellstickValues.put(KEY_URI, uri);
    if(apiKey != null)
    	newTellstickValues.put(KEY_APIKEY, apiKey);
    return db.insert(DATABASE_TABLE, null, newTellstickValues);
  }

  public boolean removeController(long identifier) {
    return db.delete(DATABASE_TABLE, KEY_ID + "=" + identifier, null) > 0;
  }

  public boolean updateController(long identifier, String name, String uri, String apiKey) {
    ContentValues newValue = new ContentValues();
    newValue.put(KEY_NAME, name);
    newValue.put(KEY_URI, uri);
    newValue.put(KEY_APIKEY, (apiKey != null ? apiKey : "null"));

    return db.update(DATABASE_TABLE, newValue, KEY_ID + "=" + identifier, null) > 0;
  }
  
  public Cursor getAllControllersCursor() {
    return db.query(DATABASE_TABLE, 
                    new String[] { KEY_ID, KEY_NAME, KEY_URI, KEY_APIKEY}, 
                    null, null, null, null, null);
  }

  public Cursor setCursorTellstick(long identifier) throws SQLException {
    Cursor result = db.query(true, DATABASE_TABLE, 
	                           new String[] {KEY_ID, KEY_NAME, KEY_URI, KEY_APIKEY},
                             KEY_ID + "=" + identifier, null, null, null, 
                             null, null);
    if ((result.getCount() == 0) || !result.moveToFirst()) {
      throw new SQLException("No controller found for identifier: " + identifier);
    }
    return result;
  }

  public Controller getController(long identifier) throws SQLException {
    Cursor cursor = db.query(true, DATABASE_TABLE, 
                             new String[] {KEY_ID, KEY_NAME, KEY_URI, KEY_APIKEY},
                             KEY_ID + "=" + identifier, null, null, null, 
                             null, null);
    if ((cursor.getCount() == 0) || !cursor.moveToFirst()) {
      throw new SQLException("No controller found for identifier: " + identifier);
    }
    
    String name = cursor.getString(cursor.getColumnIndex(KEY_NAME));
    String uri = cursor.getString(cursor.getColumnIndex(KEY_URI));
    String apiKey = cursor.getString(cursor.getColumnIndex(KEY_APIKEY));
		  
    Controller result = new Controller(name, uri, apiKey);
    return result;  
  }
  
  public List<Controller> getAllControllers() {
	  List<Controller> controllers = new ArrayList<Controller>();
	  Cursor cursor = getAllControllersCursor();
	  cursor.requery();
	  if(cursor.moveToFirst()) {
		    String name = cursor.getString(cursor.getColumnIndex(KEY_NAME));
		    String uri = cursor.getString(cursor.getColumnIndex(KEY_URI));
		    String apiKey = cursor.getString(cursor.getColumnIndex(KEY_APIKEY));
			controllers.add(new Controller(name, uri, apiKey));
			while(cursor.moveToNext()) {
			    name = cursor.getString(cursor.getColumnIndex(KEY_NAME));
			    uri = cursor.getString(cursor.getColumnIndex(KEY_URI));
			    apiKey = cursor.getString(cursor.getColumnIndex(KEY_APIKEY));
				controllers.add(new Controller(name, uri, apiKey));				
			}
	  }
	  return controllers;
  }
  
  private static class TellstickDbOpenHelper extends SQLiteOpenHelper {

	  public TellstickDbOpenHelper(Context context, String name,
	                          CursorFactory factory, int version) {
	    super(context, name, factory, version);
	  }

	  // SQL Statement to create a new database.
	  private static final String DATABASE_CREATE = "create table " + 
	    DATABASE_TABLE + " (" + KEY_ID + " integer primary key autoincrement, " +
	    KEY_NAME + " text not null, " + KEY_URI + " text not null, " + KEY_APIKEY + " text);";

	  @Override
	  public void onCreate(SQLiteDatabase _db) {
	    _db.execSQL(DATABASE_CREATE);
	  }

	  @Override
	  public void onUpgrade(SQLiteDatabase _db, int _oldVersion, int _newVersion) {
	    Log.w("ControllerDbAdapter", "Upgrading from version " + 
	                           _oldVersion + " to " +
	                           _newVersion + ", which will destroy all old data");

	    // Drop the old table.
	    _db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
	    // Create a new one.
	    onCreate(_db);
	  }
	}
}