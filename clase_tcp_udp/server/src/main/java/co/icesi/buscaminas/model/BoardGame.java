package co.icesi.buscaminas.model;

public class BoardGame {
    private Cell[][] board;
    private int mines;

    public synchronized int initGame(int n, int m, int mines) {
        this.mines = mines;
        board = new Cell[n][m];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                board[i][j] = new Cell(false, 0);
            }
        }

        int placed = 0;
        while (placed < mines) {
            int i = (int) (Math.random() * n);
            int j = (int) (Math.random() * m);
            if (!board[i][j].isLandMine()) {
                board[i][j].setLandMine(true);
                placed++;
            }
        }

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                if (!board[i][j].isLandMine()) {
                    board[i][j].setValue(getMinesAround(i, j));
                }
            }
        }
        return placed;
    }

    public synchronized void showAll(boolean show) {
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[0].length; j++) {
                board[i][j].setShowAll(show);
                board[i][j].setHide(false);
            }
        }
    }

    private int getMinesAround(int i, int j) {
        int count = 0;
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                if (x == 0 && y == 0) continue;
                int nx = i + x;
                int ny = j + y;
                if (nx >= 0 && nx < board.length && ny >= 0 && ny < board[0].length && board[nx][ny].isLandMine()) {
                    count++;
                }
            }
        }
        return count;
    }

    public synchronized void printBoard() {
        if (board == null) {
            return;
        }
        System.out.println();
        System.out.print("   ");
        for (int i = 0; i < board[0].length; i++) {
            System.out.print(" " + i);
        }
        System.out.println();
        for (int i = 0; i < board.length; i++) {
            System.out.print(i + " [");
            for (int j = 0; j < board[0].length; j++) {
                System.out.print(" " + board[i][j]);
            }
            System.out.println(" ]");
        }
    }

    public synchronized boolean selectCell(int i, int j) {
        if (board == null || i < 0 || i >= board.length || j < 0 || j >= board[0].length) {
            throw new RuntimeException("Cell no valid");
        }

        Cell cell = board[i][j];
        if (cell.isLandMine()) {
            showAll(true);
            throw new RuntimeException("Game over");
        }

        if (cell.isHide()) {
            showCells(i, j, true);
        }
        return validWin();
    }

    private boolean validWin() {
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[0].length; j++) {
                if (!board[i][j].isLandMine() && board[i][j].isHide()) {
                    return false;
                }
            }
        }
        return true;
    }

    private void showCells(int i, int j, boolean deep) {
        if (i < 0 || i >= board.length || j < 0 || j >= board[0].length || !board[i][j].isHide()) {
            return;
        }

        board[i][j].setHide(false);
        if (board[i][j].getValue() != 0) {
            return;
        }

        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                if (x == 0 && y == 0) continue;
                int nx = i + x;
                int ny = j + y;
                if (nx >= 0 && nx < board.length && ny >= 0 && ny < board[0].length) {
                    if (board[nx][ny].isHide() && !board[nx][ny].isLandMine()) {
                        showCells(nx, ny, deep);
                    }
                }
            }
        }
    }

    public synchronized Cell[][] getBoard() {
        if (board == null) {
            return new Cell[0][0];
        }
        return board;
    }

    public synchronized void markCell(int i, int j) {
        if (board == null || i < 0 || i >= board.length || j < 0 || j >= board[0].length) {
            throw new RuntimeException("Cell no valid");
        }
        Cell cell = board[i][j];
        if (cell.isHide()) {
            cell.setMarked(!cell.isMarked());
        }
    }
}
