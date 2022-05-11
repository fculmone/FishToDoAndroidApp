package com.example.doit;

import static android.view.View.INVISIBLE;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.example.doit.Adapter.ToDoAdapter;
import com.example.doit.Model.ToDoModel;
import com.example.doit.Utils.DatabaseHandler;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements DialogCloseListener {

    private RecyclerView tasksRecyclerView;
    private ToDoAdapter tasksAdapter;
    private FloatingActionButton fab;

    private List<ToDoModel> taskList;
    private DatabaseHandler db;

    public static boolean isFoodPellet = false;

    // Fish Tank Size
    private int tankWidth;
    private int tankHeight;

    // Images
    private ImageView fish;
    private ImageView food;

    // Fish Position
    private float fishX;
    private float fishY;
    private float newFishX;
    private float newFishY;

    // Food Position
    private float foodX;
    private float foodY;

    // Initialize classes
    private Handler handler = new Handler();
    private Timer timer = new Timer();




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();

        db = new DatabaseHandler(this);
        db.openDatabase();

        taskList = new ArrayList<>();

        tasksRecyclerView = findViewById(R.id.tasksRecyclerView);
        tasksRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        tasksAdapter = new ToDoAdapter(db, this);
        tasksRecyclerView.setAdapter(tasksAdapter);

        fab = findViewById(R.id.fab);

        ItemTouchHelper itemTouchHelper = new
                ItemTouchHelper(new RecyclerItemTouchHelper(tasksAdapter));
        itemTouchHelper.attachToRecyclerView(tasksRecyclerView);

        taskList = db.getAllTasks();
        Collections.reverse(taskList);
        tasksAdapter.setTasks(taskList);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddNewTask.newInstance().show(getSupportFragmentManager(), AddNewTask.TAG);
            }
        });


        fish = (ImageView) findViewById(R.id.fish);
        food = (ImageView) findViewById(R.id.food);
        food.setVisibility(INVISIBLE);

        // Get Tank Size (may not need this)
        WindowManager wm = getWindowManager();
        Display disp = wm.getDefaultDisplay();
        Point size = new Point();
        disp.getSize(size);
        tankWidth = size.x;
        tankHeight = dpToPixel(230);


        // Center Fish in Tank
        fishX = dpToPixel(120);
        fishY = dpToPixel(500);
        newFishX = fishX;
        newFishY = fishY;
        fish.setX(fishX);
        fish.setY(fishY);

        // Start the timer
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (isFoodPellet) {
                            if (food.getVisibility() == INVISIBLE) {
                                createFoodPellet();
                            }
                            goToFoodPellet();
                        } else {
                            swimAround();
                        }
                    }
                });
            }
        },0, 20);
    }

    public void swimAround() {
        if (newFishX < fishX) {
            if (fish.getScaleX() != -1) {
                fish.setScaleX(-1);
            }
            --fishX;
        } else if (newFishX > fishX) {
            if (fish.getScaleX() == -1) {
                fish.setScaleX(1);
            }
            ++fishX;
        }

        if (newFishY < fishY) {
            --fishY;
        } else if (newFishY > fishY) {
            ++fishY;
        }

        if (newFishX == fishX && newFishY == newFishY) {
            float min = fish.getWidth();
            float max = tankWidth - 2 * fish.getWidth();
            newFishX = (float) Math.floor(Math.random()*(max-min+1)+min);
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int screenHeight = displayMetrics.heightPixels;
            min = screenHeight - tankHeight + (float)Math.floor(fish.getHeight() / 2.0);
            max = screenHeight - 2 * fish.getHeight();
            newFishY = (float) Math.floor(Math.random()*(max-min+1)+min);
        }


        fish.setX(fishX);
        fish.setY(fishY);
    }

    public void createFoodPellet() {
        foodX = (float) Math.floor(Math.random() * (((tankWidth - 2 * fish.getWidth()) - fish.getWidth()) + fish.getWidth()));
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screenHeight = displayMetrics.heightPixels;
        float min = screenHeight - tankHeight - dpToPixel(35);
        float max = screenHeight - 2 * fish.getHeight();
        foodY = (float) Math.floor(Math.random()*(max-min+1)+min);

        food.setX(foodX);
        food.setY(foodY);

        food.setVisibility(View.VISIBLE);
    }

    public void goToFoodPellet() {
        if (food.getVisibility() == View.VISIBLE) {
            if (foodX < fishX) {
                if (fish.getScaleX() != -1) {
                    fish.setScaleX(-1);
                }
                if (foodY == food.getY()) {
                    foodY -= fish.getHeight() / 4;
                }
                --fishX;
            } else if (foodX > fishX) {
                if (fish.getScaleX() == -1) {
                    fish.setScaleX(1);
                }
                if (foodX == food.getX()) {
                    foodX -= fish.getWidth() / 3;
                }
                if (foodY == food.getY()) {
                    foodY -= fish.getHeight() / 4;
                }
                ++fishX;
            }

            if (foodY < fishY) {
                --fishY;
            } else if (foodY > fishY) {
                ++fishY;
            }

            if (foodX == fishX && foodY == fishY) {
                food.setVisibility(View.INVISIBLE);
                isFoodPellet = false;
            }

            fish.setX(fishX);
            fish.setY(fishY);
        }
    }



    public int dpToPixel(int dp) {
        return (int) TypedValue.applyDimension( TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics() );
    }

    @Override
    public void handleDialogClose(DialogInterface dialog) {
        taskList = db.getAllTasks();
        Collections.reverse(taskList);
        tasksAdapter.setTasks(taskList);
        tasksAdapter.notifyDataSetChanged();
    }
}