package online.lucianmusat.Parastas.controllers;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import online.lucianmusat.Parastas.entities.SmtpSettings;
import online.lucianmusat.Parastas.entities.SmtpSettingsRepository;
import online.lucianmusat.Parastas.entities.StateSettingsRepository;
import online.lucianmusat.Parastas.entities.Credentials;
import online.lucianmusat.Parastas.entities.CredentialsRepository;
import online.lucianmusat.Parastas.entities.StateSettings;
import online.lucianmusat.Parastas.utils.SettingsForm;

import jakarta.validation.Valid;


@Controller
public class SettingsController {

    private static final Logger logger = LogManager.getLogger(MainController.class);

    @Autowired
    private SmtpSettingsRepository smtpSettingsRepository;
    @Autowired
    private StateSettingsRepository stateSettingsRepository;
    @Autowired
    private CredentialsRepository credentialsRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @RequestMapping("/settings")
    public String settingsPage(Model model) {
        initModels(model);
        return "settings";
    }

    @PostMapping("/save-settings")
    public String saveSettings(@Valid SettingsForm settingsForm, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            logger.error("Could not save settings: " + bindingResult.toString());
            return "redirect:/settings";
        }
        updateSMTPSettings(settingsForm);
        updateStateSettings(settingsForm);
        initModels(model);
        model.addAttribute("passwordMatch", updateCredentials(settingsForm));
        model.addAttribute("saveSuccess", true);
        return "settings";
    }

    private void initModels(Model model) {
        SmtpSettings smtpSettings = smtpSettingsRepository.findById(1L).orElse(new SmtpSettings());
        StateSettings stateSettings = stateSettingsRepository.findById(1L).orElse(new StateSettings());
        Credentials credentials = credentialsRepository.findById(1L).orElse(new Credentials());
        model.addAttribute("smtpSettings", smtpSettings);
        model.addAttribute("stateSettings", stateSettings);
        model.addAttribute("credentials", credentials);
        model.addAttribute("passwordMatch", true);
        model.addAttribute("saveSuccess", false);
    }

    private boolean updateCredentials(final SettingsForm settingsForm) {
        Credentials credentials = credentialsRepository.findById(1L).orElse(new Credentials());
        if (settingsForm.getOldPassword().isEmpty()) {
            logger.debug("Old password is empty, disregarding credentials update");
            if (settingsForm.getNewPassword().isEmpty() || settingsForm.getUsername() == credentials.getUsername()) {
                return true;
            }
            return false;
        } else {
            if (!passwordEncoder.matches(settingsForm.getOldPassword(), credentials.getPassword())) {
                logger.error("Old password does not match!");
                return false;
            }
        }
        if (!settingsForm.getUsername().isEmpty()) {
            logger.info("Saving new Username: " + settingsForm.getUsername());
            credentials.setUsername(settingsForm.getUsername().trim());
        }

        if (!settingsForm.getNewPassword().trim().isEmpty()) {
            logger.info("Saving new Password");
            credentials.setPassword(passwordEncoder.encode(settingsForm.getNewPassword().trim()));
        }
        credentialsRepository.save(credentials);
        return true;
    }

    private void updateStateSettings(final SettingsForm settingsForm) {
        StateSettings stateSettings = stateSettingsRepository.findById(1L).orElse(new StateSettings());
        if (!settingsForm.getRefreshPeriod().isEmpty()) {
            logger.info("Saving Refresh Period: " + settingsForm.getRefreshPeriod());
            stateSettings.setRefreshPeriodSeconds(Integer.parseInt(settingsForm.getRefreshPeriod().trim()));
        }
        stateSettingsRepository.save(stateSettings);
    }

    private void updateSMTPSettings(final SettingsForm settingsForm) {
        SmtpSettings smtpSettings = smtpSettingsRepository.findById(1L).orElse(new SmtpSettings());
        if (!settingsForm.getSmtpHost().isEmpty()) {
            logger.info("Saving SMTP Host: " + settingsForm.getSmtpHost());
            smtpSettings.setSmtpHost(settingsForm.getSmtpHost().trim());
        }
        if (!settingsForm.getSmtpPort().isEmpty()) {
            logger.info("Saving SMTP Port: " + settingsForm.getSmtpPort());
            smtpSettings.setSmtpPort(Integer.parseInt(settingsForm.getSmtpPort().trim()));
        }
        if (!settingsForm.getSmtpUsername().isEmpty()) {
            logger.info("Saving SMTP Username: " + settingsForm.getSmtpUsername());
            smtpSettings.setSmtpUsername(settingsForm.getSmtpUsername().trim());
        }
        if (!settingsForm.getSmtpPassword().isEmpty()) {
            logger.info("Saving SMTP Password: " + settingsForm.getSmtpPassword());
            smtpSettings.setSmtpPassword(settingsForm.getSmtpPassword().trim());
        }
        if (!settingsForm.getRecipientEmailList().isEmpty()) {
            logger.info("Saving Recipient Email List: " + settingsForm.getRecipientEmailList());
            smtpSettings.setRecipients(settingsForm.getRecipientEmailList().trim());
        }
        smtpSettingsRepository.save(smtpSettings);
    }

}
