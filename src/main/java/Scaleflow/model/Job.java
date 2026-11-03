package Scaleflow.model;

import java.time.Instant;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "jobs")
public class Job {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String fileName;

    @Enumerated(EnumType.STRING)
    private OperationType operation;

    @Enumerated(EnumType.STRING)
    private JobStatus status;

    private String inputPath;
    private String outputPath;
    //Determines when another worker can reclaim the job
    private Instant leaseExpiresAt;
    //Identifies the current owner, preventing an old worker from saving over a newer attempt
    private String claimToken;
    /**
     * 
     * @param id the id to set
     */

    public void setId(Long id) {
        this.id = id;
    }

    /**
     * 
     * @return id of the object
     */
    public Long getId() {
        return id;
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
     * @return String return the inputPath
     */
    public String getInputPath() {
        return inputPath;
    }

    /**
     * @param inputPath the inputPath to set
     */
    public void setInputPath(String inputPath) {
        this.inputPath = inputPath;
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


    /**
     * @return Instant return the leaseExpiresAt
     */
    public Instant getLeaseExpiresAt() {
        return leaseExpiresAt;
    }

    /**
     * @param leaseExpiresAt the leaseExpiresAt to set
     */
    public void setLeaseExpiresAt(Instant leaseExpiresAt) {
        this.leaseExpiresAt = leaseExpiresAt;
    }

    /**
     * @return String return the claimToken
     */
    public String getClaimToken() {
        return claimToken;
    }

    /**
     * @param claimToken the claimToken to set
     */
    public void setClaimToken(String claimToken) {
        this.claimToken = claimToken;
    }

}
