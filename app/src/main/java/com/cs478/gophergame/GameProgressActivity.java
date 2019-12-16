package com.cs478.gophergame;

import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;


/*Strategy Used
    Player 1 - Guesses randomly till he/she encouters a near miss or close miss. The next guess will be only of the tiles which are near/close.
                Priority is given to the close tiles over near tiles.
    Player 2 - Is a Naive strategy and guesses randomly everytime.
    If a player selects a tile which is already selected, he/she gets a free turn.
 */

public class GameProgressActivity extends AppCompatActivity {

    private ArrayList<Integer> mThumbQM = new ArrayList<Integer>(100);

    private static String GAME_MODE = "";

    public static final int PLAYER_ONE_CODE = 1;
    public static final int PLAYER_TWO_CODE = 2;

    public int THREAD_SLEEP_MIL = 1000;

    public static boolean PLAYER_ONE_TURN = true;
    public static boolean PLAYER_TWO_TURN = false;

    public static final String ALREADY_TAKEN = "Oops! This tile was already selected. You get a free turn!";
    public static final String GOPHER_CLOSE = "Excellent! Gopher is in your sight.";
    public static final String GOPHER_NEAR = "Great! You are getting closer to the Gopher.";
    public static final String GOPHER_FAR = "Hard luck! Gopher is not in the vicinity";
    public static final String RANDOM_MODE = "Random";
    public static final String CLOSE_MODE = "Close";
    public static final String FAR_MODE = "Far";
    public static final String PLAYER_ONE_NAME = "Player 1: ";
    public static final String PLAYER_TWO_NAME = "Player 2: ";

    public String PLAYER_ONE_MODE = RANDOM_MODE;
    public String PLAYER_NAME = PLAYER_ONE_NAME;

    public static int GOPHER_PRESENT_INDEX;

    public boolean WINNER_FOUND = false;

    //Create Bounds to check Close miss
    public static final ArrayList<Integer> MINIMUM_BOUND_X = new ArrayList<Integer>(Arrays.asList(0,10,20,30,40,50,60,70,80,90));
    public static final ArrayList<Integer> MAXIMUM_BOUND_X = new ArrayList<Integer>(Arrays.asList(9,19,29,39,49,59,69,79,89,99));
    public static final ArrayList<Integer> MINIMUM_BOUND_Y = new ArrayList<Integer>(Arrays.asList(0,1,2,3,4,5,6,7,8,9));
    public static final ArrayList<Integer> MAXIMUM_BOUND_Y = new ArrayList<Integer>(Arrays.asList(90,91,92,93,94,95,96,97,98,99));

    //Create Bounds to check Near miss
    public static ArrayList<Integer> MINIMUM_BOUND_X_NEAR = new ArrayList<Integer>(Arrays.asList(1,11,21,31,41,51,61,71,81,91));
    public static ArrayList<Integer> MAXIMUM_BOUND_X_NEAR = new ArrayList<Integer>(Arrays.asList(8,18,28,38,48,58,68,78,88,98));
    public static ArrayList<Integer> MINIMUM_BOUND_Y_NEAR = new ArrayList<Integer>(Arrays.asList(10,11,12,13,14,15,16,17,18,19));
    public static ArrayList<Integer> MAXIMUM_BOUND_Y_NEAR = new ArrayList<Integer>(Arrays.asList(80,81,82,83,84,85,86,87,88,89));

    public static ArrayList<Integer> GOPHER_POSITION_CLOSE;
    public static ArrayList<Integer> GOPHER_POSITION_NEAR;

    Button startGame;
    Button playerTwo;
    Button playerOne;
    GridView gameProgress_gv;

    private final ThreadHandler mHandler = new ThreadHandler(this);

    //Main UI thread handler and implemented in a way to prevent Memory Leak
   public static class ThreadHandler extends Handler{
        private final WeakReference<GameProgressActivity> mGameProgressActivity;

        public ThreadHandler(GameProgressActivity activity){
            mGameProgressActivity = new WeakReference<GameProgressActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg){
            int what = msg.what;
            int selectedNumber = msg.arg1;

            GameProgressActivity gameProgressActivity = mGameProgressActivity.get();

            //Check for game modes
            if(GAME_MODE.equals("Control")){
                //Player 1
                if(what == 1){
                    PLAYER_TWO_TURN = true;
                    PLAYER_ONE_TURN = false;

                    if(msg.arg1 == 999){
                        gameProgressActivity.disableButtons();
                    }else{
                        gameProgressActivity.updateButtons();
                        gameProgressActivity.updateView(selectedNumber);
                    }
                }
                //Player 2
                else if(what == 2){
                    PLAYER_TWO_TURN = false;
                    PLAYER_ONE_TURN = true;

                    if(msg.arg1 == 999){
                        gameProgressActivity.disableButtons();
                    }else{
                        gameProgressActivity.updateButtons();
                        gameProgressActivity.updateView(selectedNumber);
                    }
                }
            }
            else{
                //Player 1
                if(what == 1){
                    PLAYER_TWO_TURN = true;
                    PLAYER_ONE_TURN = false;

                    if(msg.arg1 == 999){
                        gameProgressActivity.disableButtons();
                    }else{
                        gameProgressActivity.updateButtons();
                        if(gameProgressActivity.updateView(selectedNumber)){
                            if(!gameProgressActivity.WINNER_FOUND)
                                gameProgressActivity.callPlayerOneThread();
                        }else{
                            if(!gameProgressActivity.WINNER_FOUND)
                                gameProgressActivity.callPlayerTwoThread();
                        }

                    }


                }
                //Player 2
                else if(what == 2){
                    PLAYER_TWO_TURN = false;
                    PLAYER_ONE_TURN = true;

                    if(msg.arg1 == 999){
                        gameProgressActivity.disableButtons();
                    }else{
                        gameProgressActivity.updateButtons();
                        if(gameProgressActivity.updateView(selectedNumber)){
                            if(!gameProgressActivity.WINNER_FOUND)
                                gameProgressActivity.callPlayerTwoThread();
                        }else{
                            if(!gameProgressActivity.WINNER_FOUND)
                                gameProgressActivity.callPlayerOneThread();
                        }
                    }

                }
            }

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_progress);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        startGame = findViewById(R.id.startGame_btn);
        playerOne = findViewById(R.id.playerOne_btn);
        playerTwo = findViewById(R.id.playerTwo_btn);

        //Select a random tile where a Gopher is present
        GOPHER_PRESENT_INDEX = getRandomNumber();

        //Fill the close array based on index
        GOPHER_POSITION_CLOSE = new ArrayList<Integer>();
        //Fill the near array based on Index
        GOPHER_POSITION_NEAR = new ArrayList<Integer>();

        GOPHER_POSITION_CLOSE.add(GOPHER_PRESENT_INDEX);

        //Filling the index based on bounds on X and Y
        if(!MINIMUM_BOUND_X.contains(GOPHER_PRESENT_INDEX)){
            GOPHER_POSITION_CLOSE.add(GOPHER_PRESENT_INDEX-1);
            if(!MINIMUM_BOUND_Y.contains(GOPHER_PRESENT_INDEX)){
                GOPHER_POSITION_CLOSE.add(GOPHER_PRESENT_INDEX-11);
                GOPHER_POSITION_CLOSE.add(GOPHER_PRESENT_INDEX-10);
            }
            if(!MAXIMUM_BOUND_Y.contains(GOPHER_PRESENT_INDEX)){
                GOPHER_POSITION_CLOSE.add(GOPHER_PRESENT_INDEX+9);
                GOPHER_POSITION_CLOSE.add(GOPHER_PRESENT_INDEX+10);
            }
            if(!MINIMUM_BOUND_Y_NEAR.contains(GOPHER_PRESENT_INDEX)){
                GOPHER_POSITION_NEAR.add(GOPHER_PRESENT_INDEX-21);
                GOPHER_POSITION_NEAR.add(GOPHER_PRESENT_INDEX-20);
            }
            if(!MAXIMUM_BOUND_Y_NEAR.contains(GOPHER_PRESENT_INDEX)){
                GOPHER_POSITION_NEAR.add(GOPHER_PRESENT_INDEX+19);
                GOPHER_POSITION_NEAR.add(GOPHER_PRESENT_INDEX+20);
            }
        }

        if(!MAXIMUM_BOUND_X.contains(GOPHER_PRESENT_INDEX)){
            GOPHER_POSITION_CLOSE.add(GOPHER_PRESENT_INDEX+1);
            if(!MINIMUM_BOUND_Y.contains(GOPHER_PRESENT_INDEX)){
                GOPHER_POSITION_CLOSE.add(GOPHER_PRESENT_INDEX-9);
                GOPHER_POSITION_CLOSE.add(GOPHER_PRESENT_INDEX-10);
            }
            if(!MAXIMUM_BOUND_Y.contains(GOPHER_PRESENT_INDEX)){
                GOPHER_POSITION_CLOSE.add(GOPHER_PRESENT_INDEX+11);
                GOPHER_POSITION_CLOSE.add(GOPHER_PRESENT_INDEX+10);
            }
            if(!MINIMUM_BOUND_Y_NEAR.contains(GOPHER_PRESENT_INDEX)){
                GOPHER_POSITION_NEAR.add(GOPHER_PRESENT_INDEX-19);
                GOPHER_POSITION_NEAR.add(GOPHER_PRESENT_INDEX-20);
            }
            if(!MAXIMUM_BOUND_Y_NEAR.contains(GOPHER_PRESENT_INDEX)){
                GOPHER_POSITION_NEAR.add(GOPHER_PRESENT_INDEX+21);
                GOPHER_POSITION_NEAR.add(GOPHER_PRESENT_INDEX+20);
            }
        }

        //Fill near array based on array
        GOPHER_POSITION_NEAR.addAll(GOPHER_POSITION_CLOSE);

        MINIMUM_BOUND_X_NEAR.addAll(MINIMUM_BOUND_X);
        MAXIMUM_BOUND_X_NEAR.addAll(MAXIMUM_BOUND_X);
        MINIMUM_BOUND_Y_NEAR.addAll(MINIMUM_BOUND_Y);
        MAXIMUM_BOUND_Y_NEAR.addAll(MAXIMUM_BOUND_Y);

        //Filling the index based on bounds on X and Y
        if(!MINIMUM_BOUND_X_NEAR.contains(GOPHER_PRESENT_INDEX)){
            GOPHER_POSITION_NEAR.add(GOPHER_PRESENT_INDEX-2);
            if(!MINIMUM_BOUND_Y_NEAR.contains(GOPHER_PRESENT_INDEX)){
                GOPHER_POSITION_NEAR.add(GOPHER_PRESENT_INDEX-22);
                GOPHER_POSITION_NEAR.add(GOPHER_PRESENT_INDEX-21);
                GOPHER_POSITION_NEAR.add(GOPHER_PRESENT_INDEX-20);
            }
            if(!MAXIMUM_BOUND_Y_NEAR.contains(GOPHER_PRESENT_INDEX)){
                GOPHER_POSITION_NEAR.add(GOPHER_PRESENT_INDEX+18);
                GOPHER_POSITION_NEAR.add(GOPHER_PRESENT_INDEX+19);
                GOPHER_POSITION_NEAR.add(GOPHER_PRESENT_INDEX+20);
            }
            if(!MINIMUM_BOUND_Y.contains(GOPHER_PRESENT_INDEX)){
                GOPHER_POSITION_NEAR.add(GOPHER_PRESENT_INDEX-12);
                GOPHER_POSITION_NEAR.add(GOPHER_PRESENT_INDEX-21);
            }
            if(!MAXIMUM_BOUND_Y.contains(GOPHER_PRESENT_INDEX)){
                GOPHER_POSITION_NEAR.add(GOPHER_PRESENT_INDEX+8);
                GOPHER_POSITION_NEAR.add(GOPHER_PRESENT_INDEX+19);
            }
        }

        if(!MAXIMUM_BOUND_X_NEAR.contains(GOPHER_PRESENT_INDEX)){
            GOPHER_POSITION_NEAR.add(GOPHER_PRESENT_INDEX+2);
            if(!MINIMUM_BOUND_Y_NEAR.contains(GOPHER_PRESENT_INDEX)){
                GOPHER_POSITION_NEAR.add(GOPHER_PRESENT_INDEX-18);
                GOPHER_POSITION_NEAR.add(GOPHER_PRESENT_INDEX-19);
                GOPHER_POSITION_NEAR.add(GOPHER_PRESENT_INDEX-20);
            }
            if(!MAXIMUM_BOUND_Y_NEAR.contains(GOPHER_PRESENT_INDEX)){
                GOPHER_POSITION_NEAR.add(GOPHER_PRESENT_INDEX+22);
                GOPHER_POSITION_NEAR.add(GOPHER_PRESENT_INDEX+21);
                GOPHER_POSITION_NEAR.add(GOPHER_PRESENT_INDEX+20);
            }
            if(!MINIMUM_BOUND_Y.contains(GOPHER_PRESENT_INDEX)){
                GOPHER_POSITION_NEAR.add(GOPHER_PRESENT_INDEX-8);
                GOPHER_POSITION_NEAR.add(GOPHER_PRESENT_INDEX-19);
            }
            if(!MAXIMUM_BOUND_Y.contains(GOPHER_PRESENT_INDEX)){
                GOPHER_POSITION_NEAR.add(GOPHER_PRESENT_INDEX+12);
                GOPHER_POSITION_NEAR.add(GOPHER_PRESENT_INDEX+21);
            }
        }


        for(int i=0; i < 100; i++){
            if(i == GOPHER_PRESENT_INDEX){
                //Show Gopher Image
                mThumbQM.add(R.drawable.images);
            }else{
                //Show Question Mark
                mThumbQM.add(R.drawable.image_qm);
            }
        }

        gameProgress_gv = findViewById(R.id.gameProgress_gv);

        gameProgress_gv.setAdapter(new CurrentUserAdpater(this, mThumbQM));

        GAME_MODE = getIntent().getStringExtra("GAME_MODE");

        //Load UI based on game mode
        if(GAME_MODE.equals("Control")){
            startGame.setVisibility(View.GONE);

            playerOne.setEnabled(true);
            playerTwo.setEnabled(false);

            playerOne.setOnClickListener(view ->{
                this.callPlayerOneThread();
            });

            playerTwo.setOnClickListener(view -> {
                this.callPlayerTwoThread();

            });

        }else{
            THREAD_SLEEP_MIL = 2000;

           playerOne.setVisibility(View.GONE);
           playerTwo.setVisibility(View.GONE);

           startGame.setOnClickListener(view -> {
               Thread t1 = new Thread(new PlayerOne());
               t1.start();
           });
        }

    }

    private void callPlayerTwoThread() {
        PLAYER_NAME = PLAYER_TWO_NAME;
        Handler mHandler1 = new Handler() ;

        //Posting Runnable as per spec
        mHandler1.post(new Runnable() {
                          public void run() {
                              Thread t2 = new Thread(new PlayerTwo());
                              t2.start();
                          }
                      }
            );
   }

    private void callPlayerOneThread() {
        PLAYER_NAME = PLAYER_ONE_NAME;
        Thread t1 = new Thread(new PlayerOne());
        t1.start();
    }

    //Worker thread 1
    public class PlayerOne implements Runnable{

        @Override
        public void run() {
            Message msgOne = mHandler.obtainMessage(PLAYER_ONE_CODE);
            msgOne.arg1 = 999;

            //Post message
            //Disable Buttons so that user does not press when the thread is running
            mHandler.sendMessage(msgOne);

            try { Thread.sleep(THREAD_SLEEP_MIL); }
            catch (InterruptedException e) { System.out.println("Thread interrupted!") ; }

            Message msgTwo = mHandler.obtainMessage(PLAYER_ONE_CODE);

            //Player 1 Smart Strategy
            if(PLAYER_ONE_MODE.equals(CLOSE_MODE)){
                msgTwo.arg1 = getRandomElement(GOPHER_POSITION_CLOSE);
            }
            else if(PLAYER_ONE_MODE.equals(FAR_MODE)){
                msgTwo.arg1 = getRandomElement(GOPHER_POSITION_NEAR);
            }
            else{
                msgTwo.arg1 = getRandomNumber();
            }

            mHandler.sendMessage(msgTwo);
        }
    }

    //Worker thread 2
    public class PlayerTwo implements Runnable{

        @Override
        public void run() {
            Message msgOne = mHandler.obtainMessage(PLAYER_TWO_CODE);
            msgOne.arg1 = 999;

            //Disable Buttons so that user does not press when the thread is running
            mHandler.sendMessage(msgOne);

            try { Thread.sleep(THREAD_SLEEP_MIL); }
            catch (InterruptedException e) { System.out.println("Thread interrupted!") ; }

            Message msgTwo = mHandler.obtainMessage(PLAYER_TWO_CODE);

            //Player two Naive strategy
            msgTwo.arg1 = getRandomNumber();

            mHandler.sendMessage(msgTwo);
        }
    }



    public static int getRandomNumber(){
        Random rand = new Random();
        return rand.nextInt(100);
    }

    public static int getRandomElement(ArrayList<Integer> arrayList)
    {
        Random rand = new Random();
        return arrayList.get(rand.nextInt(arrayList.size()));
    }

    public synchronized boolean updateView(int index){
        boolean isTaken = false;

        View view = gameProgress_gv.getChildAt(index);

        if(view == null)
            return true;

        ImageView imageView = (ImageView) view;

        //Check if tile has already been updated
        if((Integer) imageView.getTag() != R.drawable.image_qm && (Integer) imageView.getTag() != R.drawable.images){
            if(PLAYER_ONE_TURN){
                PLAYER_ONE_TURN = false;
                PLAYER_TWO_TURN = true;
                this.updateButtons();
            }else{
                PLAYER_ONE_TURN = true;
                PLAYER_TWO_TURN = false;
                this.updateButtons();
            }
            //Toast.makeText(this,ALREADY_TAKEN, Toast.LENGTH_SHORT).show();
            Snackbar.make(findViewById(R.id.coordinatorLayout), PLAYER_NAME + ALREADY_TAKEN, Snackbar.LENGTH_SHORT).show();

            isTaken = true;
            return isTaken;
        }
        else{
            if(PLAYER_ONE_TURN){
                imageView.setImageResource(R.drawable.image_2);
                imageView.setTag(R.drawable.image_2);
                this.getProximity(index,2);
            }else{
                imageView.setImageResource(R.drawable.image_1);
                imageView.setTag(R.drawable.image_1);
                this.getProximity(index,1);
            }
            return isTaken;
        }


    }

    public synchronized void updateButtons(){
        if(PLAYER_ONE_TURN){
            playerOne.setEnabled(true);
            playerTwo.setEnabled(false);
        }
        else{
            playerOne.setEnabled(false);
            playerTwo.setEnabled(true);
        }
    }

    public synchronized void disableButtons(){
        playerOne.setEnabled(false);
        playerTwo.setEnabled(false);
    }

    //Display Snackbar based on Proximity
    public void getProximity(int index, int player){

       if(index == GOPHER_PRESENT_INDEX){
           //Toast.makeText(this,"You won!", Toast.LENGTH_SHORT).show();
           this.openWinnerDialog();
           WINNER_FOUND = true;
       }
       else if (GOPHER_POSITION_CLOSE.contains(index)){
           //Toast.makeText(this,GOPHER_CLOSE, Toast.LENGTH_SHORT).show();
           Snackbar.make(findViewById(R.id.coordinatorLayout), PLAYER_NAME + GOPHER_CLOSE, Snackbar.LENGTH_SHORT).show();
           if(player == 1){
                PLAYER_ONE_MODE = CLOSE_MODE;
           }
       }
       else if (GOPHER_POSITION_NEAR.contains(index)){
           //Toast.makeText(this,GOPHER_NEAR, Toast.LENGTH_SHORT).show();
           Snackbar.make(findViewById(R.id.coordinatorLayout), PLAYER_NAME + GOPHER_NEAR, Snackbar.LENGTH_SHORT).show();
           if(player == 1){
               PLAYER_ONE_MODE = FAR_MODE;
           }
       }
       else{
           //Toast.makeText(this,GOPHER_FAR, Toast.LENGTH_SHORT).show();
           Snackbar.make(findViewById(R.id.coordinatorLayout), PLAYER_NAME + GOPHER_FAR, Snackbar.LENGTH_SHORT).show();
       }
    }

    //Open Winning Modal
    public void openWinnerDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Congratulations! "+PLAYER_NAME+"you are the winner!. Press yes to start a new game.");
        builder.setTitle("You found the Gopher!");
        builder.setCancelable(false);
        builder.setPositiveButton("Yes", (d,which) ->{
                    finish();
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

}
