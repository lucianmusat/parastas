package online.lucianmusat.Parastas.application;

import jakarta.validation.constraints.NotBlank;

public class SettingsFormDTO {

    @NotBlank(message = "SMTP Host cannot be empty")
    private String smtpHost;
    @NotBlank(message = "SMTP Port cannot be empty")
    private String smtpPort;
    @NotBlank(message = "SMTP Username cannot be empty")
    private String smtpUsername;
    @NotBlank(message = "SMTP Password cannot be empty")
    private String smtpPassword;
    @NotBlank(message = "Recipient Email List cannot be empty")
    private String recipientEmailList;
    @NotBlank(message = "Refresh Period cannot be empty")
    private String refreshPeriod;
    @NotBlank(message = "Username cannot be empty")
    private String username;
    private String oldPassword;
    private String newPassword;

    public String getSmtpHost() {
        return smtpHost;
    }

    public void setSmtpHost(String smtpHost) {
        this.smtpHost = smtpHost;
    }

    public String getSmtpPort() {
        return smtpPort;
    }

    public void setSmtpPort(String smtpPort) {
        this.smtpPort = smtpPort;
    }

    public String getSmtpUsername() {
        return smtpUsername;
    }

    public void setSmtpUsername(String smtpUsername) {
        this.smtpUsername = smtpUsername;
    }

    public String getSmtpPassword() {
        return smtpPassword;
    }

    public void setSmtpPassword(String smtpPassword) {
        this.smtpPassword = smtpPassword;
    }

    public String getRecipientEmailList() {
        return recipientEmailList;
    }

    public void setRecipientEmailList(String recipientEmailList) {
        this.recipientEmailList = recipientEmailList;
    }

    public String getRefreshPeriod() {
        return refreshPeriod;
    }

    public void setRefreshPeriod(String refreshPeriod) {
        this.refreshPeriod = refreshPeriod;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String newUsername) {
        username = newUsername;
    }

    public String getOldPassword() {
        return oldPassword;
    }

    public void setOldPassword(String newPassword) {
        oldPassword = newPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}

