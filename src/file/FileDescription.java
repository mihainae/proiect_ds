package file;

import java.io.Serializable;

public class FileDescription implements Serializable {

    public String fileName;
    public long fileSize;
    public int chunkSize;
    public int sequenceLength;
    public String clientIp;
    public int clientPort;

    public FileDescription(String fileName, long fileSize,
                           int chunkSize, int sequenceLength, String clientIp, int clientPort) {
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.chunkSize = chunkSize;
        this.sequenceLength = sequenceLength;
        this.clientIp = clientIp;
        this.clientPort = clientPort;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setChunkSize(int chunkSize) {
        this.chunkSize = chunkSize;
    }

    public int getChunkSize() {
        return chunkSize;
    }

    public void setSequenceLength(int sequenceLength) {
        this.sequenceLength = sequenceLength;
    }

    public int getSequenceLength(int sequenceLength) {
        return sequenceLength;
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }

    public String getClientIp() {
        return clientIp;
    }

    public void setClientPort(int clientPort) {
        this.clientPort = clientPort;
    }

    public int getClientPort() {
        return clientPort;
    }
}
