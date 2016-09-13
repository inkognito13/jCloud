package su.orange.jcloud;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * @author Dmitry Tarasov
 *         Date: 09/13/2016
 *         Time: 13:00
 */
@Component
@Profile(App.DEBUG_RPOFILE)
public class MockRemoteDriveManager implements RemoteDriveManager {

    private Logger logger = LoggerFactory.getLogger(MockRemoteDriveManager.class);
    
    @Override
    public boolean createDir(String dirPath) {
        logger.debug("MOCK : Remote dir created "+dirPath);
        return true;
    }

    @Override
    public boolean uploadFile(String localFile, String remoteDir, String excludeEnging) {
        logger.debug("MOCK : File uploaded local="+localFile+" remoteDir="+remoteDir+" excludeEnding="+excludeEnging);
        return true;
    }
}
