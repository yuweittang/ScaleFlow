package Scaleflow.model;

import jakarta.annotation.Generated;
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
    Long id;
    private String fileName;

    @Enumerated(EnumType.STRING)
    private OperationType operation;

    @Enumerated(EnumType.STRING)
    private JobStatus status;

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

}
