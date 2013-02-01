package ita.android.myfirstapp;


import ita.android.myfirstapp.FirstSurfaceView.FirstThread;

import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	// インテント用定数
	static final int REQUEST_GET_CONTENT = 0;
	static final int REQUEST_CROP_PICK = 1;

    /** 実際にアニメーションを動作するThreadのハンドル */
    private FirstThread mThread;

    /** Viewのハンドル */
    private FirstSurfaceView mView;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//
//        // タイトルバーの削除
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
//
//        // ステータスバー削除
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);

        //Viewをセットする
        LinearLayout l = new LinearLayout(this);
        setContentView(l);
        mView = new FirstSurfaceView(this);
        l.addView(mView);
        mThread = mView.getThread();

    }


    /**
     * ユーザがメニューからアイテムを選択したときに呼び出される
     *
     * @param item MenuItem:選択されたメニューエントリ
     * @return true 正当な項目の場合, false
     *         その他
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item1:
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent, REQUEST_GET_CONTENT);
                return true;
            case R.id.item2:
            	mThread.doMenu2();
                return true;
            case R.id.item3:
            	mThread.doMenu3();
                return true;
        }

        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
        case REQUEST_GET_CONTENT:
        	try {
                if (resultCode != RESULT_OK) return;

                Uri uri = data.getData(); // 選ばれた写真のUri
                Intent intent = new Intent("com.android.camera.action.CROP");
                intent.setData(uri);
                intent.putExtra("outputX", 200);
                intent.putExtra("outputY", 200);
                intent.putExtra("aspectX", 1);
                intent.putExtra("aspectY", 1);
                intent.putExtra("scale", true);
                intent.putExtra("return-data", true);
                startActivityForResult(intent, REQUEST_CROP_PICK);
        	} catch (Exception e){
        		Toast.makeText(this, "画像取得に失敗しました", Toast.LENGTH_SHORT).show();
        	}
            break;
        case REQUEST_CROP_PICK:
            if (resultCode != RESULT_OK) return;
            Bitmap bitmap = data.getExtras().getParcelable("data");
            // 取得したBitmapでごにょごにょする
            mView.setBitmap(bitmap);
            break;
        }
    }
    
    /**
     * 最初に呼び出され、アクティビティにメニューの設定をさせる
     *
     * @param menu Menu：エントリを追加するメニュー
     * @return true
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    /**
     * アクティビティがフォーカスを失ったときに呼び出される
     */
    @Override
    protected void onPause() {
        super.onPause();
        Log.d("TEST", "onPouse");
    }
}
