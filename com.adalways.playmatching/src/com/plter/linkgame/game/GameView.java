package com.plter.linkgame.game;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.adalways.playmatching.R;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.plter.linkgame.reader.GamePkg;
import com.plter.linkgame.reader.Picture;

public class GameView extends FrameLayout{
	private InterstitialAd mInterstitialAd;


	public GameView(Context context) {
		super(context);
	}


	public GameView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}


	/**
	 * 鏍规嵁娓告垙璧勬簮鍖呭垵濮嬪寲
	 * @param gamePkg
	 */
	public void initWithGamePkg(GamePkg gamePkg){

		setGamePkg(gamePkg);

		//璁剧疆鑳屾櫙
		setBackgroundDrawable(new BitmapDrawable(getGamePkg().getBackground().getBitmap()));

		//鏋勫缓鍗＄墖瀹瑰櫒
		cardsContainer=new RelativeLayout(getContext());
		addView(cardsContainer, -1, -1);

		//鏋勫缓杩炵嚎瀹瑰櫒
		linesContainer=new LinesContainer(getContext());
		addView(linesContainer, -1, -1);
	}

	public void showStartGameAlert(){
		setEnabled(false);
		
		LayoutInflater inflater = LayoutInflater.from(getContext());
		View layout = inflater.inflate(R.layout.dialog_main_info, null);
		new AlertDialog.Builder(getContext()).setTitle("Game Start").setView(layout)
		.setMessage("Are you ready? press Start to start game")
		.setPositiveButton("Start", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					reset();
					
					startGame();				
				}
			}).show();
		
		
		 AdView mAdView = (AdView) layout.findViewById(R.id.adViewgame);
		    AdRequest adRequest = new AdRequest.Builder().build();
		    mAdView.loadAd(adRequest);
		

		
		/*new AlertDialog.Builder(getContext())
		.setTitle("Are you ready?")
		.setMessage("press 鈥淪tart鈥� to start game")
		.setCancelable(false)
		.setPositiveButton("Start", new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				reset();
				
				//mInterstitialAd = new InterstitialAd(this);
			    /*mInterstitialAd.setAdUnitId("ca-app-pub-6349721350485663/8578624037");
			    AdRequest adRequest = new AdRequest.Builder()
			    .addTestDevice("YOUR_DEVICE_HASH")
			    .build();
			    if (mInterstitialAd.isLoaded()) {
			        mInterstitialAd.show();
			    } */
			//	startGame();				
		//	}
		//}).show();
	}


	/**
	 * 閲嶇疆鍏冲崱
	 */
	public void reset(){
		levelNum=0;
	}


	/**
	 * 寮�濮嬫父鎴�
	 */
	public void startGame(){
		level=Level.LEVELS[levelNum];
		currentTime=level.getMaxTime();

		getLevelTv().setText(String.format("Level %d: ", levelNum+1));
		 
		

		genGameCards();
		addGameCards();

		startGameTimerHandler();

		gameRunning=true;

		setEnabled(true);
	}


	/**
	 * 鎵撲贡鍗＄墖
	 */
	public void breakGameCards(){
		gameCardsMap=new Card[level.getH_cards_count()][level.getV_cards_count()];

		allIndex.clear();
		for (int i = 0; i < level.getH_cards_count(); i++) {
			for (int j = 0; j < level.getV_cards_count(); j++) {
				allIndex.add(new Point(i, j));
			}
		}

		Card card=null;
		Point point=null;

		for (int i = 0; i < gameCards.size(); i++) {
			card=gameCards.get(i);
			point=allIndex.remove((int) (Math.random()*allIndex.size()));

			card.setIndexI(point.x);
			card.setIndexJ(point.y);
			card.resetPositionByIndexIJ();
			gameCardsMap[point.x][point.y]=card;
		}

		if (!GameUtil.isGameConnected(level, gameCards, gameCardsMap,null)) {
			breakGameCards();
		}
	}

	/**
	 * 鎵�鏈夌殑鍙敤绱㈠紩
	 */
	private final List<Point> allIndex=new ArrayList<Point>();



	/**
	 * 灏嗘父鎴忓崱鐗囨坊鍔犲埌鍦烘櫙涓� 
	 */
	public void addGameCards(){

		breakGameCardsArray();

		Card card;
		int index=0;

		Config.setCardsOffsetX(Config.GAME_CARDS_AREA_LEFT+(Config.getGameCardsAreaWidth()-Config.getCardWidth()*level.getH_cards_count())/2);
		Config.setCardsOffsetY(Config.GAME_CARDS_AREA_TOP+(Config.getGameCardsAreaHeight()-Config.getCardHeight()*level.getV_cards_count())/2);

		gameCardsMap=new Card[level.getH_cards_count()][level.getV_cards_count()];

		for (int i = 0; i < level.getH_cards_count(); i++) {
			for (int j = 0; j < level.getV_cards_count(); j++) {
				index=j+i*level.getV_cards_count();
				card = gameCards.get(index);
				cardsContainer.addView(card, (int)(Config.getCardWidth()), (int)(Config.getCardHeight()));

				card.setIndexI(i);
				card.setIndexJ(j);
				card.resetPositionByIndexIJ();
				gameCardsMap[i][j]=card;
			}
		}

		if (!GameUtil.isGameConnected(level, gameCards, gameCardsMap, null)) {
			breakGameCards();
		}
	}


	/**
	 * 鐢熸垚娓告垙鍗＄墖
	 */
	public void genGameCards(){

		cardsContainer.removeAllViews();
		gameCards.clear();

		int halfCardsCount=level.getH_cards_count()*level.getV_cards_count()/2;
		Picture pic=null;
		Card card;

		for (int i = 0; i < halfCardsCount; i++) {
			pic=getGamePkg().getPictures()[(int) (Math.random()*getGamePkg().getPictures().length)];

			card=new Card(getContext(),pic);
			card.setOnClickListener(cardClickHandler);
			gameCards.add(card);
			card=new Card(getContext(),pic);
			card.setOnClickListener(cardClickHandler);		
			gameCards.add(card);
		}
	}

	private final OnClickListener cardClickHandler=new OnClickListener() {

		public void onClick(View v) {
			currentCheckedCard = (Card) v;
			currentCheckedCard.setChecked(true);

			if (lastCheckedCard!=null) {
				if (lastCheckedCard!=currentCheckedCard) {
					if(testCards()){
						gameCardsMap[currentCheckedCard.getIndexI()][currentCheckedCard.getIndexJ()]=null;
						gameCardsMap[lastCheckedCard.getIndexI()][lastCheckedCard.getIndexJ()]=null;
						cardsContainer.removeView(currentCheckedCard);
						cardsContainer.removeView(lastCheckedCard);

						gameCards.remove(currentCheckedCard);
						gameCards.remove(lastCheckedCard);

						currentCheckedCard.setOnClickListener(null);
						lastCheckedCard.setOnClickListener(null);

						linesContainer.showLines(GameUtil.lastLinkedLinePoints);

						if (gameCards.size()>0){
							if (!GameUtil.isGameConnected(level, gameCards, gameCardsMap,null)) {
								breakGameCards();
							}
						}else {
							
							//濡傛灉杩樻湁鏃堕棿锛屽垯寮瑰嚭鎴愬姛瀵硅瘽妗�
							if (gameRunning) {
								stopGameTimerHandler();
								gameRunning=false;

								if (levelNum<Level.LEVELS.length-1) {

									setEnabled(false);
									
									LayoutInflater inflater = LayoutInflater.from(getContext());
									View layout = inflater.inflate(R.layout.dialog_main_info, null);
									new AlertDialog.Builder(getContext()).setTitle("Good Luck").setView(layout)
									.setMessage(String.format("Congratulation, you passed the level %d game. Game will enter level %d, Good Luck!", levelNum + 1, levelNum+2))
									.setCancelable(false)
									.setPositiveButton("Continue", new DialogInterface.OnClickListener() {

										public void onClick(DialogInterface dialog, int which) {
											levelNum++;
											startGame();										
										}
									}).show();
											 
									
									 AdView mAdView = (AdView) layout.findViewById(R.id.adViewgame);
									    AdRequest adRequest = new AdRequest.Builder().build();
									    mAdView.loadAd(adRequest);

									/*new AlertDialog.Builder(getContext())
									.setTitle("Congratulation!")
									.setMessage(String.format("Game will enter level %d", levelNum+2))
									.setCancelable(false)
									.setPositiveButton("Continue", new DialogInterface.OnClickListener() {

										public void onClick(DialogInterface dialog, int which) {
											levelNum++;
											startGame();										
										}
									}).show();*/

								}else{

									setEnabled(false);
									
									LayoutInflater inflater = LayoutInflater.from(getContext());
									View layout = inflater.inflate(R.layout.dialog_main_info, null);
									new AlertDialog.Builder(getContext()).setTitle("Congratulation").setView(layout)
									.setMessage("Awesome, You pass the game.")
									.setCancelable(false)
									.setNegativeButton("Exit", new DialogInterface.OnClickListener() {

										public void onClick(DialogInterface dialog, int which) {
											System.exit(0);
										}
									})
									.setPositiveButton("Replay", new DialogInterface.OnClickListener() {

										public void onClick(DialogInterface dialog, int which) {
											reset();
											startGame();
										}
									})
									.show();
									
							 

									
									 AdView mAdView = (AdView) layout.findViewById(R.id.adViewgame);
									    AdRequest adRequest = new AdRequest.Builder().build();
									    mAdView.loadAd(adRequest);

									/*new AlertDialog.Builder(getContext())
									.setTitle("Congratulation!")
									.setMessage("Awesome, You pass the game.")
									.setCancelable(false)
									.setNegativeButton("Exit", new DialogInterface.OnClickListener() {

										public void onClick(DialogInterface dialog, int which) {
											System.exit(0);
										}
									})
									.setPositiveButton("Replay", new DialogInterface.OnClickListener() {

										public void onClick(DialogInterface dialog, int which) {
											reset();
											startGame();
										}
									})
									.show();*/
								}
							}
						}
					}else{
						lastCheckedCard.setChecked(false);
						currentCheckedCard.setChecked(false);
					}
				}else{
					lastCheckedCard.setChecked(false);
				}

				lastCheckedCard=null;
				currentCheckedCard=null;

			}else{
				lastCheckedCard=currentCheckedCard;
			}
		}
	};


	/**
	 * 娴嬭瘯鍗＄墖鏄惁鑱旈��
	 * @return 
	 */
	private boolean testCards(){
		if (lastCheckedCard.getPicture().getId()==currentCheckedCard.getPicture().getId()) {

			int i1=lastCheckedCard.getIndexI(),		j1=lastCheckedCard.getIndexJ(),
					i2=currentCheckedCard.getIndexI(),	j2=currentCheckedCard.getIndexJ();

			if (GameUtil.testCards(level,gameCardsMap,i1, j1, i2, j2, true)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 鍙栧緱鍏冲崱鏁�
	 * @return
	 */
	public int getLevelNum(){
		return levelNum;
	}


	public Level getLevel(){
		return level;
	}

	
	/**
	 * 鏆傚仠娓告垙
	 */
	public void pauseGame(){
		gameRunning=false;
		pause();
		
		
		LayoutInflater inflater = LayoutInflater.from(getContext());
		View layout = inflater.inflate(R.layout.dialog_main_info, null);
		new AlertDialog.Builder(getContext()).setTitle("Game paused").setView(layout)
        .setCancelable(false)
		.setMessage("Game Paused and have a good rest, Press resume continue game.")
		.setPositiveButton("Resume", new DialogInterface.OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) {
				gameRunning=true;
				resume();
			}
		})
		.show();
		
 

		
		 AdView mAdView = (AdView) layout.findViewById(R.id.adViewgame);
		    AdRequest adRequest = new AdRequest.Builder().build();
		    mAdView.loadAd(adRequest);

		
		/*new AlertDialog.Builder(getContext())
		.setCancelable(false)
		.setTitle("Game pause")
		.setMessage("Game Pause锛孭ress 鈥淩esume鈥� continue game")
		.setPositiveButton("Resume", new DialogInterface.OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) {
				gameRunning=true;
				resume();
			}
		})
		.show();*/
	}
	

	/**
	 * 缁х画
	 */
	public void resume(){
		if (gameRunning) {
			startGameTimerHandler();
		}
	}


	/**
	 * 鏆傚仠
	 */
	public void pause(){
		stopGameTimerHandler();
	}


	public boolean isTimerRunning() {
		return timerRunning;
	}


	/**
	 * 鎵撲贡鍗＄墖鏁扮粍
	 */
	private void breakGameCardsArray(){
		for (int i = 0; i < 200; i++) {
			gameCards.add(gameCards.remove((int)(Math.random()*gameCards.size())));
		}
	}


	/**
	 * 鍋滄璁℃椂
	 */
	private void stopGameTimerHandler(){
		gameTimerHandler.removeMessages(1);
		timerRunning=false;
	}


	/**
	 * 鍚姩娓告垙璁℃椂
	 */
	private void startGameTimerHandler(){
		gameTimerHandler.sendEmptyMessage(1);
		timerRunning=true;
	}


	public TextView getTimeTv() {
		return timeTv;
	}


	public void setTimeTv(TextView timeTv) {
		this.timeTv = timeTv;
	}

	public TextView getLevelTv() {
		return levelTv;
	}


	public void setLevelTv(TextView levelTv) {
		this.levelTv = levelTv;
	}

	public Button getBreakCardsBtn() {
		return breakCardsBtn;
	}


	public void setBreakCardsBtn(Button breakCardsBtn) {
		if (this.breakCardsBtn!=null) {
			this.breakCardsBtn.setOnClickListener(null);
		}

		this.breakCardsBtn = breakCardsBtn;
		breakCardsBtn.setOnClickListener(breakCardsBtnClickHandler);
	}


	private final OnClickListener breakCardsBtnClickHandler=new OnClickListener() {

		public void onClick(View v) {
			breakGameCards();
		}
	};

	private final OnClickListener noteBtnClickHandler=new OnClickListener() {

		public void onClick(View v) {

			if (gameCards.size()>=2) {
				Card[] notedCards = new Card[2];
				if(GameUtil.isGameConnected(level, gameCards, gameCardsMap, notedCards)){
					notedCards[0].startNoteAnim();
					notedCards[1].startNoteAnim();
				}
			}
		}
	};

	private final OnClickListener pauseBtnClickListener=new OnClickListener() {
		
		public void onClick(View v) {
			pauseGame();
		}
	};


	public Button getNoteBtn() {
		return noteBtn;
	}


	public void setNoteBtn(Button noteBtn) {
		if (this.noteBtn!=null) {
			this.noteBtn.setOnClickListener(null);
		}
		this.noteBtn = noteBtn;

		noteBtn.setOnClickListener(noteBtnClickHandler);
	}

	/**
	 * @return the gamePkg
	 */
	public GamePkg getGamePkg() {
		return gamePkg;
	}


	/**
	 * @param gamePkg the gamePkg to set
	 */
	private void setGamePkg(GamePkg gamePkg) {
		this.gamePkg = gamePkg;
	}

	/**
	 * @return the pauseBtn
	 */
	public Button getPauseBtn() {
		return pauseBtn;
	}


	/**
	 * @param pauseBtn the pauseBtn to set
	 */
	public void setPauseBtn(Button pauseBtn) {
		if (this.pauseBtn!=null) {
			this.pauseBtn.setOnClickListener(null);
		}
		
		this.pauseBtn = pauseBtn;
		
		this.pauseBtn.setOnClickListener(pauseBtnClickListener);
	}

	private final List<Card> gameCards = new ArrayList<Card>();

	/**
	 * 娓告垙鍖呭璞�
	 */
	private GamePkg gamePkg=null;

	/**
	 * 鍏冲崱鏁�
	 */
	private int levelNum=1;
	private Level level=null;
	private int currentTime=0;


	/**
	 * 娓告垙鍗＄墖鍥�
	 */
	private Card[][] gameCardsMap = null;
	private Card lastCheckedCard=null;
	private Card currentCheckedCard=null;
	private LinesContainer linesContainer=null;
	private RelativeLayout cardsContainer=null;
	private TextView timeTv=null;
	private TextView levelTv=null;
	private Button breakCardsBtn=null,noteBtn=null,pauseBtn;

	/**
	 * 娓告垙鏄惁姝ｅ湪杩愯锛岃鏃跺櫒鏄惁姝ｅ湪杩愯
	 */
	private boolean gameRunning=false,timerRunning=false;

	private final Handler gameTimerHandler=new Handler(){
		public void handleMessage(android.os.Message msg) {

			getTimeTv().setText(String.format("Time: %d", currentTime));

			if (currentTime>0) {
				currentTime--;
				gameTimerHandler.sendEmptyMessageDelayed(1, 1000);
			}else{
				if (gameCards.size()>0) {

					gameRunning=false;
					timerRunning=false;

					setEnabled(false);
					
					
					
					LayoutInflater inflater = LayoutInflater.from(getContext());
					View layout = inflater.inflate(R.layout.dialog_main_info, null);
					new AlertDialog.Builder(getContext()).setTitle("Bad luck").setView(layout)
					.setMessage("Oh, Shit. You failed! Game over!!!")
					.setCancelable(false)
					.setPositiveButton("Replay", new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dialog, int which) {
							reset();
							startGame();							
						}
					})
					.setNegativeButton("Have a Rest", new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dialog, int which) {
							System.exit(0);
						}
					}).show();
					
			 

					
					 AdView mAdView = (AdView) layout.findViewById(R.id.adViewgame);
					    AdRequest adRequest = new AdRequest.Builder().build();
					    mAdView.loadAd(adRequest);

					/*new AlertDialog.Builder(getContext())
					.setTitle("Bad luck")
					.setMessage("You failed锛実ame over")
					.setCancelable(false)
					.setPositiveButton("Replay", new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dialog, int which) {
							reset();
							startGame();							
						}
					})
					.setNegativeButton("Exit", new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dialog, int which) {
							System.exit(0);
						}
					}).show();*/
				}
			}
		}
	};
}
