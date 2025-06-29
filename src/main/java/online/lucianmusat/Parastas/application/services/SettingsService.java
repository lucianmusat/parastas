package online.lucianmusat.Parastas.application.services;

import online.lucianmusat.Parastas.application.SettingsFormDTO;
import online.lucianmusat.Parastas.domain.entities.Credentials;
import online.lucianmusat.Parastas.domain.entities.SmtpSettings;
import online.lucianmusat.Parastas.domain.entities.StateSettings;
import online.lucianmusat.Parastas.domain.repositories.CredentialsRepository;
import online.lucianmusat.Parastas.domain.repositories.SmtpSettingsRepository;
import online.lucianmusat.Parastas.domain.repositories.StateSettingsRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.ui.Model;

import java.util.Objects;

@Service
public class SettingsService {
    private static final Logger logger = LogManager.getLogger(SettingsService.class);

    @Autowired
    private SmtpSettingsRepository smtpSettingsRepository;
    @Autowired
    private StateSettingsRepository stateSettingsRepository;
    @Autowired
    private CredentialsRepository credentialsRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    public void initModels(Model model) {
        SmtpSettings smtpSettings = smtpSettingsRepository.findTopByOrderByIdAsc().orElse(new SmtpSettings());
        StateSettings stateSettings = stateSettingsRepository.findTopByOrderByIdAsc().orElse(new StateSettings());
        Credentials credentials = credentialsRepository.findTopByOrderByIdAsc().orElse(new Credentials());
        model.addAttribute("smtpSettings", smtpSettings);
        model.addAttribute("stateSettings", stateSettings);
        model.addAttribute("credentials", credentials);
        model.addAttribute("passwordMatch", true);
        model.addAttribute("saveSuccess", false);
    }

    public void updateSMTPSettings(final SettingsFormDTO settingsFormDTO) {
        SmtpSettings smtpSettings = smtpSettingsRepository.findTopByOrderByIdAsc().orElse(new SmtpSettings());

        if (!settingsFormDTO.getSmtpHost().isEmpty()) {
            logger.info("Saving SMTP Host: {}", settingsFormDTO.getSmtpHost());
            smtpSettings.setSmtpHost(settingsFormDTO.getSmtpHost().trim());
        }

        if (!settingsFormDTO.getSmtpPort().isEmpty()) {
            logger.info("Saving SMTP Port: {}", settingsFormDTO.getSmtpPort());
            smtpSettings.setSmtpPort(Integer.parseInt(settingsFormDTO.getSmtpPort().trim()));
        }

        if (!settingsFormDTO.getSmtpUsername().isEmpty()) {
            logger.info("Saving SMTP Username: {}", settingsFormDTO.getSmtpUsername());
            smtpSettings.setSmtpUsername(settingsFormDTO.getSmtpUsername().trim());
        }

        if (!settingsFormDTO.getSmtpPassword().isEmpty()) {
            logger.info("Saving SMTP Password");
            smtpSettings.setSmtpPassword(settingsFormDTO.getSmtpPassword().trim());
        }

        if (!settingsFormDTO.getRecipientEmailList().isEmpty()) {
            logger.info("Saving Recipient Email List: {}", settingsFormDTO.getRecipientEmailList());
            smtpSettings.setRecipients(settingsFormDTO.getRecipientEmailList().trim());
        }

        smtpSettingsRepository.save(smtpSettings);
    }

    public boolean updateCredentials(final SettingsFormDTO settingsFormDTO) {
        Credentials credentials = credentialsRepository.findTopByOrderByIdAsc().orElse(new Credentials());

        if (settingsFormDTO.getOldPassword().isEmpty()) {
            logger.debug("Old password is empty, disregarding credentials update");
            return settingsFormDTO.getNewPassword().isEmpty() || Objects.equals(settingsFormDTO.getUsername(), credentials.getUsername());
        } else {
            if (!passwordEncoder.matches(settingsFormDTO.getOldPassword(), credentials.getPassword())) {
                logger.error("Old password does not match!");
                return false;
            }
        }

        if (!settingsFormDTO.getUsername().isEmpty()) {
            logger.info("Saving new Username: {}", settingsFormDTO.getUsername());
            credentials.setUsername(settingsFormDTO.getUsername().trim());
        }

        if (!settingsFormDTO.getNewPassword().trim().isEmpty()) {
            logger.info("Saving new Password");
            credentials.setPassword(passwordEncoder.encode(settingsFormDTO.getNewPassword().trim()));
        }

        credentialsRepository.save(credentials);
        return true;
    }

    public void updateStateSettings(final SettingsFormDTO settingsFormDTO) {
        StateSettings stateSettings = stateSettingsRepository.findTopByOrderByIdAsc().orElse(new StateSettings());

        if (!settingsFormDTO.getRefreshPeriod().isEmpty()) {
            logger.info("Saving Refresh Period: {}", settingsFormDTO.getRefreshPeriod());
            stateSettings.setRefreshPeriodSeconds(Integer.parseInt(settingsFormDTO.getRefreshPeriod().trim()));
        }

        stateSettingsRepository.save(stateSettings);
    }

}
