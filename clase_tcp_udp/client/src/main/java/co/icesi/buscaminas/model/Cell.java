package co.icesi.buscaminas.model;

import java.io.Serializable;

public class Cell implements Serializable {
    private boolean isLandMine;
    private int value;
    private boolean hide;
    private boolean showAll;
    private boolean isMarked;

    public Cell(boolean isMine, int value) {
        this.isLandMine = isMine;
        this.value = value;
        this.hide = true;
        this.showAll = false;
        this.isMarked = false;
    }

    public boolean isMarked() {
        return isMarked;
    }

    public void setMarked(boolean marked) {
        isMarked = marked;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public boolean isLandMine() {
        return isLandMine;
    }

    public void setLandMine(boolean landMine) {
        isLandMine = landMine;
    }

    public boolean isHide() {
        return hide;
    }

    public void setHide(boolean hide) {
        this.hide = hide;
    }

    public boolean isShowAll() {
        return showAll;
    }

    public void setShowAll(boolean showAll) {
        this.showAll = showAll;
    }

    @Override
    public String toString() {
        if (isMarked) {
            return "\u001B[33mM\u001B[0m";
        }
        if (hide && !showAll) {
            return ".";
        }
        if (isLandMine) {
            return "\u001B[31m*\u001B[0m";
        }
        return value == 0 ? "0" : String.valueOf(value);
    }
}
