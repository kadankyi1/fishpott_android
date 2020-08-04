package gh.com.payswitch.thetellerandroid.card;

import com.google.gson.annotations.SerializedName;

public class ChargeRequestBody {
    private String amount;
    private String processing_code;
    private String transaction_id;
    private String desc;
    private String merchant_id;
    private String pan;
    @SerializedName("3d_url_response")
    private String d_url_response;
    private String exp_month;
    private String exp_year;
    private String cvv;
    private String currency;
    private String card_holder;
    private String customer_email;
    private String subscriber_number;
    @SerializedName("r-switch")
    private String rSwitch;

    public ChargeRequestBody getClient() {
        return this;
    }

    public void setClient(String amount, String processing_code, String transaction_id, String desc, String merchant_id,
                          String pan, String d_url_response, String exp_month, String exp_year, String cvv, String currency,
                          String card_holder, String customer_email, String subscriber_number, String rSwitch) {
        this.amount = amount;
        this.processing_code = processing_code;
        this.transaction_id = transaction_id;
        this.desc = desc;
        this.merchant_id = merchant_id;
        this.pan = pan;
        this.d_url_response = d_url_response;
        this.exp_month = exp_month;
        this.exp_year = exp_year;
        this.cvv = cvv;
        this.currency = currency;
        this.card_holder = card_holder;
        this.customer_email = customer_email;
        this.subscriber_number = subscriber_number;
        this.rSwitch = rSwitch;
    }

    public String getrSwitch() {
        return rSwitch;
    }

    public void setrSwitch(String rSwitch) {
        this.rSwitch = rSwitch;
    }

}
