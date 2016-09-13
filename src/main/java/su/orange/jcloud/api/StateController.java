package su.orange.jcloud.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import su.orange.jcloud.MonitorService;

/**
 * @author Dmitry Tarasov
 *         Date: 08/25/2016
 *         Time: 20:09
 */
@RestController
public class StateController {
    
    @Autowired
    MonitorService monitorService;

    @RequestMapping(path = "state", method = RequestMethod.GET)
    public String getState() {
        return monitorService.getKeys().toString();
    }
}
