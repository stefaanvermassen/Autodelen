package database;

/**
 * Created by Cedric on 2/16/14.
 */
public class DataAccessException extends RuntimeException {

    private Exception innerException;
    private String desc;

    public DataAccessException(String desc, Exception exception){
        this.innerException = exception;
        this.desc = desc;
    }

    public Exception getInnerException() {
        return innerException;
    }

    public String getDesc() {
        return desc;
    }

    @Override
    public String getMessage(){
        return super.getMessage() + " -- " + (innerException != null ? innerException.getMessage() : "");
    }
}
