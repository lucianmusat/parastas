package online.lucianmusat.Parastas.presentation.controllers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.boot.web.servlet.error.ErrorController;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.RequestDispatcher;


@Controller
public class ErrController  implements ErrorController {

    private static final Logger logger = LogManager.getLogger(MainController.class);

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        if (status != null) {
            Integer statusCode = Integer.valueOf(status.toString());
            logger.error("An error {} occurred!", statusCode);
        }
        return "error";
    }

}
