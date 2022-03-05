package cn.jx.minesweeper4.HeroBean;

import org.litepal.crud.LitePalSupport;

public class HeroIntermediate extends LitePalSupport {

    private String mTime;
    private String mTimer;

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
