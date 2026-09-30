package Scaleflow.dto;

import Scaleflow.model.JobStatus;
import Scaleflow.model.OperationType;

public class JobResponse {
    Long id;
    private String fileName;
    private OperationType operation;
    private JobStatus status;
    private String outputPath;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
     * @return JobStatus return the status
     */
    public JobStatus getStatus() {
        return status;
    }

    /**
     * @param status the status to set
     */
    public void setStatus(JobStatus status) {
        this.status = status;
    }

    /**
     * @return String return the outputPath
     */
    public String getOutputPath() {
        return outputPath;
    }

    /**
     * @param outputPath the outputPath to set
     */
    public void setOutputPath(String outputPath) {
        this.outputPath = outputPath;
    }

}
