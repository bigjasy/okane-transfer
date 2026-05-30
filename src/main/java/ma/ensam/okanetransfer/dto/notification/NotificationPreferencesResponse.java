package ma.ensam.okanetransfer.dto.notification;

public class NotificationPreferencesResponse {
    private boolean email;
    private boolean sms;
    private boolean push;

    public NotificationPreferencesResponse() {
    }

    public NotificationPreferencesResponse(boolean email, boolean sms, boolean push) {
        this.email = email;
        this.sms = sms;
        this.push = push;
    }

    public boolean isEmail() {
        return email;
    }

    public void setEmail(boolean email) {
        this.email = email;
    }

    public boolean isSms() {
        return sms;
    }

    public void setSms(boolean sms) {
        this.sms = sms;
    }

    public boolean isPush() {
        return push;
    }

    public void setPush(boolean push) {
        this.push = push;
    }
}
