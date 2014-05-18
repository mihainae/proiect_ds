package file;

import java.io.Serializable;

public class FileChunk implements Serializable{

    public String fileName;
    public int sequenceNumber;
    public int type;
    public byte [] chunk;

    public FileChunk(String fileName, int sequenceNumber, int type, byte [] chunk) {
        this.fileName = fileName;
        this.sequenceNumber = sequenceNumber;
        this.type = type;
        this.chunk = chunk;
    }

    public FileChunk(String fileName, int sequenceNumber, int type) {
        this.fileName = fileName;
        this.sequenceNumber = sequenceNumber;
        this.type = type;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }

    public void setSequenceNumber(int sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public void setChunk(byte [] chunk) {
        this.chunk = chunk;
    }

    public byte [] getChunk() {
        return chunk;
    }

}
