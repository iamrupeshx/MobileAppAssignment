package com.smartfit.app.data.local.db;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.smartfit.app.data.model.FoodLog;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Integer;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class FoodDao_Impl implements FoodDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<FoodLog> __insertionAdapterOfFoodLog;

  private final EntityDeletionOrUpdateAdapter<FoodLog> __deletionAdapterOfFoodLog;

  private final EntityDeletionOrUpdateAdapter<FoodLog> __updateAdapterOfFoodLog;

  public FoodDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfFoodLog = new EntityInsertionAdapter<FoodLog>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `food_logs` (`id`,`userId`,`foodName`,`mealType`,`calories`,`proteinG`,`carbsG`,`fatG`,`servingSize`,`date`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final FoodLog entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getUserId());
        statement.bindString(3, entity.getFoodName());
        statement.bindString(4, entity.getMealType());
        statement.bindLong(5, entity.getCalories());
        statement.bindDouble(6, entity.getProteinG());
        statement.bindDouble(7, entity.getCarbsG());
        statement.bindDouble(8, entity.getFatG());
        statement.bindString(9, entity.getServingSize());
        statement.bindLong(10, entity.getDate());
      }
    };
    this.__deletionAdapterOfFoodLog = new EntityDeletionOrUpdateAdapter<FoodLog>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `food_logs` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final FoodLog entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__updateAdapterOfFoodLog = new EntityDeletionOrUpdateAdapter<FoodLog>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `food_logs` SET `id` = ?,`userId` = ?,`foodName` = ?,`mealType` = ?,`calories` = ?,`proteinG` = ?,`carbsG` = ?,`fatG` = ?,`servingSize` = ?,`date` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final FoodLog entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getUserId());
        statement.bindString(3, entity.getFoodName());
        statement.bindString(4, entity.getMealType());
        statement.bindLong(5, entity.getCalories());
        statement.bindDouble(6, entity.getProteinG());
        statement.bindDouble(7, entity.getCarbsG());
        statement.bindDouble(8, entity.getFatG());
        statement.bindString(9, entity.getServingSize());
        statement.bindLong(10, entity.getDate());
        statement.bindLong(11, entity.getId());
      }
    };
  }

  @Override
  public Object insert(final FoodLog foodLog, final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfFoodLog.insertAndReturnId(foodLog);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object delete(final FoodLog foodLog, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfFoodLog.handle(foodLog);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object update(final FoodLog foodLog, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfFoodLog.handle(foodLog);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<FoodLog>> getAllForUser(final int userId) {
    final String _sql = "SELECT * FROM food_logs WHERE userId = ? ORDER BY date DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, userId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"food_logs"}, new Callable<List<FoodLog>>() {
      @Override
      @NonNull
      public List<FoodLog> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "userId");
          final int _cursorIndexOfFoodName = CursorUtil.getColumnIndexOrThrow(_cursor, "foodName");
          final int _cursorIndexOfMealType = CursorUtil.getColumnIndexOrThrow(_cursor, "mealType");
          final int _cursorIndexOfCalories = CursorUtil.getColumnIndexOrThrow(_cursor, "calories");
          final int _cursorIndexOfProteinG = CursorUtil.getColumnIndexOrThrow(_cursor, "proteinG");
          final int _cursorIndexOfCarbsG = CursorUtil.getColumnIndexOrThrow(_cursor, "carbsG");
          final int _cursorIndexOfFatG = CursorUtil.getColumnIndexOrThrow(_cursor, "fatG");
          final int _cursorIndexOfServingSize = CursorUtil.getColumnIndexOrThrow(_cursor, "servingSize");
          final int _cursorIndexOfDate = CursorUtil.getColumnIndexOrThrow(_cursor, "date");
          final List<FoodLog> _result = new ArrayList<FoodLog>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final FoodLog _item;
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final int _tmpUserId;
            _tmpUserId = _cursor.getInt(_cursorIndexOfUserId);
            final String _tmpFoodName;
            _tmpFoodName = _cursor.getString(_cursorIndexOfFoodName);
            final String _tmpMealType;
            _tmpMealType = _cursor.getString(_cursorIndexOfMealType);
            final int _tmpCalories;
            _tmpCalories = _cursor.getInt(_cursorIndexOfCalories);
            final float _tmpProteinG;
            _tmpProteinG = _cursor.getFloat(_cursorIndexOfProteinG);
            final float _tmpCarbsG;
            _tmpCarbsG = _cursor.getFloat(_cursorIndexOfCarbsG);
            final float _tmpFatG;
            _tmpFatG = _cursor.getFloat(_cursorIndexOfFatG);
            final String _tmpServingSize;
            _tmpServingSize = _cursor.getString(_cursorIndexOfServingSize);
            final long _tmpDate;
            _tmpDate = _cursor.getLong(_cursorIndexOfDate);
            _item = new FoodLog(_tmpId,_tmpUserId,_tmpFoodName,_tmpMealType,_tmpCalories,_tmpProteinG,_tmpCarbsG,_tmpFatG,_tmpServingSize,_tmpDate);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<FoodLog>> getSince(final int userId, final long fromMs) {
    final String _sql = "SELECT * FROM food_logs WHERE userId = ? AND date >= ? ORDER BY date DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, userId);
    _argIndex = 2;
    _statement.bindLong(_argIndex, fromMs);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"food_logs"}, new Callable<List<FoodLog>>() {
      @Override
      @NonNull
      public List<FoodLog> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "userId");
          final int _cursorIndexOfFoodName = CursorUtil.getColumnIndexOrThrow(_cursor, "foodName");
          final int _cursorIndexOfMealType = CursorUtil.getColumnIndexOrThrow(_cursor, "mealType");
          final int _cursorIndexOfCalories = CursorUtil.getColumnIndexOrThrow(_cursor, "calories");
          final int _cursorIndexOfProteinG = CursorUtil.getColumnIndexOrThrow(_cursor, "proteinG");
          final int _cursorIndexOfCarbsG = CursorUtil.getColumnIndexOrThrow(_cursor, "carbsG");
          final int _cursorIndexOfFatG = CursorUtil.getColumnIndexOrThrow(_cursor, "fatG");
          final int _cursorIndexOfServingSize = CursorUtil.getColumnIndexOrThrow(_cursor, "servingSize");
          final int _cursorIndexOfDate = CursorUtil.getColumnIndexOrThrow(_cursor, "date");
          final List<FoodLog> _result = new ArrayList<FoodLog>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final FoodLog _item;
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final int _tmpUserId;
            _tmpUserId = _cursor.getInt(_cursorIndexOfUserId);
            final String _tmpFoodName;
            _tmpFoodName = _cursor.getString(_cursorIndexOfFoodName);
            final String _tmpMealType;
            _tmpMealType = _cursor.getString(_cursorIndexOfMealType);
            final int _tmpCalories;
            _tmpCalories = _cursor.getInt(_cursorIndexOfCalories);
            final float _tmpProteinG;
            _tmpProteinG = _cursor.getFloat(_cursorIndexOfProteinG);
            final float _tmpCarbsG;
            _tmpCarbsG = _cursor.getFloat(_cursorIndexOfCarbsG);
            final float _tmpFatG;
            _tmpFatG = _cursor.getFloat(_cursorIndexOfFatG);
            final String _tmpServingSize;
            _tmpServingSize = _cursor.getString(_cursorIndexOfServingSize);
            final long _tmpDate;
            _tmpDate = _cursor.getLong(_cursorIndexOfDate);
            _item = new FoodLog(_tmpId,_tmpUserId,_tmpFoodName,_tmpMealType,_tmpCalories,_tmpProteinG,_tmpCarbsG,_tmpFatG,_tmpServingSize,_tmpDate);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object totalCaloriesSince(final int userId, final long fromMs,
      final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT SUM(calories) FROM food_logs WHERE userId = ? AND date >= ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, userId);
    _argIndex = 2;
    _statement.bindLong(_argIndex, fromMs);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Integer>() {
      @Override
      @Nullable
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final Integer _tmp;
            if (_cursor.isNull(0)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getInt(0);
            }
            _result = _tmp;
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
