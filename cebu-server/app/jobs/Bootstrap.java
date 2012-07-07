package jobs;
import play.*;
import play.jobs.*;
import play.test.*;
import models.*;

@OnApplicationStart
public class Bootstrap extends Job {
    public void doJob() {
    	
    	Logger.info("loading native BLAS libs");
    	
    	Play.getFile("lib/libjniarpack-linux-x86_64.so");
    	Play.getFile("lib/libjniblas-linux-x86_64.so");
    	Play.getFile("lib/libjnilapack-linux-x86_64.so");
    
    	System.out.println(org.netlib.blas.BLAS.getInstance().getClass().getName());
    	
        // Check if the database is empty
        if(Operator.count() == 0) {
            Fixtures.loadModels("initial-data.yml");
        }
    }
}