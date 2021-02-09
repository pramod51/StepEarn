package step.earn.stepearn.Adopters;

public class RewardsItems {
    String imageUrl;
    String tittle;
    String buttonText;


    public RewardsItems() {
    }

    public RewardsItems(String imageUrl, String tittle, String buttonText) {
        this.imageUrl = imageUrl;
        this.tittle = tittle;
        this.buttonText = buttonText;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getTittle() {
        return tittle;
    }

    public String getButtonText() {
        return buttonText;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setTittle(String tittle) {
        this.tittle = tittle;
    }

    public void setButtonText(String buttonText) {
        this.buttonText = buttonText;
    }

}
