package cn.jx.minesweeper4;

/**
 * 矩阵地图
 */
public class Tile {

    public int value;//方块状态值,默认值：0，雷：-1
    public boolean longpress;//长按状态
    public boolean open;//打开状态

    public Tile() {
        this.value = 0;
        this.open = false;
        this.longpress = false;
    }

}
