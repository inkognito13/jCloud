package su.orange.jcloud;

/**
 * @author Dmitry Tarasov
 *         Date: 09/13/2016
 *         Time: 13:01
 */
public interface RemoteDriveManager {
    public boolean createDir(String dirPath);
    public boolean uploadFile(String localFile, String remoteDir, String excludeEnging);
}
