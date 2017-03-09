package yucl.learn.demo.fs.service;

/**
 * @author chunlei.yu
 *
 */
public interface ReplicateService {

	void replicate(String fileId);

    String getClusterId();

    void replicateFileFromPeer(String fileId);

}