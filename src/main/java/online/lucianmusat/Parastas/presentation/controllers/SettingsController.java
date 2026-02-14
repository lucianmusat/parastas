package online.lucianmusat.Parastas.presentation.controllers;

import jakarta.validation.Valid;
import online.lucianmusat.Parastas.application.SettingsFormDTO;
import online.lucianmusat.Parastas.application.services.SettingsService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;


@Controller
public class SettingsController {

    private static final Logger logger = LogManager.getLogger(SettingsController.class);

    @Autowired
    private SettingsService settingsService;

    @RequestMapping("/settings")
    public String settingsPage(Model model) {
        settingsService.initModels(model);
        model.addAttribute("activeTab", "user");
        return "settings";
    }

    @PostMapping("/save-user-settings")
    public String saveUserSettings(SettingsFormDTO settingsFormDTO, Model model) {
        boolean passwordMatch = settingsService.updateCredentials(settingsFormDTO);
        settingsService.initModels(model);
        model.addAttribute("passwordMatch", passwordMatch);
        model.addAttribute("saveSuccess", passwordMatch);
        model.addAttribute("activeTab", "user");
        return "settings";
    }

    @PostMapping("/save-notification-settings")
    public String saveNotificationSettings(SettingsFormDTO settingsFormDTO, Model model) {
        settingsService.updateSMTPSettings(settingsFormDTO);
        settingsService.updateStateSettings(settingsFormDTO);
        settingsService.initModels(model);
        model.addAttribute("saveSuccess", true);
        model.addAttribute("activeTab", "notifications");
        return "settings";
    }

}
