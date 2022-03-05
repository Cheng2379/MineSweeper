package cn.jx.minesweeper4;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.litepal.LitePal;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import cn.jx.minesweeper4.Adapter.RvAdapter;
import cn.jx.minesweeper4.HeroBean.Hero;

public class MainActivity extends AppCompatActivity {

    private TextView mTime, mLandmine, menu;
    private RecyclerView mRv;
    private RvAdapter mRvAdapter;
    private boolean isFirst;//第一次点击
    private Tile[][] mTile = new Tile[9][9];
    private int[][] xy = {
            {-1, -1},//左上角
            {-1, 0},//正上方
            {-1, 1},//右上角
            {0, -1},//左边
            {0, 1},//右边
            {1, -1},//左下角
            {1, 0},//正下方
            {1, 1}//右下角
    };
    private int[] map = new int[81];
    private List<Point> minePoint = new ArrayList<>();//地雷
    private List<TextView> tvlist = new ArrayList<>();//方块
    private boolean booleanTimer;
    private int mineNumes, nums, timeNumes = 0;
    private TextView mTvPage;
    private TimerTask mTimerTask;
    private String mTiming, mDate;
    private Hero hero = new Hero();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LitePal.initialize(this);
        initView();
        rvMapPrimary();

    }

    private void initView() {
        menu = findViewById(R.id.tv_menu);
        mTime = findViewById(R.id.time);
        mLandmine = findViewById(R.id.landmine);
        mRv = findViewById(R.id.rv);

        menu.setOnClickListener(v -> {
            setMenu(menu);
        });
    }

    //菜单按钮
    private void setMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.getMenuInflater().inflate(R.menu.menu_minesweeper, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(item -> {
            //重新开始
            if ("重新开始".equals(item.toString())) {
                if (booleanTimer) {
                    mTimerTask.cancel();
                }
                if (mineNumes == 10) {
                    rvMapPrimary();
                } else if (mineNumes == 40) {
                    rvMapIntermediate();
                }
            }
            //难度
            if ("初级".equals(item.toString())) {
                if (booleanTimer) {
                    mTimerTask.cancel();
                }
                rvMapPrimary();
            } else if ("中级".equals(item.toString())) {
                if (booleanTimer) {
                    mTimerTask.cancel();
                }
                rvMapIntermediate();
            } else if ("初级排行榜".equals(item.toString())) {
                primaryHero();
            } else if ("中级排行榜".equals(item.toString())) {
                intermediateHero();
            }else if ("关于".equals(item.toString())){
                about();
            }

            return false;
        });
        popupMenu.show();
    }

    //初始化地图
    private void initMap() {
        for (int i = 0; i < mTile.length; i++) {
            for (int j = 0; j < mTile[i].length; j++) {
                mTile[i][j] = new Tile();
                mTile[i][j].value = 0;
                mTile[i][j].open = false;
                mTile[i][j].longpress = false;
            }
        }
    }

    //生成地雷(初级)
    private void getMinesPrimary(int position) {
        List<Point> addPoint = new ArrayList<>();//总方格坐标轴
        Random random = new Random();
        List<Integer> list = new ArrayList<>();

        //将地图坐标加入链表
        for (int i = 0; i < mTile.length; i++) {
            for (int j = 0; j < mTile[i].length; j++) {
                Point po = new Point(i, j);
                addPoint.add(po);
            }
        }
        //随机数去重
        do {
            for (int i = 0; i < 10; i++) {
                int index = random.nextInt(mTile.length * mTile.length);
                list.add(index);
                if (index == position) {
                    list.clear();
                }
            }
            HashSet set = new HashSet(list);
            list.clear();
            list.addAll(set);
        } while (list.size() < 10);

        //生成地雷
        for (int i = 0; i < 10; i++) {
            minePoint.add(addPoint.get(list.get(i)));
            System.out.println("地雷坐标：(" + minePoint.get(i).x + "," + minePoint.get(i).y + ")");
        }
        System.out.println("地雷总数：" + minePoint.size());

        //在地图中标记雷的位置
        for (int i = 0; i < minePoint.size(); i++) {
            int mineX = minePoint.get(i).x;
            int mineY = minePoint.get(i).y;
            mTile[mineX][mineY].value = -1;
        }

        //打印地雷地图,添加数字提示
        for (int k = 0; k < xy.length; k++) {
            for (int i = 0; i < mTile.length; i++) {
                for (int j = 0; j < mTile[i].length; j++) {
                    if (mTile[i][j].value == -1) {
                        int offsetX = i + xy[k][0];
                        int offsetY = j + xy[k][1];
                        if (offsetX >= 0 && offsetX < mTile.length && offsetY >= 0 && offsetY < mTile.length) {
                            if (mTile[offsetX][offsetY].value != -1) {
                                mTile[offsetX][offsetY].value += 1;
                            }
                        }
                    }
                }
            }
        }

        //打印方格状态值
        for (int i = 0; i < mTile.length; i++) {
            for (int j = 0; j < mTile[i].length; j++) {
                System.out.print(" \t" + mTile[i][j].value);
            }
            System.out.println();
        }


    }

    //生成地雷(中级)
    private void getMinesIntermediate(int position) {
        List<Point> addPoint = new ArrayList<>();//总方格坐标轴
        Random random = new Random();
        List<Integer> list = new ArrayList<>();

        //将地图坐标加入链表
        for (int i = 0; i < mTile.length; i++) {
            for (int j = 0; j < mTile[i].length; j++) {
                Point po = new Point(i, j);
                addPoint.add(po);
            }
        }
        //随机数去重
        do {
            for (int i = 0; i < 40; i++) {
                int index = random.nextInt(256);
                list.add(index);
                if (index == position) {
                    list.clear();
                }
            }
            HashSet set = new HashSet(list);
            list.clear();
            list.addAll(set);
        } while (list.size() < 40);

        //生成地雷
        for (int i = 0; i < 40; i++) {
            minePoint.add(addPoint.get(list.get(i)));
            System.out.println("地雷坐标：(" + minePoint.get(i).x + "," + minePoint.get(i).y + ")");
        }
        System.out.println("地雷总数：" + minePoint.size());

        //在地图中标记雷的位置
        for (int i = 0; i < minePoint.size(); i++) {
            int mineX = minePoint.get(i).x;
            int mineY = minePoint.get(i).y;
            mTile[mineX][mineY].value = -1;
        }

        //打印地雷地图,添加数字提示
        for (int k = 0; k < xy.length; k++) {
            for (int i = 0; i < mTile.length; i++) {
                for (int j = 0; j < mTile[i].length; j++) {
                    if (mTile[i][j].value == -1) {
                        int offsetX = i + xy[k][0];
                        int offsetY = j + xy[k][1];
                        if (offsetX >= 0 && offsetX < 16 && offsetY >= 0 && offsetY < 16) {
                            if (mTile[offsetX][offsetY].value != -1) {
                                mTile[offsetX][offsetY].value += 1;
                            }
                        }
                    }
                }
            }
        }

        //打印方格状态值
        for (int i = 0; i < mTile.length; i++) {
            for (int j = 0; j < mTile[i].length; j++) {
                System.out.print(" \t" + mTile[i][j].value);
            }
            System.out.println();
        }
    }

    //扫描地雷
    @SuppressLint("SetTextI18n")
    private void scan(int position) {
        int mines = 0;
        int xPosition = position / mTile.length;
        int yPosition = position % mTile.length;
        // 终止条件
        //默认false，若已打开或者已标记，则返回
        if (mTile[xPosition][yPosition].open && !mTile[xPosition][yPosition].longpress) {
            return;
        }
        //设置为打开状态
        mTile[xPosition][yPosition].open = true;

        //有雷，则添加mines
        for (int[] value : xy) {
            int offsetX = position / mTile.length + value[0];
            int offsetY = position % mTile.length + value[1];
            if (offsetX >= 0 && offsetX < mTile.length && offsetY >= 0 && offsetY < mTile.length) {
                if (mTile[offsetX][offsetY].value == -1 && !mTile[offsetX][offsetY].longpress) {
                    mines++;
                }
            }
        }
        //如果无雷，则显示数字
        if (mines != 0) {
            //显示数字
            if (mTile[xPosition][yPosition].value != 0) {
                switch (mTile[xPosition][yPosition].value) {
                    case 1:
                        tvlist.get(position).setTextColor(getResources().getColor(R.color.one));
                        break;
                    case 2:
                        tvlist.get(position).setTextColor(getResources().getColor(R.color.two));
                        break;
                    case 3:
                        tvlist.get(position).setTextColor(getResources().getColor(R.color.three));
                        break;
                    case 4:
                        tvlist.get(position).setTextColor(getResources().getColor(R.color.four));
                        break;
                    case 5:
                        tvlist.get(position).setTextColor(getResources().getColor(R.color.five));
                        break;
                    case 6:
                        tvlist.get(position).setTextColor(getResources().getColor(R.color.six));
                        break;
                    case 7:
                        tvlist.get(position).setTextColor(getResources().getColor(R.color.seven));
                        break;
                    case 8:
                        tvlist.get(position).setTextColor(getResources().getColor(R.color.eight));
                        break;
                }
                tvlist.get(position).setText("" + mTile[xPosition][yPosition].value);
            }
            return;
        }
        //若不满足终止条件，则遍历地图，深度优先搜索
        for (int[] ints : xy) {
            int offsetX = position / mTile.length + ints[0];
            int offsetY = position % mTile.length + ints[1];
            if (offsetX >= 0 && offsetX < mTile.length && offsetY >= 0 && offsetY < mTile.length && !mTile[offsetX][offsetY].longpress) {
                int position2 = offsetX * mTile.length + offsetY;
                tvlist.get(position2).setBackgroundResource(R.drawable.background_onclick);
                scan(position2);
            }
        }
    }

    //初级难度
    @SuppressLint("SetTextI18n")
    private void rvMapPrimary()  {
        //初始化
        mTile = new Tile[9][9];
        map = new int[81];
        minePoint.clear();
        tvlist.clear();
        nums = 10;
        mineNumes = 10;
        timeNumes = 0;
        mLandmine.setText("" + nums);
        mTime.setText("0");
        isFirst = true;
        booleanTimer = false;
        initMap();

        GridLayoutManager gridLayout = new GridLayoutManager(this, 9) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        };
        mRv.setLayoutManager(gridLayout);
        mRvAdapter = new RvAdapter(this, map, R.layout.rv_map, new RvAdapter.Callback() {
            @Override
            public void callback(RvAdapter.Holder holder, int position) {
                super.callback(holder, position);

                mTvPage = mItemView.findViewById(R.id.tv_page);
                tvlist.add(mTvPage);
                int xPosition = position / 9;
                int yPosition = position % 9;

                //点击事件
                mTvPage.setOnClickListener(v -> {
                    //未标记执行游戏逻辑
                    if (!mTile[xPosition][yPosition].longpress) {
                        //第一次点击
                        if (isFirst) {
                            //开始计时
                            Timer mTimer = new Timer();
                            mTimerTask = new TimerTask() {
                                @Override
                                public void run() {
                                    runOnUiThread(() -> mTime.setText(++timeNumes + ""));
                                }
                            };
                            mTimer.schedule(mTimerTask, 1000, 1000);
                            booleanTimer = true;

                            //生成地雷
                            getMinesPrimary(position);
                            //深度优先搜索扫描地雷
                            scan(position);
                            //点击地雷游戏结束
                            if (mTile[xPosition][yPosition].value == -1) {
                                mTimer.cancel();
                                gameOver(position);
                            } else {
                                tvlist.get(position).setBackgroundResource(R.drawable.background_onclick);
                            }

                            isFirst = false;
                        } else {
                            //非第一次点击

                            //显示数字
                            if (mTile[xPosition][yPosition].value != 0) {
                                switch (mTile[xPosition][yPosition].value) {
                                    case 1:
                                        tvlist.get(position).setTextColor(getResources().getColor(R.color.one));
                                        break;
                                    case 2:
                                        tvlist.get(position).setTextColor(getResources().getColor(R.color.two));
                                        break;
                                    case 3:
                                        tvlist.get(position).setTextColor(getResources().getColor(R.color.three));
                                        break;
                                    case 4:
                                        tvlist.get(position).setTextColor(getResources().getColor(R.color.four));
                                        break;
                                    case 5:
                                        tvlist.get(position).setTextColor(getResources().getColor(R.color.five));
                                        break;
                                }
                                tvlist.get(position).setText("" + mTile[xPosition][yPosition].value);
                            }

                            //游戏失败
                            if (mTile[xPosition][yPosition].value == -1) {
                                mTimerTask.cancel();
                                gameOver(position);
                            } else {
                                //深度优先搜索扫描地雷
                                scan(position);
                                tvlist.get(position).setBackgroundResource(R.drawable.background_onclick);

                                //游戏胜利
                                winGame();
                            }
                        }
                    }
                });

                //长按事件
                mTvPage.setOnLongClickListener(v -> {

                    if (!mTile[xPosition][yPosition].open) {
                        if (!mTile[xPosition][yPosition].longpress) {
                            tvlist.get(position).setBackgroundResource(R.mipmap.red_flag);
                            mLandmine.setText(--nums + "");
                            mTile[xPosition][yPosition].longpress = true;
                        } else {
                            tvlist.get(position).setBackgroundResource(R.drawable.blue_squares);
                            mLandmine.setText(++nums + "");
                            mTile[xPosition][yPosition].longpress = false;
                        }
                    }
                    return true;
                });
            }
        });
        mRv.setAdapter(mRvAdapter);
    }

    //中级难度
    @SuppressLint("SetTextI18n")
    private void rvMapIntermediate() {
        //初始化
        mTile = new Tile[16][16];
        map = new int[256];
        minePoint.clear();
        tvlist.clear();
        nums = 40;
        mineNumes = 40;
        timeNumes = 0;
        mLandmine.setText("" + nums);
        mTime.setText("0");
        isFirst = true;
        booleanTimer = false;
        initMap();

        GridLayoutManager gridLayout = new GridLayoutManager(this, 16) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        };
        mRv.setLayoutManager(gridLayout);
        mRvAdapter = new RvAdapter(this, map, R.layout.rv_map_intermediate, new RvAdapter.Callback() {
            @Override
            public void callback(RvAdapter.Holder holder, int position) {
                super.callback(holder, position);

                mTvPage = mItemView.findViewById(R.id.tv_page);
                tvlist.add(mTvPage);
                int xPosition = position / 16;
                int yPosition = position % 16;

                //点击事件
                mTvPage.setOnClickListener(v -> {
                    //未标记执行游戏逻辑
                    if (!mTile[xPosition][yPosition].longpress) {
                        //第一次点击
                        if (isFirst) {
                            //开始计时
                            Timer mTimer = new Timer();
                            mTimerTask = new TimerTask() {
                                @Override
                                public void run() {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            mTime.setText(++timeNumes + "");
                                        }
                                    });
                                }
                            };
                            mTimer.schedule(mTimerTask, 1000, 1000);
                            booleanTimer = true;

                            //生成地雷
                            getMinesIntermediate(position);
                            //深度优先搜索扫描地雷
                            scan(position);
                            //点击地雷游戏结束
                            if (mTile[xPosition][yPosition].value == -1) {
                                mTimer.cancel();
                                gameOver(position);
                            } else {
                                tvlist.get(position).setBackgroundResource(R.drawable.background_onclick);
                            }

                            isFirst = false;
                        } else {
                            //非第一次点击

                            //显示数字
                            if (mTile[xPosition][yPosition].value != 0) {
                                switch (mTile[xPosition][yPosition].value) {
                                    case 1:
                                        tvlist.get(position).setTextColor(getResources().getColor(R.color.one));
                                        break;
                                    case 2:
                                        tvlist.get(position).setTextColor(getResources().getColor(R.color.two));
                                        break;
                                    case 3:
                                        tvlist.get(position).setTextColor(getResources().getColor(R.color.three));
                                        break;
                                    case 4:
                                        tvlist.get(position).setTextColor(getResources().getColor(R.color.four));
                                        break;
                                    case 5:
                                        tvlist.get(position).setTextColor(getResources().getColor(R.color.five));
                                        break;
                                }
                                tvlist.get(position).setText("" + mTile[xPosition][yPosition].value);
                            }

                            //游戏失败
                            if (mTile[xPosition][yPosition].value == -1) {
                                mTimerTask.cancel();
                                gameOver(position);
                            } else {
                                //深度优先搜索扫描地雷
                                scan(position);
                                tvlist.get(position).setBackgroundResource(R.drawable.background_onclick);

                                //游戏胜利
                                winGame();
                            }
                        }
                    }
                });

                //长按事件
                mTvPage.setOnLongClickListener(v -> {

                    if (!mTile[xPosition][yPosition].open) {
                        if (!mTile[xPosition][yPosition].longpress) {
                            tvlist.get(position).setBackgroundResource(R.mipmap.red_flag);
                            mLandmine.setText(--nums + "");
                            mTile[xPosition][yPosition].longpress = true;
                        } else {
                            tvlist.get(position).setBackgroundResource(R.drawable.blue_squares);
                            mLandmine.setText(++nums + "");
                            mTile[xPosition][yPosition].longpress = false;
                        }
                    }
                    return true;
                });
            }
        });
        mRv.setAdapter(mRvAdapter);
    }

    //游戏胜利
    @SuppressLint("SetTextI18n")
    private void winGame() {
        //已打开的方格数量
        int count = 0;

        for (Tile[] tiles : mTile) {
            for (Tile tile : tiles) {
                if (!tile.open) {
                    count++;
                }
            }
        }
        //剩余未打开方格数等于地雷总数则游戏胜利
        if (count == minePoint.size()) {
            //停止计时
            mTimerTask.cancel();
            //获取计时时间
            mTiming = this.mTime.getText().toString();

            //获取系统时间
            @SuppressLint("SimpleDateFormat")
            SimpleDateFormat time = new SimpleDateFormat("yyyy-MM-dd");
            mDate = time.format(new Date());

            System.out.println("日期：" + mDate);
            System.out.println("mineNums:" + mineNumes);
            //保存记录和日期
            if (mineNumes == 10) {
                hero.setDifficulty("初级");
                hero.setmTimer(mTiming);
                hero.setmTime(mDate);
                hero.save();
            } else if (mineNumes == 40) {
                hero.setDifficulty("中级");
                hero.setmTimer(mTiming);
                hero.setmTime(mDate);
                hero.save();
            }

            //剩余地雷数
            mLandmine.setText("0");
            //显示所有地雷
            for (int i = 0; i < mTile.length; i++) {
                for (int j = 0; j < mTile[i].length; j++) {
                    if (mTile[i][j].value == -1) {
                        tvlist.get(i * mTile.length + j).setBackgroundResource(R.drawable.red_yes_mine);
                    }
                }
            }
            //禁用点击事件
            for (int i = 0; i < mTile.length * mTile.length; i++) {
                tvlist.get(i).setEnabled(false);
            }
            //弹窗布局
            View view = LayoutInflater.from(this).inflate(R.layout.the_game_win, null);

            RelativeLayout mWinCloseOne = view.findViewById(R.id.win_close_one);//右上角关闭按钮
            TextView mWinComplete = view.findViewById(R.id.win_complete);//完成时间
            Button mWinRestart = view.findViewById(R.id.win_restart);//重新开始
            Button mWinCloseTwo = view.findViewById(R.id.win_close_two);//右下角关闭按钮

            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            AlertDialog alertDialog = builder.setView(view).create();

            //右上角关闭按钮
            mWinCloseOne.setOnClickListener(v -> {
                alertDialog.cancel();
            });

            //右下角关闭按钮
            mWinCloseTwo.setOnClickListener(v -> {
                alertDialog.cancel();
            });

            //显示完成时间
            mWinComplete.setText("完成!\n" + mTiming + "秒");
            //重新开始
            mWinRestart.setOnClickListener(v -> {
                if (mineNumes == 10) {
                    rvMapPrimary();
                } else if (mineNumes == 40) {
                    rvMapIntermediate();
                }
                alertDialog.cancel();
            });

            alertDialog.setCanceledOnTouchOutside(false);
            alertDialog.show();

        }
    }

    //游戏失败
    private void gameOver(int position) {
        //游戏失败显示所有地雷与点击的地雷
        int xPosition = position / mTile.length;
        int yPosition = position % mTile.length;
        for (int i = 0; i < mTile.length; i++) {
            for (int j = 0; j < mTile[i].length; j++) {
                if (mTile[i][j].value == -1) {
                    if (xPosition == i && yPosition == j) {
                        tvlist.get(position).setBackgroundResource(R.drawable.mine_onclick);
                    } else {
                        tvlist.get(i * mTile.length + j).setBackgroundResource(R.drawable.blue_mines);
                        if (mTile[i][j].longpress) {
                            tvlist.get(i * mTile.length + j).setBackgroundResource(R.drawable.red_yes_mine);
                        }
                    }
                } else {
                    if (mTile[i][j].longpress) {
                        tvlist.get(i * mTile.length + j).setBackgroundResource(R.drawable.red_no_mine);
                    }
                }
            }
        }

        //弹窗页面
        View view = LayoutInflater.from(this).inflate(R.layout.the_game_failed, null);

        Button mRestart = view.findViewById(R.id.restart_failed);
        Button mClose = view.findViewById(R.id.close_failed);

        RelativeLayout mFailedCloseOne = view.findViewById(R.id.failed_close_one);


        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        AlertDialog alertDialog = builder
                .setView(view)
                .create();

        //重新开始
        mRestart.setOnClickListener(v -> {
            if (mineNumes == 10) {
                rvMapPrimary();
            } else if (mineNumes == 40) {
                rvMapIntermediate();
            }
            alertDialog.cancel();
        });

        //右上角关闭按钮
        mFailedCloseOne.setOnClickListener(v -> {
            alertDialog.cancel();
            for (int i = 0; i < mTile.length * mTile.length; i++) {
                tvlist.get(i).setEnabled(false);
            }
        });

        //关闭
        mClose.setOnClickListener(v -> {
            alertDialog.cancel();
            for (int i = 0; i < mTile.length * mTile.length; i++) {
                tvlist.get(i).setEnabled(false);
            }
        });

        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();

    }

    //初级排行榜
    @SuppressLint("SetTextI18n")
    private void primaryHero() {
        View view = LayoutInflater.from(this).inflate(R.layout.primary_hero, null);

        TextView ranking = view.findViewById(R.id.ranking_list);
        RelativeLayout close = view.findViewById(R.id.primaryHero_close);
        RelativeLayout two   = view.findViewById(R.id.two_Hero);
        RelativeLayout three= view.findViewById(R.id.three_Hero);
        RelativeLayout four = view.findViewById(R.id.four_Hero);
        RelativeLayout five = view.findViewById(R.id.five_Hero);

        LinearLayout mLinearLayout = view.findViewById(R.id.primary_Hero);
        TextView not = view.findViewById(R.id.no_hero);
        TextView mOneTimer = view.findViewById(R.id.one_timer);
        TextView mOneTime = view.findViewById(R.id.one_time);
        TextView mTwoTimer = view.findViewById(R.id.two_timer);
        TextView mTwoTime = view.findViewById(R.id.two_time);
        TextView mThreeTimer = view.findViewById(R.id.three_timer);
        TextView mThreeTime = view.findViewById(R.id.three_time);
        TextView mFourTimer = view.findViewById(R.id.four_timer);
        TextView mFourTime = view.findViewById(R.id.four_time);
        TextView mFiveTimer = view.findViewById(R.id.five_timer);
        TextView mFiveTime = view.findViewById(R.id.five_time);

        ranking.setText("初级排行榜");
        AlertDialog alertDialog = new AlertDialog.Builder(this).setView(view).create();

        List<Hero> heroList = LitePal.where("difficulty = ?", "初级").order("mTimer asc").limit(5).find(Hero.class);

        if (heroList.size() == 0) {
            mLinearLayout.setVisibility(View.GONE);
            not.setVisibility(View.VISIBLE);
        } else {
            if (heroList.size() == 1) {
                mOneTimer.setText(heroList.get(0).getmTimer() + "秒");
                mOneTime.setText(heroList.get(0).getmTime());

                two.setVisibility(View.GONE);
                three.setVisibility(View.GONE);
                four.setVisibility(View.GONE);
                five.setVisibility(View.GONE);

            } else if (heroList.size() == 2) {
                mOneTimer.setText(heroList.get(0).getmTimer() + "秒");
                mOneTime.setText(heroList.get(0).getmTime());
                mTwoTimer.setText(heroList.get(1).getmTimer() + "秒");
                mTwoTime.setText(heroList.get(1).getmTime());

                three.setVisibility(View.GONE);
                four.setVisibility(View.GONE);
                five.setVisibility(View.GONE);

            } else if (heroList.size() == 3) {
                mOneTimer.setText(heroList.get(0).getmTimer() + "秒");
                mOneTime.setText(heroList.get(0).getmTime());
                mTwoTimer.setText(heroList.get(1).getmTimer() + "秒");
                mTwoTime.setText(heroList.get(1).getmTime());

                mThreeTimer.setText(heroList.get(2).getmTimer() + "秒");
                mThreeTime.setText(heroList.get(2).getmTime());

                four.setVisibility(View.GONE);
                five.setVisibility(View.GONE);
            } else if (heroList.size() == 4) {
                mOneTimer.setText(heroList.get(0).getmTimer() + "秒");
                mOneTime.setText(heroList.get(0).getmTime());

                mTwoTimer.setText(heroList.get(1).getmTimer() + "秒");
                mTwoTime.setText(heroList.get(1).getmTime());

                mThreeTimer.setText(heroList.get(2).getmTimer() + "秒");
                mThreeTime.setText(heroList.get(2).getmTime());

                mFourTimer.setText(heroList.get(3).getmTimer() + "秒");
                mFourTime.setText(heroList.get(3).getmTime());

                five.setVisibility(View.GONE);
            } else if (heroList.size() == 5) {
                mOneTimer.setText(heroList.get(0).getmTimer() + "秒");
                mOneTime.setText(heroList.get(0).getmTime());

                mTwoTimer.setText(heroList.get(1).getmTimer() + "秒");
                mTwoTime.setText(heroList.get(1).getmTime());

                mThreeTimer.setText(heroList.get(2).getmTimer() + "秒");
                mThreeTime.setText(heroList.get(2).getmTime());

                mFourTimer.setText(heroList.get(3).getmTimer() + "秒");
                mFourTime.setText(heroList.get(3).getmTime());

                mFiveTimer.setText(heroList.get(4).getmTimer() + "秒");
                mFiveTime.setText(heroList.get(4).getmTime());
            }
        }

        close.setOnClickListener(v -> {
            alertDialog.cancel();
        });

        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();


    }

    //中级排行榜
    @SuppressLint("SetTextI18n")
    private void intermediateHero() {

        View view = LayoutInflater.from(this).inflate(R.layout.intermediate_hero, null);

        RelativeLayout close = view.findViewById(R.id.primaryHero_close);

        TextView ranking = view.findViewById(R.id.ranking_list);
        RelativeLayout two   = view.findViewById(R.id.two_Hero);
        RelativeLayout three= view.findViewById(R.id.three_Hero);
        RelativeLayout four = view.findViewById(R.id.four_Hero);
        RelativeLayout five = view.findViewById(R.id.five_Hero);

        LinearLayout mLinearLayout = view.findViewById(R.id.primary_Hero);
        TextView not = view.findViewById(R.id.no_hero);
        TextView mOneTimer = view.findViewById(R.id.one_timer);
        TextView mOneTime = view.findViewById(R.id.one_time);
        TextView mTwoTimer = view.findViewById(R.id.two_timer);
        TextView mTwoTime = view.findViewById(R.id.two_time);
        TextView mThreeTimer = view.findViewById(R.id.three_timer);
        TextView mThreeTime = view.findViewById(R.id.three_time);
        TextView mFourTimer = view.findViewById(R.id.four_timer);
        TextView mFourTime = view.findViewById(R.id.four_time);
        TextView mFiveTimer = view.findViewById(R.id.five_timer);
        TextView mFiveTime = view.findViewById(R.id.five_time);

        ranking.setText("中级排行榜");
        AlertDialog alertDialog = new AlertDialog.Builder(this).setView(view).create();

        List<Hero> heroList = LitePal.where("difficulty = ?", "中级").order("mTimer asc").limit(5).find(Hero.class);

        if (heroList.size() == 0) {
            mLinearLayout.setVisibility(View.GONE);
            not.setVisibility(View.VISIBLE);
        } else {
            if (heroList.size() == 1) {
                mOneTimer.setText(heroList.get(0).getmTimer() + "秒");
                mOneTime.setText(heroList.get(0).getmTime());

                two.setVisibility(View.GONE);
                three.setVisibility(View.GONE);
                four.setVisibility(View.GONE);
                five.setVisibility(View.GONE);

            } else if (heroList.size() == 2) {
                mOneTimer.setText(heroList.get(0).getmTimer() + "秒");
                mOneTime.setText(heroList.get(0).getmTime());
                mTwoTimer.setText(heroList.get(1).getmTimer() + "秒");
                mTwoTime.setText(heroList.get(1).getmTime());

                three.setVisibility(View.GONE);
                four.setVisibility(View.GONE);
                five.setVisibility(View.GONE);

            } else if (heroList.size() == 3) {
                mOneTimer.setText(heroList.get(0).getmTimer() + "秒");
                mOneTime.setText(heroList.get(0).getmTime());
                mTwoTimer.setText(heroList.get(1).getmTimer() + "秒");
                mTwoTime.setText(heroList.get(1).getmTime());

                mThreeTimer.setText(heroList.get(2).getmTimer() + "秒");
                mThreeTime.setText(heroList.get(2).getmTime());

                four.setVisibility(View.GONE);
                five.setVisibility(View.GONE);
            } else if (heroList.size() == 4) {
                mOneTimer.setText(heroList.get(0).getmTimer() + "秒");
                mOneTime.setText(heroList.get(0).getmTime());

                mTwoTimer.setText(heroList.get(1).getmTimer() + "秒");
                mTwoTime.setText(heroList.get(1).getmTime());

                mThreeTimer.setText(heroList.get(2).getmTimer() + "秒");
                mThreeTime.setText(heroList.get(2).getmTime());

                mFourTimer.setText(heroList.get(3).getmTimer() + "秒");
                mFourTime.setText(heroList.get(3).getmTime());

                five.setVisibility(View.GONE);
            } else if (heroList.size() == 5) {
                mOneTimer.setText(heroList.get(0).getmTimer() + "秒");
                mOneTime.setText(heroList.get(0).getmTime());

                mTwoTimer.setText(heroList.get(1).getmTimer() + "秒");
                mTwoTime.setText(heroList.get(1).getmTime());

                mThreeTimer.setText(heroList.get(2).getmTimer() + "秒");
                mThreeTime.setText(heroList.get(2).getmTime());

                mFourTimer.setText(heroList.get(3).getmTimer() + "秒");
                mFourTime.setText(heroList.get(3).getmTime());

                mFiveTimer.setText(heroList.get(4).getmTimer() + "秒");
                mFiveTime.setText(heroList.get(4).getmTime());
            }
        }

        close.setOnClickListener(v -> {
            alertDialog.cancel();
        });

        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();

    }

    //关于
    private void about() {
        View view = LayoutInflater.from(this).inflate(R.layout.about, null);

        RelativeLayout close = view.findViewById(R.id.primaryHero_close);

        AlertDialog alertDialog = new AlertDialog.Builder(this).setView(view).create();

        close.setOnClickListener(v -> {
            alertDialog.cancel();
        });

        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();

    }

}