package online.lucianmusat.Parastas.controllers;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import online.lucianmusat.Parastas.entities.SmtpSettings;
import online.lucianmusat.Parastas.entities.SmtpSettingsRepository;
import online.lucianmusat.Parastas.utils.SettingsForm;

import jakarta.validation.Valid;


@Controller
public class SettingsController {

    private static final Logger logger = LogManager.getLogger(MainController.class);

    private final SmtpSettingsRepository smtpSettingsRepository;

    @Autowired
    public SettingsController(SmtpSettingsRepository smtpSettingsRepository) {
        this.smtpSettingsRepository = smtpSettingsRepository;
    }

    @RequestMapping("/settings")
    public String settingsPage(Model model) {
        SmtpSettings smtpSettings = smtpSettingsRepository.findById(1L).orElse(new SmtpSettings());
        model.addAttribute("smtpSettings", smtpSettings);
        return "settings";
    }

    @PostMapping("/save-settings")
    public String saveSettings(@Valid SettingsForm settingsForm, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            logger.error("Could not save settings: " + bindingResult.toString());
            return "redirect:/settings";
        }
        logger.info("Refresh Period: " + settingsForm.getRefreshPeriod());

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

        return "redirect:/settings";
    }

}
