package ita.android.myfirstapp;


import java.lang.Thread.State;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.EditText;
import android.widget.Toast;
import org.w3c.dom.*;
import android.app.*;
import android.graphics.*;

public class  FirstSurfaceView extends SurfaceView implements SurfaceHolder.Callback {

	class FirstThread extends Thread {

		public static final int FLAMES_PAR_SECOND = 120;							// 1秒あたりのフレーム数
		public static final int MILLIS_PAR_FLAME = 1000 / FLAMES_PAR_SECOND;	// 1フレームの秒数


        //画像読み込み
        Resources res = getResources();
    	Bitmap box = BitmapFactory.decodeResource(res, R.drawable.box2);
    	Bitmap panel01 = BitmapFactory.decodeResource(res, R.drawable.panel_01);
    	Bitmap panel02 = BitmapFactory.decodeResource(res, R.drawable.panel_02);
    	Bitmap panel03 = BitmapFactory.decodeResource(res, R.drawable.panel_03);
    	Bitmap panel04 = BitmapFactory.decodeResource(res, R.drawable.panel_04);
    	Bitmap panel1 = BitmapFactory.decodeResource(res, R.drawable.panel_1);
    	Bitmap panel2 = BitmapFactory.decodeResource(res, R.drawable.panel_2);
    	Bitmap panel3 = BitmapFactory.decodeResource(res, R.drawable.panel_3);
    	Bitmap panel4 = BitmapFactory.decodeResource(res, R.drawable.panel_4);
    	Bitmap panel5 = BitmapFactory.decodeResource(res, R.drawable.panel_5);
		Bitmap b_goal = BitmapFactory.decodeResource(res, R.drawable.goal);

    	Bitmap b_charactor = BitmapFactory.decodeResource(res, R.drawable.charactor);
    	Bitmap b_charactor_dead = BitmapFactory.decodeResource(res, R.drawable.charactor_dead);

    	// dipの取得
		private float dip = res.getDisplayMetrics().scaledDensity;
	    
		public final int SizeofPanelImage = (int)(74.0 * dip);		// パネルのさいず
		public final int TopLoc = (int)(60.0 * dip);				// 第1パネルを設置する上からの距離
		public final int LeftLoc = (int)(12.0 * dip);				// 第1パネルを設置する左からの距離

		public int VelofPanel = (int)(800.0 * dip);					// パネルスライドの速度
		public int VelofChara = (int)(10.0 * dip);					// キャラクター移動速度

		
    	private SurfaceHolder holder;

        private boolean mRun = true;

		private boolean mInBackground = false;

        Paint paint = new Paint();

        /** 各回で経過時間を計算するのに使用する */
        private long mLastTime;
        /** ゲーム開始からの経過時間 */
        private long mPastTime;

        /** サウンドを鳴らしても良いか */
        private boolean soundFlg;

		private MediaPlayer mBgmPlayer;
		
		private int seId;
		private int seId2;
		private int seId3;
		private SoundPool sp;

		// あいてるとこ
        private int space_point;
        // 動いてるパネル番号
        private int moving_panel_number;

    	private boolean isDrug = false;
    	private boolean isCleared = false;


    	double dx = 0;
    	double dy = 0;

    	
    	// ゲーム状態
    	private int gameStat;

    	// パネル配列
    	RailPanel[] rpArray;

    	// キャラクター
    	RailPanel chara;

    	// 主人公がどこのパネル上にいるか
    	private int charaPos = 0;
    	
    	// カーブしている時の状態
		double curvetime;		// カーブ開始からの経過時間
		int curvestat = 9;		// カーブしているかどうか、及び、カーブに入った時の方向

    	// 着信音モードを取得
    	int ringerMode = 0;

		String tempstr;

        // コンストラクタ
        public FirstThread(SurfaceHolder surfaceHolder, Context context) {
            // 主要オブジェクトのハンドルの取得
            holder = surfaceHolder;
            mContext = context;

        	// AudioManagerオブジェクトを取得
        	AudioManager audioManager = (AudioManager)mContext.getSystemService(Context.AUDIO_SERVICE);
        	// 着信音モードを取得
        	ringerMode = audioManager.getRingerMode();
        	switch (ringerMode) {
        	case AudioManager.RINGER_MODE_SILENT:
        	case AudioManager.RINGER_MODE_VIBRATE:
        	    // マナーモードなので鳴らさない
        		soundFlg = false;
        	    break;

        	default:
        	    // ノーマルモードなので音を鳴らす
        	    soundFlg = true;
        	    break;
        	}

			mBgmPlayer = MediaPlayer.create(mContext,R.raw.tw044);
			mBgmPlayer.setLooping(true);
			mBgmPlayer.start();
			
    		sp = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
    		seId = sp.load(mContext.getApplicationContext(), R.raw.se_maoudamashii_se_door04, 1);
			seId2 = sp.load(mContext.getApplicationContext(), R.raw.sm1up, 1);
			seId3 = sp.load(mContext.getApplicationContext(),R.raw.smdead, 1);

            // 初期化処理
            init();
            
        }
		
		public void stopBgm() {
			mBgmPlayer.stop();
		}

        public void init (){

			synchronized(holder){
				// 最初に3秒物理計算をウエイト
				mLastTime = System.currentTimeMillis() + 100;

				mPastTime = 0;

				// ゲーム状態を開始に
				gameStat = 1;

				// レールパネルの設定
				rpArray = new RailPanel[15];

				// パネルをランダムにするためにランダム配列を生成
				int rndPanel[] ={0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15};
				int n = 0;
				// 0番目だけは、垂直レールが来るようにしておく
				for ( int i = 1; i < rndPanel.length - 1; ++i ) {
					n = (int)(Math.random() * (rndPanel.length -1 - i));
					int tmp = rndPanel[i];
					rndPanel[i] = rndPanel[i + n];
					rndPanel[i + n] = tmp;
				}

				// レールパネルの実体生成
				for ( int i = 0; i < rpArray.length; ++i ) {
					rpArray[i] = new RailPanel();
				}
				for ( int i = 0; i < rpArray.length; ++i ) {
					Log.d("TEST", "FirstThred [rnd = " + rndPanel[i]);
					rpArray[rndPanel[i]].setNow_posision(rndPanel[i]);
					rpArray[rndPanel[i]].setMynumber(i);
					if (i < 3) {
						rpArray[rndPanel[i]].setImg(panel1);
						rpArray[rndPanel[i]].setMylailstyle(1);
					} else if (i < 6) {
						rpArray[rndPanel[i]].setImg(panel2);
						rpArray[rndPanel[i]].setMylailstyle(2);
					} else if (i < 9) {
						rpArray[rndPanel[i]].setImg(panel3);
						rpArray[rndPanel[i]].setMylailstyle(3);
					} else if (i < 12) {
						rpArray[rndPanel[i]].setImg(panel4);
						rpArray[rndPanel[i]].setMylailstyle(4);
					} else {
						rpArray[rndPanel[i]].setImg(panel5);
						rpArray[rndPanel[i]].setMylailstyle(5);
					}
					int xxx = (int)(LeftLoc + (i % 4) * SizeofPanelImage);
					int yyy = (int)(TopLoc + (i / 4) * SizeofPanelImage);
					rpArray[i].setX_pos(xxx);
					rpArray[i].setY_pos(yyy);
					rpArray[i].setX_vel(0);
					rpArray[i].setY_vel(0);
				}
				space_point = 15;
				moving_panel_number = -1;
				curvetime = 0;


				// 主人公の設定
				chara = new RailPanel();
				chara.setImg(b_charactor);
				chara.setX_pos(LeftLoc + (SizeofPanelImage / 2) - (b_charactor.getWidth() / 2));
				chara.setY_pos(TopLoc - (b_charactor.getHeight() / 2));
				chara.setX_vel(0);
				chara.setY_vel(VelofChara);
				charaPos = 0;
				chara.setDirection(2);
				chara.setMylailstyle(rpArray[getRailPanelbyPos(0)].getMylailstyle());
				Log.d("TEST", "chara style " + chara.getMylailstyle());
				
				if (mBgmPlayer.isPlaying() != true){
					mBgmPlayer.start();
				}
			}
            

        }
        
        /**
         * イメージを再セット
         * @param inBitmap
         */
        public void resetImages(Bitmap inBitmap){
        	int rndPanel[] ={0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15};
        	int n = 0;
        	for ( int i = 0; i < rpArray.length - 1; ++i ) {
        		n = (int)(Math.random() * (rpArray.length - i));
        		int tmp = rndPanel[i];
        		rndPanel[i] = rndPanel[i + n];
        		rndPanel[i + n] = tmp;
        	}

        	for ( int i = 0; i < rpArray.length; ++i ) {
        		rpArray[rndPanel[i]].setImg(Bitmap.createBitmap(inBitmap, ((i % 4) * SizeofPanelImage),
        				(i / 4) * SizeofPanelImage, SizeofPanelImage, SizeofPanelImage));
        		rpArray[rndPanel[i]].setMynumber(i);
        	}
        }

        // タッチイベント
        public boolean doTouchEvent(MotionEvent event) {

        	double x = event.getX();
        	double y = event.getY();

        	boolean b = mDetector.onTouchEvent(event);//★タッチイベントをバケツリレー。

            //タッチされた時
            switch (event.getAction())
            {
            case MotionEvent.ACTION_DOWN:
        		if(isDrug) {
            		break;
            	}

                break;
            case MotionEvent.ACTION_MOVE:
            	break;
            case MotionEvent.ACTION_UP:
            	isDrug = false;
            	break;
            default:
                break;
            }
            return true;
        }

        /**
         * 効果音が鳴らせるか判断し、再生
         * @param mp
         */
        private void ring_se(int resId) {
        	if (soundFlg) {
        		sp.play(resId, 1.0F, 1.0F, 0, 0, 1.0F);
        	}
        }

        public boolean isInBackground() {
			return mInBackground;
		}

		public void setInBackground(boolean b) {
			synchronized (mThreadLock) {
				this.mInBackground = b;
				if (!b){
					mThreadLock.notifyAll();
				}
			}
		}


        /**
         * スレッドが動作可能かどうかを示すのに使用。
         * trueを指定すると、スレッドに動作を許可する。
         * falseを指定すると、動作中だった場合は停止させる。
         * 直近にfalseでこの処理が呼ばれたあと、start()を呼び出すと、すぐさま終了となる。
         *
         * @param b true：動作指示, false：停止指示
         */
        public void setRunning(boolean b) {
            mRun = b;
        }

        @Override
        public void run() {
        	while(mRun){
        		synchronized (mThreadLock) {
					while (mInBackground) {
						try {
							mThreadLock.wait();
						} catch (final InterruptedException e) {
							// Ignore error here.
						}
					}
				}

        		Canvas c = null;
    		    try {
            		//描画処理
        			c = holder.lockCanvas(null);
        			synchronized (holder) {
        	            long elapsed = (long) ((System.currentTimeMillis() - mLastTime) / 1000.0);
        	            long towait = MILLIS_PAR_FLAME - elapsed;
        	            if (0 < towait){
        	            	try {
        	            		Thread.sleep(towait);
        	            	} catch (final InterruptedException e) {
        	            		setRunning(false);
        	            		break;
        	            	}
        	            }
            			culculate_state();
            			doDrow(c);
						/*            		    holder.unlockCanvasAndPost(c);*/
						if (gameStat == 0 && mBgmPlayer.isPlaying()){
							mBgmPlayer.pause();
							mBgmPlayer.seekTo(0);
							ring_se(seId3);
						}
    				}
    		    } catch(Exception e) {
    		    	ErrorDialog(e);
    		    } finally {
                	// 上の処理で例外が発生したときに矛盾した状態で表示が残らないよう
                	// finallyで次の処理を行う
                    if (c != null) {
                        holder.unlockCanvasAndPost(c);
                    }
    		    }
    		}

        }

        /***
         * 物理系さん
         */
        public void culculate_state() {
            long now = System.currentTimeMillis();
            // mLastTimeが未来の場合はなにもしない。
            // これは、ゲームスタート時に物理計算の開始を100ミリ秒遅らせるなどのため。
            if (mLastTime > now) return;

            // ゲーム開始からの経過時間
            if (gameStat == 1) {
            	mPastTime = mPastTime + now - mLastTime;
            }
            
            // 前回処理からの経過時間
            double elapsed = (now - mLastTime) / 1000.0;
            mLastTime = now;
            

            // 加速
            VelofChara = (int) ((6 + (int)mPastTime / 1000) * Math.sqrt(dip));
            if (chara.getX_vel() != 0) {
            	if (chara.getX_vel() < 0) chara.setX_vel(VelofChara * -1);
            	else chara.setX_vel(VelofChara);
            }
            if (chara.getY_vel() != 0) {
            	if (chara.getY_vel() < 0) chara.setY_vel(VelofChara * -1);
            	else chara.setY_vel(VelofChara);
            }
            
            // パネルの移動
            if (moving_panel_number >= 0){
            	double Xv = rpArray[moving_panel_number].getX_vel();
            	double Yv = rpArray[moving_panel_number].getY_vel();
            	double Xp = rpArray[moving_panel_number].getX_pos();
            	double Yp = rpArray[moving_panel_number].getY_pos();
            	int MvPanelRealPos = rpArray[moving_panel_number].getNow_posision();
            	int space_bef = space_point;

//        		Log.d("TEST", "culc [SP = " + space_point);
//        		Log.d("TEST", "culc [MP = " + moving_panel_number);
//        		Log.d("TEST", "culc [MPRP = " + MvPanelRealPos);
//        		Log.d("TEST", "culc [ELAP = " + elapsed);

            	if (Xv == 0){
            		Yp = Yp + Yv * elapsed;
            		if (Yv > 0){
            			if (Yp >= (TopLoc + (space_point / 4) * SizeofPanelImage)) {
            				Yp = TopLoc + (space_point / 4) * SizeofPanelImage;
            				space_point = MvPanelRealPos;
            			}
            		} else {
            			if (Yp <= (TopLoc + (space_point / 4) * SizeofPanelImage)) {
            				Yp = TopLoc + (space_point / 4) * SizeofPanelImage;
            				space_point = MvPanelRealPos;
            			}
            		}
            	} else {
            		Xp = Xp + Xv * elapsed;
            		if (Xv > 0){
            			if (Xp >= (LeftLoc + (space_point % 4) * SizeofPanelImage)) {
            				Xp = LeftLoc + (space_point % 4) * SizeofPanelImage;
            				space_point = MvPanelRealPos;
            			}
            		} else {
            			if (Xp <= (LeftLoc + (space_point % 4) * SizeofPanelImage)) {
            				Xp = LeftLoc + (space_point % 4) * SizeofPanelImage;
            				space_point = MvPanelRealPos;
            			}
            		}
            	}

            	rpArray[moving_panel_number].setX_pos((int)Xp);
            	rpArray[moving_panel_number].setY_pos((int)Yp);

            	if (space_point == MvPanelRealPos) {
                	rpArray[moving_panel_number].setX_vel(0);
                	rpArray[moving_panel_number].setY_vel(0);
                	rpArray[moving_panel_number].setNow_posision(space_bef);
    				moving_panel_number = -1;
    				//testPanelArr();
            	}
            }
            
            
            // 主人公の移動
            if (gameStat == 1){
				if ( mPastTime < 3000 ) return;
                double cYv = chara.getY_vel();
                double cXv = chara.getX_vel();
                double cYp = chara.getY_pos();
                double cXp = chara.getX_pos();
    			int cDir = chara.getDirection();
    			Double omega = VelofChara / ((double)SizeofPanelImage / 2);
    			switch (chara.getMylailstyle()) {
    			// 垂直パネル
    			case 1:
    				cYp = cYp + cYv * elapsed;
    				break;
    			// 水平パネル
    			case 2:
    				cXp = cXp + cXv * elapsed;
    				break;
    			// カーブパネル１
    			case 3:
    				// 突入時の方向を保持
    			    if (curvestat == 9) {
    					curvestat = cDir;
    				}
    				curvetime = curvetime + elapsed;
    				
    				//下向き
    			    if (curvestat == 2) {
    					cXp = LeftLoc + (SizeofPanelImage / 2) - (chara.getImg().getHeight() / 2) + (charaPos % 4) * SizeofPanelImage
    							+ (SizeofPanelImage / 2) - ((SizeofPanelImage / 2) * Math.cos(omega * curvetime));
    					cYp = TopLoc - (chara.getImg().getHeight() / 2) + (charaPos / 4) * SizeofPanelImage
    							+ (SizeofPanelImage / 2) * Math.sin(omega * curvetime);
    					chara.setX_vel(VelofChara);
    					chara.setY_vel(0);
    					chara.setDirection(1);
    				}
    			    // 上向き
    			    if (curvestat == 0){
    					cXp = LeftLoc + (SizeofPanelImage / 2) - (chara.getImg().getHeight() / 2) + (charaPos % 4) * SizeofPanelImage
    							- (SizeofPanelImage / 2) + ((SizeofPanelImage / 2) * Math.cos(omega * curvetime));
    					cYp = TopLoc - (chara.getImg().getHeight() / 2) + SizeofPanelImage + (charaPos / 4) * SizeofPanelImage
    							- (SizeofPanelImage / 2) * Math.sin(omega * curvetime);
    					chara.setX_vel(0 - VelofChara);
    					chara.setY_vel(0);
    					chara.setDirection(3);
    				}
    			    // 右向き
    				if (curvestat == 1) {
    					cXp = LeftLoc - (chara.getImg().getHeight() / 2) + (charaPos % 4) * SizeofPanelImage 
    							+ (SizeofPanelImage / 2) * Math.sin(omega * curvetime);
    					cYp = TopLoc + (SizeofPanelImage / 2) - (chara.getImg().getHeight() / 2) + (charaPos / 4) * SizeofPanelImage 
    							+ (SizeofPanelImage / 2) - ((SizeofPanelImage / 2) * Math.cos(omega * curvetime));
    					chara.setX_vel(0);
    					chara.setY_vel(VelofChara);
    					chara.setDirection(2);
    				}
    				// 左向き
    				if (curvestat == 3){
    					cXp = LeftLoc - (chara.getImg().getHeight() / 2) + SizeofPanelImage + (charaPos % 4) * SizeofPanelImage 
    							- (SizeofPanelImage / 2) * Math.sin(omega * curvetime);
    					cYp = TopLoc + (SizeofPanelImage / 2) - (chara.getImg().getHeight() / 2) + (charaPos / 4) * SizeofPanelImage 
    							- (SizeofPanelImage / 2) + ((SizeofPanelImage / 2) * Math.cos(omega * curvetime));
    					chara.setX_vel(0);
    					chara.setY_vel(0 - VelofChara);
    					chara.setDirection(0);
    				}
    				break;
    			// カーブパネル２
    			case 4:
    				// 突入時の方向を保持
    				if (curvestat == 9) {
    					curvestat = cDir;
    				}
    				curvetime = curvetime + elapsed;
    				
    				// 下向き
    				if (curvestat == 2) {
    					cXp = LeftLoc - (chara.getImg().getHeight() / 2) + (SizeofPanelImage / 2) + (charaPos % 4) * SizeofPanelImage
    							- (SizeofPanelImage / 2) + ((SizeofPanelImage / 2) * Math.cos(omega * curvetime));
    					cYp = TopLoc - (chara.getImg().getHeight() / 2) + (charaPos / 4) * SizeofPanelImage
    							+ (SizeofPanelImage / 2) * Math.sin(omega * curvetime);
    					chara.setX_vel(0 - VelofChara);
    					chara.setY_vel(0);
    					chara.setDirection(3);
    				}
    				// 上向き
    				if (curvestat == 0){
    					cXp = LeftLoc + (SizeofPanelImage / 2) - (chara.getImg().getHeight() / 2) + (charaPos % 4) * SizeofPanelImage 
    							+ (SizeofPanelImage / 2) - ((SizeofPanelImage / 2) * Math.cos(omega * curvetime));
    					cYp = TopLoc - (chara.getImg().getHeight() / 2) + SizeofPanelImage + (charaPos / 4) * SizeofPanelImage 
    							- (SizeofPanelImage / 2) * Math.sin(omega * curvetime);

    					chara.setX_vel(VelofChara);
    					chara.setY_vel(0);
    					chara.setDirection(1);
    				}
    				// 右向き
    				if (curvestat == 1) {
    					cXp = LeftLoc - (chara.getImg().getHeight() / 2) + (charaPos % 4) * SizeofPanelImage 
    							+ (SizeofPanelImage / 2) * Math.sin(omega * curvetime);
    					cYp = TopLoc + (SizeofPanelImage / 2) - (chara.getImg().getHeight() / 2) + (charaPos / 4) * SizeofPanelImage 
    							- (SizeofPanelImage / 2) + ((SizeofPanelImage / 2) * Math.cos(omega * curvetime));
    					chara.setX_vel(0);
    					chara.setY_vel(0 - VelofChara);
    					chara.setDirection(0);
    				}
    				// 左向き
    				if (curvestat == 3){
    					cXp = LeftLoc - (chara.getImg().getHeight() / 2) + SizeofPanelImage + (charaPos % 4) * SizeofPanelImage 
    							- (SizeofPanelImage / 2) * Math.sin(omega * curvetime);
    					cYp = TopLoc + (SizeofPanelImage / 2) - (chara.getImg().getHeight() / 2) + (charaPos / 4) * SizeofPanelImage 
    							+ (SizeofPanelImage / 2) - ((SizeofPanelImage / 2) * Math.cos(omega * curvetime));
    					chara.setX_vel(0);
    					chara.setY_vel(VelofChara);
    					chara.setDirection(2);
    				}
    				break;
    			// 十字パネル
    			case 5:
    				cYp = cYp + cYv * elapsed;
    				cXp = cXp + cXv * elapsed;
    				break;
    			default:
    				break;
    			}

                //パネルまたぎの時
    			if (chara.getDirection() == 2) {		// 上から下
            		if ( cYp + (chara.getImg().getHeight() / 2) > (TopLoc + ((charaPos / 4) + 1) * SizeofPanelImage)) {
						// Goal check
						if ( charaPos == 15 ){
							gameStat = 2;
							ring_se(seId2);
							return;
						}
    					// パネルをまたぐときはカーブ終了とみなす
            			curvetime = 0;
    					curvestat = 9;
    					// 行先にパネルがない場合は死亡
    					if (getRailPanelbyPos(charaPos + 4) == -1) {
    						chara.setY_vel(0);
    						gameStat = 0;
    		        		Log.d("TEST", "Matagi Direction 2 stop");
    						tempstr = tempstr + "2stop";
    						return;
    					}
    					// 行き先パネルに突入できるか判定
    					if (rpArray[getRailPanelbyPos(charaPos + 4)].checkGoPass(2) == false){
    						chara.setY_vel(0);
    						gameStat = 0;
    		        		Log.d("TEST", "Matagi Direction 2 ng");
    						tempstr = tempstr + "2ng";
    						return;
    					} else {
    						charaPos = charaPos + 4;
    						tempstr = tempstr + "2ok";
    						chara.setMylailstyle(rpArray[getRailPanelbyPos(charaPos)].getMylailstyle());
    						tempstr = tempstr + ">" +chara.getMylailstyle();
    		        		Log.d("TEST", "Matagi Direction 2 ok > style" +chara.getMylailstyle() + "charapos" + charaPos);
    		        		Log.d("TEST", "Matagi Direction 2 ok > cXp" + cXp);
    					}
    				}
    			}
    			if (chara.getDirection() == 0) {		// 下から上
    				if ( cYp + (chara.getImg().getHeight() / 2) < (TopLoc + (charaPos / 4) * SizeofPanelImage)) {
    					curvetime = 0;
    					curvestat = 9;
    					if (getRailPanelbyPos(charaPos - 4) == -1) {
    						chara.setY_vel(0);
    						gameStat = 0;
    		        		Log.d("TEST", "Matagi Direction 0 stop");
    						return;
    					}
    					if (rpArray[getRailPanelbyPos(charaPos - 4)].checkGoPass(0) == false){
    						chara.setY_vel(0);
    						gameStat = 0;
    		        		Log.d("TEST", "Matagi Direction 0 ng");
    						tempstr = tempstr + "0ng";
    						return;
    					} else {
    						charaPos = charaPos - 4;
    						tempstr = tempstr + "0ok";
    						chara.setMylailstyle(rpArray[getRailPanelbyPos(charaPos)].getMylailstyle());
    						tempstr = tempstr + ">" +chara.getMylailstyle();
    		        		Log.d("TEST", "Matagi Direction 0 ok > style" +chara.getMylailstyle() + "charapos" + charaPos);
    		        		Log.d("TEST", "Matagi Direction 0 ok > cXp" + cXp);
    					}
    				}
    			}
    			if (chara.getDirection() == 1) {		// 左から右
    				if ( cXp + (chara.getImg().getHeight() / 2) > (LeftLoc + ((charaPos % 4) + 1) * SizeofPanelImage)) {
    					curvetime = 0;
    					curvestat = 9;
    					if (getRailPanelbyPos(charaPos + 1) == -1 || charaPos % 4 == 3) {
    						chara.setX_vel(0);
    						gameStat = 0;
    		        		Log.d("TEST", "Matagi Direction 1 stop");
    						return;
    					}
    					if (rpArray[getRailPanelbyPos(charaPos + 1)].checkGoPass(1) == false){
    						chara.setX_vel(0);
    						gameStat = 0;
    		        		Log.d("TEST", "Matagi Direction 1 ng");
    						tempstr = tempstr + "1ng";
    						return;
    					} else {
    						charaPos = charaPos + 1;
    						tempstr = tempstr + "1ok";
    						chara.setMylailstyle(rpArray[getRailPanelbyPos(charaPos)].getMylailstyle());
    						tempstr = tempstr + ">" +chara.getMylailstyle();
    		        		Log.d("TEST", "Matagi Direction 1 ok > style" +chara.getMylailstyle() + "charapos" + charaPos);
    		        		Log.d("TEST", "Matagi Direction 1 ok > cXp" + cXp);
    					}
    				}
    			}
    			if (chara.getDirection() == 3) {		// 右から左
    				if ( cXp + (chara.getImg().getHeight() / 2) < (LeftLoc + ((charaPos % 4) * SizeofPanelImage))) {
    					curvetime = 0;
    					curvestat = 9;
    					if (getRailPanelbyPos(charaPos - 1) == -1 || charaPos % 4 == 0) {
    						chara.setX_vel(0);
    						gameStat = 0;
    		        		Log.d("TEST", "Matagi Direction 3 stop");
    						return;
    					}
    					if (rpArray[getRailPanelbyPos(charaPos - 1)].checkGoPass(1) == false){
    						chara.setX_vel(0);
    						gameStat = 0;
    		        		Log.d("TEST", "Matagi Direction 3 ng");
    						tempstr = tempstr + "3ng";
    						return;
    					} else {
    						charaPos = charaPos - 1;
    						tempstr = tempstr + "3ok";
    						chara.setMylailstyle(rpArray[getRailPanelbyPos(charaPos)].getMylailstyle());
    						tempstr = tempstr + ">" +chara.getMylailstyle();
    		        		Log.d("TEST", "Matagi Direction 3 ok > style" +chara.getMylailstyle() + "charapos" + charaPos);
    		        		Log.d("TEST", "Matagi Direction 3 ok > cXp" + cXp);
    					}
    				}
    			}
    			chara.setY_pos(cYp);
    			chara.setX_pos(cXp);
            }


        }

        private void testPanelArr(){
    		Log.d("TEST", "testPanelArr");
			for (int i = 0; i < rpArray.length; ++i ){
	    		Log.d("TEST", "testPanelArr pos = [" + rpArray[i].getNow_posision() + "] nuber = [" + rpArray[i].getMynumber() + "]");

				if (rpArray[i].getMynumber() != rpArray[i].getNow_posision()){
					return;
				}
			}
			isCleared = true;
//			setRunning(false);
//	    	// ダイアログの表示
//	    	 new AlertDialog.Builder(mContext)
//	    	.setTitle("Congratulations!")
//	    	.setMessage("You are smart.")
//	    	.setPositiveButton("OK", null)
//	    	.show();
//	    	 setRunning(true);
        }

        /**
         * 描画処理
         * @param c
         */
        private void doDrow(Canvas c){
        	if (c == null) {
        		Log.d("TEST", "doDrow c is null");
        		return;
        	}
        	/** スコアの描画情報 */
        	Paint paintScore;
            paintScore = new Paint();
            paintScore.setTextAlign(Paint.Align.LEFT);
            paintScore.setAntiAlias(true);
            if (gameStat == 1){
            	paintScore.setColor(Color.BLACK);
            } else {
            	paintScore.setColor(Color.RED);
            }
            paintScore.setTextSize(32 * dip);

			Matrix matx = new Matrix();
			float scale = (float)320 * dip / (float)box.getHeight();
			matx.postScale(scale,scale);
            Bitmap tmp_Bitmap;
			// Draw white screen
		    c.drawColor(Color.WHITE);
			// Draw frame
		    tmp_Bitmap = Bitmap.createBitmap(box, 0, 0, box.getWidth(), box.getHeight(), matx, true);
		    c.drawBitmap(tmp_Bitmap, 0, TopLoc - 12 * dip, paint);
			// Draw goal
			tmp_Bitmap = Bitmap.createBitmap(b_goal, 0, 0, b_goal.getWidth(), b_goal.getHeight(), matx, true);
			c.drawBitmap(tmp_Bitmap, (LeftLoc + SizeofPanelImage * 3), (TopLoc + SizeofPanelImage * 4), paint);
			
			// Draw panel
		    for ( int i = 0; i < rpArray.length; ++i ) {
			    tmp_Bitmap = Bitmap.createBitmap(rpArray[i].getImg(), 0, 0, 
			    		rpArray[i].getImg().getWidth(), rpArray[i].getImg().getHeight(), matx, true);
			    c.drawBitmap(tmp_Bitmap, (int)rpArray[i].getX_pos(), (int)rpArray[i].getY_pos(), paint);
		    }
			// Draw charactor
		    if (gameStat != 0) tmp_Bitmap = Bitmap.createBitmap(b_charactor, 0, 0, 
		    		b_charactor.getWidth(), b_charactor.getHeight(), matx, true);
		    else tmp_Bitmap = Bitmap.createBitmap(b_charactor_dead, 0, 0, 
		    		b_charactor.getWidth(), b_charactor.getHeight(), matx, true);
		    c.drawBitmap(tmp_Bitmap, (int)chara.getX_pos(), (int)chara.getY_pos(), paint);
			// Draw score
			if ( gameStat == 2 ) {
		        c.drawText("ゴール！！ " + ((int)mPastTime / 1000) * 100, 10 * dip, 30 * dip, paintScore);
		    } else {
				c.drawText("スコア： " + ((int)mPastTime / 1000) * 100, 10 * dip, 30 * dip, paintScore);
			}
        }

        /**
         * フリック入力時の処理
         * @param e1
         * @param e2
         * @param velocityX
         * @param velocityY
         * @return
         */
        public boolean doFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        	try {
        		Log.d("TEST", "onFling");
        		Log.d("TEST", "onFling [vX = " + velocityX + ", vY = " + velocityY + "]");
        		Log.d("TEST", "onFling [cP = " + charaPos + ", sP = " + space_point + "]");
        		// 誤操作を防ぐため、少ないフリック距離は無視
        		if (Math.abs(e1.getX() - e2.getX()) < 100 && Math.abs(e1.getY() - e2.getY()) < 40 ) return true;
        		// 誤操作を防ぐため、縦横の距離の比が小さい時も無視（斜めフリック）
        		if ( Math.abs(velocityY/velocityX) > Math.tan(Math.toRadians(35)) && Math.abs(velocityY/velocityX) < Math.tan(Math.toRadians(55)) ) return true;

        		// パネル移動中は処理を抜ける
        		if (moving_panel_number >= 0) return true;

				if (gameStat != 1){
					return true;
				}
        		if (Math.abs(velocityX) > Math.abs(velocityY)){
        			if (velocityX > 0){		// 左から右
        				if (space_point % 4 != 0){
        					// 主人公が乗っかってるパネルは移動不可
        					if (charaPos == space_point - 1) {
        		        		Log.d("TEST", "onFling notteru chara " + charaPos + "mvp" + getRailPanelbyPos(space_point - 1));
        						return true;
        					}
        					moving_panel_number = getRailPanelbyPos(space_point - 1);
        					rpArray[moving_panel_number].setX_vel(VelofPanel);
        				}
        			} else {				// 右から左
        				if (space_point % 4 < 3){
        					// 主人公が乗っかってるパネルは移動不可
        					if (charaPos == space_point + 1) {
        						Log.d("TEST", "onFling notteru chara " + charaPos + "mvp" + getRailPanelbyPos(space_point + 1));
        						return true;
        					}
        					moving_panel_number = getRailPanelbyPos(space_point + 1);
        					rpArray[moving_panel_number].setX_vel(VelofPanel * -1);
        				}

        			}
        		} else {
        			if (velocityY > 0){		// 上から下
        				if (space_point > 3){
        					// 主人公が乗っかってるパネルは移動不可
        					if (charaPos == space_point - 4) {
        		        		Log.d("TEST", "onFling notteru chara " + charaPos + "mvp" + getRailPanelbyPos(space_point - 4));
        		        		return true;
        					}
        					moving_panel_number = getRailPanelbyPos(space_point - 4);
        					rpArray[moving_panel_number].setY_vel(VelofPanel);
        				}
        			}else{					// 下から上
        				if (space_point < 12){
        					// 主人公が乗っかってるパネルは移動不可
        					if (charaPos == space_point + 4) {
        		        		Log.d("TEST", "onFling notteru chara " + charaPos + "mvp" + getRailPanelbyPos(space_point + 4));
        						return true;
        					}
        					moving_panel_number = getRailPanelbyPos(space_point + 4);
        					rpArray[moving_panel_number].setY_vel(VelofPanel * -1);
        				}
        			}
        		}
        		if (moving_panel_number >= 0) {
                	// パネル移動サウンド
        			ring_se(seId);
        		}

        		return true;
        	} catch (Exception e) {
        		ErrorDialog(e);

            	// ダイアログの表示
            	 new AlertDialog.Builder(mContext)
            	.setTitle("TEST INFO")
            	.setMessage("mp = " + moving_panel_number + ", sp = " + space_point + ", vx = " + velocityX + ", vy = " + velocityY)
            	.setPositiveButton("OK", null)
            	.show();
        		return false;
        	}

        }

        /**
         * 場所情報から、そこにいるパネルの配列番号を取得
         * @param pos
         * @return
         */
        private int getRailPanelbyPos(int pos){
			for (int i = 0; i < rpArray.length; ++i ){
				if (rpArray[i].getNow_posision() == pos){
					return i;
				}
			}
			return -1;
        }

        public void doMenu1(){
			// Create EditText
        	final EditText edtInput = new EditText(mContext);
        	edtInput.setText(tempstr);
            // Show Dialog
            new AlertDialog.Builder(mContext)
			.setTitle("Temp str")
			.setView(edtInput)
			.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					String aaa = edtInput.getText().toString();
				}
			})
			.show();
        }
        public void doMenu2(){
            init();
        }
        public void doMenu3(){
            // Create EditText
        	final EditText edtInput = new EditText(mContext);
        	edtInput.setText("dip" + res.getDisplayMetrics().scaledDensity);
            // Show Dialog
            new AlertDialog.Builder(mContext)
            .setTitle("Set Velocity")
            .setView(edtInput)
            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
            	public void onClick(DialogInterface dialog, int whichButton) {
            		String aaa = edtInput.getText().toString();
            	}
            })
            .show();
        }
    }

    /** アプリケーションコンテキストのハンドル。Drawablesの取得などに使用 */
    private Context mContext;

    private FirstThread thread;
	private final Object mThreadLock = new Object();
    private GestureDetector mDetector;

    private Bitmap mBitmap;

	/**
	 * コンストラクタ
	 * @param context
	 */
	/**
	 * @param context
	 */
	public FirstSurfaceView(Context context) {
		super(context);
		mContext = context;

        // 画面の変更通知コールバックをビューに登録する
        SurfaceHolder viewholder = getHolder();
        viewholder.addCallback(this);

        // スレッドは作成のみを行う。スレッドの開始はsurfaceCreated()で行う
        thread = new FirstThread(viewholder, context);
	}

    /**
     * このLunarViewに対応するアニメーションスレッドの取得
     *
     * @return アニメーションスレッド
     */
    public FirstThread getThread() {
        return thread;
    }

    //サーフェイス変化で実行される
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		Log.d("TEST", "surfaceChanged");
	}

    //サーフェイス生成で実行される
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		Log.d("TEST", "surfaceCreated");
		mDetector = new GestureDetector(getContext(), new GestureListener());//★GestureDetectorオブジェクト生成。
		if (thread.isInBackground()) {
	        if (mBitmap != null){
		        thread.resetImages(mBitmap);
	        }
			thread.setInBackground(false);
		} else {
			thread.setRunning(true);
	        //run()を実行
			thread.start();
		}
	}

    //サーフェイス破棄で実行される
	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		Log.d("TEST", "surfaceDestroyed");

		//thread.pause();
		thread.stopBgm();
		thread.setInBackground(true);

//    	// スレッドに終了を通知し、その処理が終了するまで待つ。
//    	// そうしないと終了して画面を開放したあとに、スレッドが画面を操作しようとしてしまう。
//        boolean retry = true;
//        thread.setRunning(false);
//        while (retry) {
//            try {
//        		Log.d("TEST", "retry");
//                thread.join();
//                retry = false;
//            } catch (InterruptedException e) {
//            }
//        }
	}

    private class GestureListener extends SimpleOnGestureListener {

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
    		return thread.doFling(e1, e2, velocityX, velocityY);
        }
    }

    // タッチイベントを処理するためOverrideする
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return thread.doTouchEvent(event);
    }

    public void setBitmap(Bitmap inBitmap){
    	mBitmap = Bitmap.createScaledBitmap(inBitmap, 480, 480, false);
    }

    /**
     * エラーダイアログ
     * @param e
     */
    public void ErrorDialog(Exception e){
    	// ダイアログの表示
    	 new AlertDialog.Builder(mContext)
    	.setTitle("TEST ERROR")
    	.setMessage(e.getMessage())
    	.setPositiveButton("OK", null)
    	.show();
    }

    
    /**
     * パネルクラス
     * @author Makita
     *
     */
	public class RailPanel {
		private Bitmap Img;
		private double X_pos;
		private double Y_pos;
		private int X_vel;
		private int Y_vel;
		private int now_posision;
		private int mynumber;
		private int mylailstyle;
		private int direction;

		public int getDirection() {
			return direction;
		}

		public void setDirection(int direction) {
			this.direction = direction;
		}

		public boolean checkGoPass(int direction){
			if (mylailstyle >= 3){
				return true;
			} else if (mylailstyle == 1){
				switch (direction) {
				case 0:
					return true;
				case 1:
					return false;
				case 2:
					return true;
				case 3:
					return false;
				default:
					return false;
				}
			}else if (mylailstyle == 2){
				switch (direction) {
				case 0:
					return false;
				case 1:
					return true;
				case 2:
					return false;
				case 3:
					return true;
				default:
					return false;
				}
			} else {
				return false;
			}
		}

		public int getMylailstyle() {
			return mylailstyle;
		}


		public void setMylailstyle(int mylailstyle) {
			this.mylailstyle = mylailstyle;
		}


		public double getX_pos() {
			return X_pos;
		}


		public void setX_pos(double x_pos) {
			X_pos = x_pos;
		}


		public double getY_pos() {
			return Y_pos;
		}


		public void setY_pos(double y_pos) {
			Y_pos = y_pos;
		}



		public int getX_vel() {
			return X_vel;
		}


		public void setX_vel(int x_vel) {
			X_vel = x_vel;
		}


		public int getY_vel() {
			return Y_vel;
		}


		public void setY_vel(int y_vel) {
			Y_vel = y_vel;
		}


		public int getNow_posision() {
			return now_posision;
		}


		public void setNow_posision(int now_posision) {
			this.now_posision = now_posision;
		}


		public int getMynumber() {
			return mynumber;
		}


		public void setMynumber(int mynumber) {
			this.mynumber = mynumber;
		}


		public Bitmap getImg() {
			return Img;
		}


		public void setImg(Bitmap img) {
			Img = img;
		}


		public RailPanel(){

		}



	}
}
