package step.earn.stepearn.Adopters;

public class HistoryItems {
    String date,Level;
    int Today,Total;

    public HistoryItems() {
    }

    public HistoryItems(String date, int today, int total) {
        this.date = date;
        Today = today;
        Total = total;
    }

    public String getLevel() {
        return Level;
    }

    public void setLevel(String level) {
        Level = level;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getToday() {
        return Today;
    }

    public void setToday(int today) {
        Today = today;
    }

    public int getTotal() {
        return Total;
    }

    public void setTotal(int total) {
        Total = total;
    }
}
