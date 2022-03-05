package cn.jx.minesweeper4.HeroBean;

import org.litepal.crud.LitePalSupport;

public class Hero extends LitePalSupport {

    private int mId;//id
    private String mTime;//日期
    private String mTimer;//记录
    private String difficulty;//难度

    public int getmId() {
        return mId;
    }

    public void setmId(int mId) {
        this.mId = mId;
    }
    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public String getmTime() {
        return mTime;
    }

    public void setmTime(String mTime) {
        this.mTime = mTime;
    }

    public String getmTimer() {
        return mTimer;
    }

    public void setmTimer(String mTimer) {
        this.mTimer = mTimer;
    }
}
