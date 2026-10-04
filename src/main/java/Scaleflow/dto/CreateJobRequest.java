package Scaleflow.dto;

import Scaleflow.model.OperationType;

public class CreateJobRequest {
    private String fileName;
    private OperationType operation;
    private String inputPath;

    /**
     * @return String return the operation
     */
    public OperationType getOperation() {
        return operation;
    }

    /**
     * @param operation the operation to set
     */
    public void setOperation(OperationType operation) {
        this.operation = operation;
    }

    /**
     * @return String return the fileName
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * @param fileName the fileName to set
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }


    /**
     * @return String return the inputpath
     */
    public String getInputPath() {
        return inputPath;
    }

    /**
     * @param inputPath the inputpath to set
     */
    public void setInputPath(String inputPath) {
        this.inputPath = inputPath;
    }

}
